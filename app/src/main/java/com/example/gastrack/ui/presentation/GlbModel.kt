package com.example.gastrack.ui.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val GLB_TAG = "GlbModel"

/** One drawable mesh part. vertices = x y z nx ny nz u v (already scaled to meters, bottom-center at origin). */
class GlbPrimitive(
    val vertices: FloatArray,
    val indices: IntArray,
    val baseColor: FloatArray,
    val texture: Bitmap?
)

class GlbData(val primitives: List<GlbPrimitive>)

private class Acc(val count: Int, val comps: Int, val type: Int, val offset: Int, val stride: Int, val normalized: Boolean)

private class RawPrim(
    val pos: FloatArray, val nrm: FloatArray, val uv: FloatArray,
    val idx: IntArray, val color: FloatArray, val tex: Bitmap?
)

object GlbParser {

    /** Safe to call on a background thread. [targetSizeMeters] = size of the model's largest dimension. */
    fun parse(context: Context, assetPath: String, targetSizeMeters: Float): GlbData {
        val bytes = context.assets.open(assetPath).use { it.readBytes() }
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        require(bytes.size > 28 && bb.getInt(0) == 0x46546C67) { "Not a GLB file: $assetPath" }
        val jsonLen = bb.getInt(12)
        require(bb.getInt(16) == 0x4E4F534A) { "First GLB chunk is not JSON" }
        val root = JSONObject(String(bytes, 20, jsonLen, Charsets.UTF_8))
        val binChunk = 20 + jsonLen
        require(binChunk + 8 <= bytes.size && bb.getInt(binChunk + 4) == 0x004E4942) { "GLB has no binary chunk" }
        val binStart = binChunk + 8

        val accessors = root.optJSONArray("accessors") ?: JSONArray()
        val views = root.optJSONArray("bufferViews") ?: JSONArray()
        val nodes = root.optJSONArray("nodes") ?: JSONArray()
        val meshes = root.optJSONArray("meshes") ?: JSONArray()
        val materials = root.optJSONArray("materials")
        val textures = root.optJSONArray("textures")
        val images = root.optJSONArray("images")

        fun compSize(t: Int) = when (t) { 5120, 5121 -> 1; 5122, 5123 -> 2; else -> 4 }

        fun acc(index: Int): Acc? {
            val a = accessors.getJSONObject(index)
            val comps = when (a.getString("type")) {
                "SCALAR" -> 1; "VEC2" -> 2; "VEC3" -> 3; "VEC4" -> 4; "MAT4" -> 16
                else -> return null
            }
            if (!a.has("bufferView")) return null
            val v = views.getJSONObject(a.getInt("bufferView"))
            val type = a.getInt("componentType")
            val stride = v.optInt("byteStride", 0).let { if (it == 0) comps * compSize(type) else it }
            val offset = binStart + v.optInt("byteOffset", 0) + a.optInt("byteOffset", 0)
            return Acc(a.getInt("count"), comps, type, offset, stride, a.optBoolean("normalized", false))
        }

        fun comp(a: Acc, i: Int, c: Int): Float {
            val pos = a.offset + i * a.stride + c * compSize(a.type)
            return when (a.type) {
                5126 -> bb.getFloat(pos)
                5121 -> (bytes[pos].toInt() and 0xFF).let { if (a.normalized) it / 255f else it.toFloat() }
                5123 -> (bb.getShort(pos).toInt() and 0xFFFF).let { if (a.normalized) it / 65535f else it.toFloat() }
                5120 -> bytes[pos].toInt().let { if (a.normalized) max(it / 127f, -1f) else it.toFloat() }
                5122 -> bb.getShort(pos).toInt().let { if (a.normalized) max(it / 32767f, -1f) else it.toFloat() }
                5125 -> (bb.getInt(pos).toLong() and 0xFFFFFFFFL).toFloat()
                else -> 0f
            }
        }

        fun index(a: Acc, i: Int): Int {
            val pos = a.offset + i * a.stride
            return when (a.type) {
                5121 -> bytes[pos].toInt() and 0xFF
                5123 -> bb.getShort(pos).toInt() and 0xFFFF
                else -> bb.getInt(pos)
            }
        }

        val imageCache = HashMap<Int, Bitmap?>()
        fun loadImage(i: Int): Bitmap? = imageCache.getOrPut(i) {
            val img = images?.optJSONObject(i) ?: return@getOrPut null
            if (!img.has("bufferView")) return@getOrPut null
            val v = views.getJSONObject(img.getInt("bufferView"))
            BitmapFactory.decodeByteArray(bytes, binStart + v.optInt("byteOffset", 0), v.getInt("byteLength"))
        }

        val raws = ArrayList<RawPrim>()

        fun addMesh(meshIndex: Int, world: FloatArray) {
            val prims = meshes.getJSONObject(meshIndex).optJSONArray("primitives") ?: return
            for (pi in 0 until prims.length()) {
                val pr = prims.getJSONObject(pi)
                if (pr.optInt("mode", 4) != 4) continue
                val attrs = pr.optJSONObject("attributes") ?: continue
                if (!attrs.has("POSITION")) continue
                val pa = acc(attrs.getInt("POSITION")) ?: continue
                val na = if (attrs.has("NORMAL")) acc(attrs.getInt("NORMAL")) else null
                val ta = if (attrs.has("TEXCOORD_0")) acc(attrs.getInt("TEXCOORD_0")) else null
                val n = pa.count
                val pos = FloatArray(n * 3)
                val nrm = FloatArray(n * 3)
                val uv = FloatArray(n * 2)
                for (i in 0 until n) {
                    val x = comp(pa, i, 0); val y = comp(pa, i, 1); val z = comp(pa, i, 2)
                    pos[i * 3] = world[0] * x + world[4] * y + world[8] * z + world[12]
                    pos[i * 3 + 1] = world[1] * x + world[5] * y + world[9] * z + world[13]
                    pos[i * 3 + 2] = world[2] * x + world[6] * y + world[10] * z + world[14]
                    if (na != null) {
                        val nx = comp(na, i, 0); val ny = comp(na, i, 1); val nz = comp(na, i, 2)
                        var tx = world[0] * nx + world[4] * ny + world[8] * nz
                        var ty = world[1] * nx + world[5] * ny + world[9] * nz
                        var tz = world[2] * nx + world[6] * ny + world[10] * nz
                        val len = sqrt(tx * tx + ty * ty + tz * tz)
                        if (len > 1e-6f) { tx /= len; ty /= len; tz /= len }
                        nrm[i * 3] = tx; nrm[i * 3 + 1] = ty; nrm[i * 3 + 2] = tz
                    } else {
                        nrm[i * 3 + 1] = 1f
                    }
                    if (ta != null) { uv[i * 2] = comp(ta, i, 0); uv[i * 2 + 1] = comp(ta, i, 1) }
                }
                val ia = if (pr.has("indices")) acc(pr.getInt("indices")) else null
                val all = if (ia != null) IntArray(ia.count) { index(ia, it) } else IntArray(n) { it }
                val idx = all.copyOf((all.size / 3) * 3)

                var color = floatArrayOf(1f, 1f, 1f, 1f)
                var tex: Bitmap? = null
                val mi = pr.optInt("material", -1)
                if (mi >= 0 && materials != null) {
                    val pbr = materials.getJSONObject(mi).optJSONObject("pbrMetallicRoughness")
                    if (pbr != null) {
                        val f = pbr.optJSONArray("baseColorFactor")
                        if (f != null && f.length() == 4) color = FloatArray(4) { k -> f.getDouble(k).toFloat() }
                        val bt = pbr.optJSONObject("baseColorTexture")
                        if (bt != null && textures != null) {
                            val src = textures.getJSONObject(bt.getInt("index")).optInt("source", -1)
                            if (src >= 0) tex = loadImage(src)
                        }
                    }
                }
                raws.add(RawPrim(pos, nrm, uv, idx, color, tex))
            }
        }

        fun localMatrix(n: JSONObject): FloatArray {
            val m = FloatArray(16)
            val arr = n.optJSONArray("matrix")
            if (arr != null && arr.length() == 16) {
                for (i in 0 until 16) m[i] = arr.getDouble(i).toFloat()
                return m
            }
            val tm = FloatArray(16); Matrix.setIdentityM(tm, 0)
            val rm = FloatArray(16); Matrix.setIdentityM(rm, 0)
            val sm = FloatArray(16); Matrix.setIdentityM(sm, 0)
            n.optJSONArray("translation")?.let {
                Matrix.translateM(tm, 0, it.getDouble(0).toFloat(), it.getDouble(1).toFloat(), it.getDouble(2).toFloat())
            }
            n.optJSONArray("scale")?.let {
                Matrix.scaleM(sm, 0, it.getDouble(0).toFloat(), it.getDouble(1).toFloat(), it.getDouble(2).toFloat())
            }
            n.optJSONArray("rotation")?.let {
                val x = it.getDouble(0).toFloat(); val y = it.getDouble(1).toFloat()
                val z = it.getDouble(2).toFloat(); val w = it.getDouble(3).toFloat()
                val xx = x * x; val yy = y * y; val zz = z * z
                rm[0] = 1 - 2 * (yy + zz); rm[1] = 2 * (x * y + w * z); rm[2] = 2 * (x * z - w * y)
                rm[4] = 2 * (x * y - w * z); rm[5] = 1 - 2 * (xx + zz); rm[6] = 2 * (y * z + w * x)
                rm[8] = 2 * (x * z + w * y); rm[9] = 2 * (y * z - w * x); rm[10] = 1 - 2 * (xx + yy)
            }
            val tr = FloatArray(16)
            Matrix.multiplyMM(tr, 0, tm, 0, rm, 0)
            Matrix.multiplyMM(m, 0, tr, 0, sm, 0)
            return m
        }

        fun visit(nodeIndex: Int, parent: FloatArray) {
            val n = nodes.getJSONObject(nodeIndex)
            val world = FloatArray(16)
            Matrix.multiplyMM(world, 0, parent, 0, localMatrix(n), 0)
            if (n.has("mesh")) addMesh(n.getInt("mesh"), world)
            val children = n.optJSONArray("children")
            if (children != null) for (i in 0 until children.length()) visit(children.getInt(i), world)
        }

        val identity = FloatArray(16); Matrix.setIdentityM(identity, 0)
        val sceneNodes = root.optJSONArray("scenes")?.optJSONObject(root.optInt("scene", 0))?.optJSONArray("nodes")
        if (sceneNodes != null) {
            for (i in 0 until sceneNodes.length()) visit(sceneNodes.getInt(i), identity)
        } else {
            for (i in 0 until nodes.length()) visit(i, identity)
        }
        check(raws.isNotEmpty()) { "GLB has no triangle meshes: $assetPath" }

        // Bounds -> bottom-center at origin, largest dimension = targetSizeMeters
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
        for (r in raws) {
            for (i in 0 until r.pos.size / 3) {
                minX = min(minX, r.pos[i * 3]); maxX = max(maxX, r.pos[i * 3])
                minY = min(minY, r.pos[i * 3 + 1]); maxY = max(maxY, r.pos[i * 3 + 1])
                minZ = min(minZ, r.pos[i * 3 + 2]); maxZ = max(maxZ, r.pos[i * 3 + 2])
            }
        }
        val rawW = maxX - minX
        val rawH = maxY - minY
        val rawD = maxZ - minZ
        // Scale so the model's HEIGHT (Y) equals the real height in meters.
        val scale = if (rawH > 1e-6f) targetSizeMeters / rawH else 1f
        val cx = (minX + maxX) / 2f
        val cz = (minZ + maxZ) / 2f

        val out = raws.map { r ->
            val count = r.pos.size / 3
            val v = FloatArray(count * 8)
            for (i in 0 until count) {
                v[i * 8] = (r.pos[i * 3] - cx) * scale
                v[i * 8 + 1] = (r.pos[i * 3 + 1] - minY) * scale
                v[i * 8 + 2] = (r.pos[i * 3 + 2] - cz) * scale
                v[i * 8 + 3] = r.nrm[i * 3]; v[i * 8 + 4] = r.nrm[i * 3 + 1]; v[i * 8 + 5] = r.nrm[i * 3 + 2]
                v[i * 8 + 6] = r.uv[i * 2]; v[i * 8 + 7] = r.uv[i * 2 + 1]
            }
            GlbPrimitive(v, r.idx, r.color, r.tex)
        }
        Log.i(
            GLB_TAG,
            "Parsed $assetPath: ${out.size} parts, raw size x=$rawW y=$rawH z=$rawD, " +
                    "scaled to meters: width=${rawW * scale} height=${rawH * scale} depth=${rawD * scale}"
        )
        return GlbData(out)
    }
}

private const val GLB_VERT = """#version 300 es
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec2 aUV;
uniform mat4 uMvp;
uniform mat4 uModel;
out vec3 vNormal;
out vec2 vUV;
void main() {
    gl_Position = uMvp * vec4(aPos, 1.0);
    vNormal = mat3(uModel) * aNormal;
    vUV = aUV;
}
"""

private const val GLB_FRAG = """#version 300 es
precision mediump float;
uniform vec4 uColor;
uniform sampler2D uTex;
uniform float uUseTex;
uniform vec3 uLightDir;
in vec3 vNormal;
in vec2 vUV;
out vec4 fragColor;
void main() {
    vec4 base = uColor;
    if (uUseTex > 0.5) { base *= texture(uTex, vUV); }
    float ndl = max(dot(normalize(vNormal), normalize(uLightDir)), 0.0);
    float light = 0.4 + 0.6 * ndl;
    fragColor = vec4(base.rgb * light, base.a);
}
"""

/** All methods must be called on the GL thread that owns the context. */
class GlbGlRenderer {
    private class Gpu(val vao: Int, val vbo: Int, val ibo: Int, val indexCount: Int, val color: FloatArray, val tex: Int)

    private var program = 0
    private var uMvp = -1
    private var uModel = -1
    private var uColor = -1
    private var uTex = -1
    private var uUseTex = -1
    private var uLight = -1
    private var context: EGLContext? = null
    private val gpu = ArrayList<Gpu>()

    private fun compile(type: Int, src: String): Int {
        val s = GLES30.glCreateShader(type)
        GLES30.glShaderSource(s, src)
        GLES30.glCompileShader(s)
        val ok = IntArray(1)
        GLES30.glGetShaderiv(s, GLES30.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) Log.e(GLB_TAG, "Shader error: ${GLES30.glGetShaderInfoLog(s)}")
        return s
    }

    fun init() {
        context = (EGLContext.getEGL() as EGL10).eglGetCurrentContext()
        val vs = compile(GLES30.GL_VERTEX_SHADER, GLB_VERT)
        val fs = compile(GLES30.GL_FRAGMENT_SHADER, GLB_FRAG)
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)
        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)
        uMvp = GLES30.glGetUniformLocation(program, "uMvp")
        uModel = GLES30.glGetUniformLocation(program, "uModel")
        uColor = GLES30.glGetUniformLocation(program, "uColor")
        uTex = GLES30.glGetUniformLocation(program, "uTex")
        uUseTex = GLES30.glGetUniformLocation(program, "uUseTex")
        uLight = GLES30.glGetUniformLocation(program, "uLightDir")
    }

    private fun floatBuf(a: FloatArray) =
        ByteBuffer.allocateDirect(a.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(a); position(0) }

    private fun intBuf(a: IntArray) =
        ByteBuffer.allocateDirect(a.size * 4).order(ByteOrder.nativeOrder()).asIntBuffer().apply { put(a); position(0) }

    fun upload(data: GlbData) {
        releaseGpu()
        for (p in data.primitives) {
            val vao = IntArray(1)
            GLES30.glGenVertexArrays(1, vao, 0)
            GLES30.glBindVertexArray(vao[0])
            val bufs = IntArray(2)
            GLES30.glGenBuffers(2, bufs, 0)
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufs[0])
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, p.vertices.size * 4, floatBuf(p.vertices), GLES30.GL_STATIC_DRAW)
            GLES30.glEnableVertexAttribArray(0)
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 32, 0)
            GLES30.glEnableVertexAttribArray(1)
            GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 32, 12)
            GLES30.glEnableVertexAttribArray(2)
            GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 32, 24)
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, bufs[1])
            GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, p.indices.size * 4, intBuf(p.indices), GLES30.GL_STATIC_DRAW)
            GLES30.glBindVertexArray(0)

            var tex = 0
            val bmp = p.texture
            if (bmp != null) {
                val t = IntArray(1)
                GLES30.glGenTextures(1, t, 0)
                tex = t[0]
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, tex)
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0)
                GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
                GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            }
            gpu.add(Gpu(vao[0], bufs[0], bufs[1], p.indices.size, p.baseColor, tex))
        }
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }

    fun draw(mvp: FloatArray, model: FloatArray) {
        if (program == 0 || gpu.isEmpty()) return
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glUseProgram(program)
        GLES30.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)
        GLES30.glUniformMatrix4fv(uModel, 1, false, model, 0)
        GLES30.glUniform3f(uLight, 0.3f, 0.8f, 0.5f)
        GLES30.glUniform1i(uTex, 0)
        for (g in gpu) {
            GLES30.glUniform4fv(uColor, 1, g.color, 0)
            if (g.tex != 0) {
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, g.tex)
                GLES30.glUniform1f(uUseTex, 1f)
            } else {
                GLES30.glUniform1f(uUseTex, 0f)
            }
            GLES30.glBindVertexArray(g.vao)
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, g.indexCount, GLES30.GL_UNSIGNED_INT, 0)
        }
        GLES30.glBindVertexArray(0)
    }

    private fun contextStillCurrent(): Boolean =
        (EGLContext.getEGL() as EGL10).eglGetCurrentContext() == context

    private fun releaseGpu() {
        if (contextStillCurrent()) {
            for (g in gpu) {
                GLES30.glDeleteVertexArrays(1, intArrayOf(g.vao), 0)
                GLES30.glDeleteBuffers(2, intArrayOf(g.vbo, g.ibo), 0)
                if (g.tex != 0) GLES30.glDeleteTextures(1, intArrayOf(g.tex), 0)
            }
        }
        gpu.clear()
    }

    /** The GL context was lost: forget handles without deleting them. */
    fun forget() {
        gpu.clear()
        program = 0
    }

    fun dispose() {
        releaseGpu()
        if (program != 0 && contextStillCurrent()) GLES30.glDeleteProgram(program)
        program = 0
    }
}
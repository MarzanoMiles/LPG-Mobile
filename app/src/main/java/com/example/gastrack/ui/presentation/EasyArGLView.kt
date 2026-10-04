package com.example.gastrack.ui.presentation

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import cn.easyar.Engine
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10

class EasyArGLView(context: Context, private val scene: EasyArScene) : GLSurfaceView(context) {

    private val lock = Any()
    private var finishing = false
    @Volatile private var initialized = false
    private var surfaceW = 1
    private var surfaceH = 1
    private var paused = true

    private val tapDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val w = this@EasyArGLView.width
            val h = this@EasyArGLView.height
            if (w > 0 && h > 0) scene.requestPlacement(e.x / w, e.y / h)
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scene.rotateBy(-distanceX * 0.4f)
            return true
        }
    })

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scene.scaleBy(detector.scaleFactor)
            return true
        }
    })

    init {
        setEGLContextFactory(ContextFactory())
        setEGLWindowSurfaceFactory(WindowSurfaceFactory())
        setEGLConfigChooser(ConfigChooser())
        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                if (!initialized) {
                    initialized = true
                    scene.initialize()
                } else {
                    scene.recreateContext()
                }
                scene.start()
            }

            override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
                surfaceW = w
                surfaceH = h
            }

            override fun onDrawFrame(gl: GL10?) {
                if (!initialized) return
                scene.render(surfaceW, surfaceH, screenRotationDegrees())
            }
        })
        setZOrderMediaOverlay(true)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        if (!scaleDetector.isInProgress) tapDetector.onTouchEvent(event)
        return true
    }

    private fun screenRotationDegrees(): Int {
        return when (display?.rotation ?: Surface.ROTATION_0) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    // ---- lifecycle, driven by Compose ----
    fun resumeView() {
        if (!paused) return
        paused = false
        onResume()
        Engine.onResume()
    }

    fun pauseView() {
        if (paused) return
        paused = true
        Engine.onPause()
        onPause()
    }

    /** The screen is leaving: release EasyAR once the surface goes away. */
    fun finishAndPause() {
        synchronized(lock) { finishing = true }
        if (paused) cleanupIfFinishing() else pauseView()
    }

    private fun cleanupIfFinishing() {
        val b = synchronized(lock) { finishing }
        if (initialized && b) {
            initialized = false
            scene.stop()
            scene.dispose()
        }
    }

    private class ContextFactory : EGLContextFactory {
        override fun createContext(egl: EGL10, display: EGLDisplay, eglConfig: EGLConfig): EGLContext {
            val attrib = intArrayOf(0x3098 /* EGL_CONTEXT_CLIENT_VERSION */, 3, EGL10.EGL_NONE)
            return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib)
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
            egl.eglDestroyContext(display, context)
        }
    }

    private inner class WindowSurfaceFactory : EGLWindowSurfaceFactory {
        override fun createWindowSurface(egl: EGL10, display: EGLDisplay, config: EGLConfig, nativeWindow: Any?): EGLSurface? {
            return try {
                egl.eglCreateWindowSurface(display, config, nativeWindow, null)
            } catch (e: IllegalArgumentException) {
                Log.e("GLSurfaceView", "eglCreateWindowSurface", e)
                null
            }
        }

        override fun destroySurface(egl: EGL10, display: EGLDisplay, surface: EGLSurface) {
            cleanupIfFinishing()
            egl.eglDestroySurface(display, surface)
        }
    }

    private class ConfigChooser : EGLConfigChooser {
        override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
            val eglOpenGlEs2Bit = 0x0004
            val attrib = intArrayOf(
                EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, eglOpenGlEs2Bit, EGL10.EGL_NONE
            )
            val numConfig = IntArray(1)
            egl.eglChooseConfig(display, attrib, null, 0, numConfig)
            val n = numConfig[0]
            require(n > 0) { "fail to choose EGL configs" }
            val configs = arrayOfNulls<EGLConfig>(n)
            egl.eglChooseConfig(display, attrib, configs, n, numConfig)
            for (config in configs) {
                if (config == null) continue
                val v = IntArray(1)
                var r = 0; var g = 0; var b = 0; var a = 0; var d = 0
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, v)) d = v[0]
                if (d < 16) continue
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, v)) r = v[0]
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, v)) g = v[0]
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, v)) b = v[0]
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, v)) a = v[0]
                if (r == 8 && g == 8 && b == 8 && a == 0) return config
            }
            return configs[0]!!
        }
    }
}
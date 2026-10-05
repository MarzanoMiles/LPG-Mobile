package com.example.gastrack.ui.presentation

/** What EasyArGLView needs from a scene. */
interface EasyArRenderer {
    fun initialize()
    fun recreateContext()
    fun start(): Boolean
    fun stop()
    fun dispose()
    fun render(width: Int, height: Int, screenRotation: Int)
    fun requestPlacement(nx: Float, ny: Float)
    fun rotateBy(deg: Float)
    fun scaleBy(f: Float)
}

/** Lets the existing motion-tracking scene be used through the interface, without editing it. */
class EasyArSceneAdapter(private val s: EasyArScene) : EasyArRenderer {
    override fun initialize() = s.initialize()
    override fun recreateContext() = s.recreateContext()
    override fun start(): Boolean = s.start()
    override fun stop() = s.stop()
    override fun dispose() = s.dispose()
    override fun render(width: Int, height: Int, screenRotation: Int) = s.render(width, height, screenRotation)
    override fun requestPlacement(nx: Float, ny: Float) = s.requestPlacement(nx, ny)
    override fun rotateBy(deg: Float) = s.rotateBy(deg)
    override fun scaleBy(f: Float) = s.scaleBy(f)
}
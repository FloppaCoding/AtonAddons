package atonaddons.shaders.uniforms.impl

import atonaddons.shaders.Shader
import atonaddons.shaders.uniforms.Uniform
import org.lwjgl.opengl.GL20

class Uniform1F(shader: Shader, name: String, private val source: () -> Float) : Uniform<Float>(shader, name) {

    override var lastValue: Float? = null

    override fun update() {
        val newVal = source()
        if (newVal != lastValue) {
            GL20.glUniform1f(this.uninformID, newVal)
            lastValue = newVal
        }
    }
}
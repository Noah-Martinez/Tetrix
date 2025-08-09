package ch.tetrix.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import net.mgsx.gltf.loaders.glb.GLBLoader

enum class ModelAssets(val path: String) {
    Cube("models/cube.glb");

    private var sharedModel: Model? = null

    fun load() {
        if (sharedModel != null) return
        val file: FileHandle = Gdx.files.internal(path)
        sharedModel = GLBLoader().load(file).scene.model
    }

    fun createInstance(): ModelInstance {
        load()
        return ModelInstance(sharedModel!!)
    }

    fun dispose() {
        sharedModel?.dispose()
        sharedModel = null
    }
}

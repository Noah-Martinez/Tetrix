package ch.tetrix.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.Vector3
import net.mgsx.gltf.loaders.glb.GLBLoader

/**
 * Manages the creation, movement, and rendering of cubes in the game.
 */
class CubeManager {
    // No boundaries for cube movement
    // List to store multiple cubes and their positions
    private var activeCubeIndex: Int? = null
    private val cubes = mutableListOf<ModelInstance>()
    private val cubePositions = mutableListOf<Vector3>()

    // Speed for falling and lateral movement
    private val fallingSpeed = 0.5f
    private val lateralSpeed = 0.5f

    // Model for cubes
    private val modelAsset = Gdx.files.internal("model/cube.glb")
    private val model = GLBLoader().load(modelAsset)

    fun spawnCube(position: Vector3) {
        val newCube = ModelInstance(model.scene.model)
        newCube.transform.setToTranslation(position)
        newCube.transform.scale(1f, 1f, 1f) // Start with a scale of 1x for visibility

        cubes.add(newCube)
        cubePositions.add(position)

        // Set the new cube as the active cube
        activeCubeIndex = cubes.size - 1
    }


    fun update(delta: Float) {

    }

    fun render(modelBatch: ModelBatch, environment: Environment) {
        for (cube in cubes) {
            modelBatch.render(cube, environment)
        }
    }

    /**
     * Moves the active cube to the left.
     */
    fun moveActiveLeft() {
        activeCubeIndex?.let { index ->
            val position = cubePositions[index]
            val newX = position.x - lateralSpeed
            position.x = newX
            cubes[index].transform.setToTranslation(position)
        }
    }

    /**
     * Moves the active cube to the right.
     */
    fun moveActiveRight() {
        activeCubeIndex?.let { index ->
            val position = cubePositions[index]
            val newX = position.x + lateralSpeed
            position.x = newX
            cubes[index].transform.setToTranslation(position)
        }
    }

    /**
     * Disposes of resources used by the cube manager.
     */
    fun dispose() {
        model.scene.model.dispose()
    }
}

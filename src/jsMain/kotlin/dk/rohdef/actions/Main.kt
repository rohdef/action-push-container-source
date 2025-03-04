package dk.rohdef.actions

import com.docker.actions_toolkit.lib.docker.Docker
import com.docker.actions_toolkit.lib.github.GitHub
import com.github.actions.Exec
import dk.rohdef.actions.github.Core
import dk.rohdef.actions.github.Inputs
import dk.rohdef.actions.github.OutputName
import kotlinx.coroutines.await
import node.process.Process
import node.process.process

suspend fun Core.actionInfo() {
    group("GitHub Actions runtime token ACs") {
        try {
            GitHub.printActionsRuntimeTokenACs().await()
        } catch (exception: Exception) {
            warning(exception.message ?: "Could not print token ACs")
        }
    }

    group("Docker info") {
        try {
            Docker.printVersion().await()
            Docker.printInfo().await()
        } catch (exception: Exception) {
            info(exception.message ?: "Could not get docker information")
        }
    }
}

class ActionEnvironment(
    val process: Process,
) {
    fun Process.getEnvironmentValue(key: String): String =
        process.env[key] ?: throw IllegalArgumentException("$key environment variable is not set")

    val ACTOR = "GITHUB_ACTOR"
    val COMMIT_SHA = "GITHUB_SHA"
    val REFERENCE = "GITHUB_REF"
    val REPOSITORY = "GITHUB_REPOSITORY"
    val RUN_ID = "GITHUB_RUN_ID"
    val SERVER_URL = "GITHUB_SERVER_URL"

    val actor = process.getEnvironmentValue(ACTOR)
    val commitSha = process.getEnvironmentValue(COMMIT_SHA)
    val reference = process.getEnvironmentValue(REFERENCE)
    val repository = process.getEnvironmentValue(REPOSITORY)
    val runId = process.getEnvironmentValue(RUN_ID)
    val serverUrl = process.getEnvironmentValue(SERVER_URL)

    val projectUrl = "$serverUrl/$repository"
}

suspend fun main() {
    Core().run(
        {
            actionInfo()

            val actionEnvironment = ActionEnvironment(process)

            info("Starting push workflow with the following input:\n$inputs")

            val autoTags: List<String> = when (inputs.autoTagging.strategy) {
                Inputs.AutoTagging.Strategy.TAGS_AS_RELEASE -> {
                    val referenceParts = actionEnvironment.reference.split("/")
                    val lastReferencePart = referenceParts.last()

                    val version = when {
                        referenceParts.subList(0, 2) == listOf("refs", "tags") -> lastReferencePart.replace("^v".toRegex(), "")
                        lastReferencePart == "main" -> "latest"
                        else -> lastReferencePart
                    }

                    listOf(version)
                }
                Inputs.AutoTagging.Strategy.DISABLED -> emptyList()
            }

            val tags = autoTags + inputs.destinationTags.tags

            val imageIds = inputs.destinationImageNames.names.flatMap { image ->
                tags.map { tag -> "$image:$tag" }
            }

            info("The following images with tags will be pushed: ${imageIds.joinToString(", ")}")
            val imageIdsToPush = mutableListOf<String>()
            inputs.destinationHosts.hosts.forEach { host ->
                info("\tTagging image IDs for host: [$host]")

                imageIds.forEach { imageAndTag ->
                    val imageId = "$host/$imageAndTag"

                    val tagExecution = Exec.getExecOutput(
                        "docker",
                        arrayOf("tag", inputs.imageId.value, imageId),
                    ).await()

                    if (tagExecution.exitCode != 0) {
                        error("Could not add tag to image")
                        error("    image: ${inputs.imageId.value}")
                        error("    tag: ${imageId}")
                        setFailed("Could not add tag [$imageId] to image [${inputs.imageId.value}]'")
                    }

                    imageIdsToPush += imageId
                }
            }

            val imagesPushed =  mutableListOf<String>()
            imageIdsToPush.forEach { imageId ->
                info("""docker push "$imageId" """)
                imagesPushed += imageId
            }
            setOutput(OutputName.IMAGES_PUSHED, imagesPushed.map { "- $it" }.joinToString("\n"))

            // TODO when no digest, maybe not fail? Or perhaps make it optional to fail?
            val imageDigestOutput = Exec.getExecOutput(
                "docker",
                arrayOf("inspect", "--format", "{{index .RepoDigests 0}}", inputs.imageId.value)
            ).await()
            when (imageDigestOutput.exitCode) {
                0 -> {
                    val digest = imageDigestOutput.stdout
                    info("Digest: [$digest]")
                    setOutput(OutputName.DIGEST, digest)
                }

                else -> setFailed("Could not get docker image digest")
            }
        },
    )
}
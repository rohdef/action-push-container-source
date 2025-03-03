package dk.rohdef.actions

import com.docker.actions_toolkit.lib.docker.Docker
import com.docker.actions_toolkit.lib.github.GitHub
import com.github.actions.Exec
import dk.rohdef.actions.github.Core
import dk.rohdef.actions.github.Inputs
import kotlinx.coroutines.await
import node.process.Process
import node.process.process
import kotlin.uuid.ExperimentalUuidApi

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
    val REPOSITORY = "GITHUB_REPOSITORY"
    val RUN_ID = "GITHUB_RUN_ID"
    val SERVER_URL = "GITHUB_SERVER_URL"

    val actor = process.getEnvironmentValue(ACTOR)
    val commitSha = process.getEnvironmentValue(COMMIT_SHA)
    val repository = process.getEnvironmentValue(REPOSITORY)
    val runId = process.getEnvironmentValue(RUN_ID)
    val serverUrl = process.getEnvironmentValue(SERVER_URL)

    val projectUrl = "$serverUrl/$repository"
}

@OptIn(ExperimentalUuidApi::class)
suspend fun main() {
    Core().run(
        {
            actionInfo()

            val actionEnvironment = ActionEnvironment(process)

            val autoTags: List<String> = when (inputs.autoTagging.strategy) {
                Inputs.AutoTagging.Strategy.TAGS_AS_RELEASE -> listOf()
                Inputs.AutoTagging.Strategy.DISABLED -> emptyList()
            }

            val tags = autoTags + inputs.destinationTags.tags

            val taggedImages = inputs.destinationImageNames.names.flatMap { image ->
                tags.map { tag -> "$image:$tag" }
            }

            info("The following images with tags will be pushed: ${taggedImages.joinToString(", ")}")
            inputs.destinationHosts.hosts.forEach { host ->
                info("\tPusing images with tags to [$host]")

                taggedImages.forEach { imageAndTag ->
                    val destination = "$host/$imageAndTag"

                    info("docker tag \\")
                    info("""\t"${inputs.imageId}" \""")
                    info("""\t"$destination" """)
                    info("""docker push "$destination""")                       
                }
            }

            // TODO when no digest, maybe not fail? Or perhaps make it optional to fail?
            val imageDigestOutput = Exec.getExecOutput(
                "docker",
                arrayOf("inspect", "--format", "{{index .RepoDigests 0}}", inputs.imageId.value)
            ).await()
            when (imageDigestOutput.exitCode) {
                0 -> {
                    val digest = imageDigestOutput.stdout
                    info("Digest: [$digest]")
                    setOutput("digest", digest)
                }

                else -> setFailed("Could not get docker image digest")
            }
        },
    )
}
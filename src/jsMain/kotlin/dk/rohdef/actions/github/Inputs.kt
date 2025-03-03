package dk.rohdef.actions.github

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString

data class Inputs(
    val imageId: ImageId,
    val destinationHosts: DestinationHosts,
    val destinationImageNames: DestinationImageNames,
    val autoTagging: AutoTagging,
    val destinationTags: DestinationTags,
) {
    value class ImageId private constructor(
        val value: String,
    ) {
        companion object {
            fun fromValue(value: String) : ImageId {
                return ImageId(value)
            }
        }
    }

    value class DestinationHosts private constructor(
        val hosts: List<String>,
    ) {
        companion object {
            fun fromValue(value: String) : DestinationHosts {
                val hosts: List<String> = Yaml.default.decodeFromString(value)
                return DestinationHosts(hosts)
            }
        }
    }

    value class DestinationImageNames private constructor(
        val names: List<String>,
    ) {
        companion object {
            fun fromValue(value: String) : DestinationImageNames {
                val names: List<String> = Yaml.default.decodeFromString(value)
                return DestinationImageNames(names)
            }
        }
    }

    value class AutoTagging private constructor(
        val strategy: Strategy,
    ) {
        companion object {
            fun fromValue(value: String) : AutoTagging {
                val strategy = Strategy.valueOf(value)
                return AutoTagging(strategy)
            }
        }

        enum class Strategy {
            TAGS_AS_RELEASE,
            DISABLED,
        }
    }

    value class DestinationTags private constructor(
        val tags: List<String>,
    ) {
        companion object {
            fun fromValue(value: String) : DestinationTags {
                val tags: List<String> = Yaml.default.decodeFromString(value)
                return DestinationTags(tags)
            }
        }
    }

    companion object {
        fun fromInput(getInput: (String) -> String): Inputs {
            return Inputs(
                ImageId.fromValue(getInput(InputNames.imageId)),
                DestinationHosts.fromValue(getInput(InputNames.destinationHosts)),
                DestinationImageNames.fromValue(getInput(InputNames.destinationImageNames)),
                AutoTagging.fromValue(getInput(InputNames.autoTagging)),
                DestinationTags.fromValue(getInput(InputNames.destinationTags)),
            )
        }
    }
}

object InputNames {
    val imageId = "imageid"
    val destinationHosts = "destinationHosts"
    val destinationImageNames = "destinationImageNames"
    val autoTagging = "autoTagging"
    val destinationTags = "destinationTags"
}
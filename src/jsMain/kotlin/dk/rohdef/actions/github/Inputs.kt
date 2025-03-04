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
                println(value)
                return ImageId(value)
            }
        }
    }

    value class DestinationHosts private constructor(
        val hosts: List<String>,
    ) {
        companion object {
            fun fromValue(value: String) : DestinationHosts {
                println(value)
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
                println(value)
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
                println(value)
                val strategy = Strategy.valueOf(value.uppercase())
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
                println(value)
                val tags: List<String> = Yaml.default.decodeFromString(value)
                return DestinationTags(tags)
            }
        }
    }

    companion object {
        fun fromInput(getInput: (InputName) -> String): Inputs {
            return Inputs(
                ImageId.fromValue(getInput(InputName.IMAGE_ID)),
                DestinationHosts.fromValue(getInput(InputName.DESTINATION_HOSTS)),
                DestinationImageNames.fromValue(getInput(InputName.DESTINATION_IMAGE_NAMES)),
                AutoTagging.fromValue(getInput(InputName.AUTO_TAGGING)),
                DestinationTags.fromValue(getInput(InputName.DESTINATION_TAGS)),
            )
        }
    }
}
package dk.rohdef.actions.github

import com.charleskorn.kaml.IncorrectTypeException
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
            private val inputName = InputName.IMAGE_ID

            fun fromValue(getInput: (InputName) -> String) = fromValue(getInput(inputName))

            fun fromValue(value: String) = ImageId(value)
        }
    }

    value class DestinationHosts private constructor(
        val hosts: List<String>,
    ) {
        companion object {
            private val inputName = InputName.DESTINATION_HOSTS

            fun fromValue(getInput: (InputName) -> String) = fromValue(getInput(inputName))

            fun fromValue(value: String) = DestinationHosts(value.parseYamlList(inputName))
        }
    }

    value class DestinationImageNames private constructor(
        val names: List<String>,
    ) {
        companion object {
            private val inputName = InputName.DESTINATION_IMAGE_NAMES

            fun fromValue(getInput: (InputName) -> String) = fromValue(getInput(inputName))

            fun fromValue(value: String) = DestinationImageNames(value.parseYamlList(inputName))
        }
    }

    value class AutoTagging private constructor(
        val strategy: Strategy,
    ) {
        companion object {
            private val inputName = InputName.AUTO_TAGGING

            fun fromValue(getInput: (InputName) -> String) = fromValue(getInput(inputName))

            fun fromValue(value: String) = AutoTagging(value.parseEnumInput(inputName))
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
            private val inputName = InputName.DESTINATION_TAGS

            fun fromValue(getInput: (InputName) -> String) = fromValue(getInput(inputName))

            fun fromValue(value: String) = DestinationTags(value.parseYamlList(inputName))
        }
    }

    companion object {
        fun fromInput(getInput: (InputName) -> String): Inputs {
            return Inputs(
                ImageId.fromValue(getInput),
                DestinationHosts.fromValue(getInput),
                DestinationImageNames.fromValue(getInput),
                AutoTagging.fromValue(getInput),
                DestinationTags.fromValue(getInput),
            )
        }
    }
}

fun Throwable.asParsingError(message: String, defaultErrorContent: String) : Nothing {
    throw IllegalArgumentException(
        """
$message

$defaultErrorContent
        """,
        this,
    )
}

private inline fun <reified T : Enum<T>> String.parseEnumInput(inputName: InputName): T {
    val values = enumValues<T>().toList()

    val defaultErrorContent = """
Attempted to parse: ${inputName.actionName}

The input was:
--- BEGIN INPUT ---
$this
--- END  INPUT ---

Make that the input is an enum of the type ${T::class.simpleName} e.g.,:

${inputName.actionName}: ${values.firstOrNull()}

Valid values are: ${values.joinToString(", ")}
        """

    try {
        return enumValueOf<T>(this.uppercase())
    } catch (exception: IllegalArgumentException) {
        exception.asParsingError(
            "Could not parse to enum: ${exception.message}",
            defaultErrorContent,
        )
    } catch (exception: Exception) {
        exception.asParsingError(
            "Unknown error occured when parsing.",
            defaultErrorContent,
        )
    }
}

private inline fun <reified T> String.parseYamlList(inputName: InputName): List<T> {
    val defaultErrorContent = """
Attempted to parse: ${inputName.actionName}

The input was:
--- BEGIN INPUT ---
$this
--- END  INPUT ---

Make that the input is a string containing a yaml list of strings, e.g.,:

${inputName.actionName}: |
    - foo
    - bar
    - baz
        """

    try {
        return Yaml.default.decodeFromString(this)
    } catch (exception: IncorrectTypeException) {
        exception.asParsingError(
            "Got a YAML parsing error on the type.",
            defaultErrorContent,
        )
    } catch (exception: Throwable) {
        exception.asParsingError(
            "Unknown error occured when parsing.",
            defaultErrorContent,
        )
    }
}
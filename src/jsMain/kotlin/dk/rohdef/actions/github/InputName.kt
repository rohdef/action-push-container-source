package dk.rohdef.actions.github

enum class InputName(val actionName: String) {
    IMAGE_ID("image-id"),
    DESTINATION_HOSTS("destination-hosts"),
    DESTINATION_IMAGE_NAMES("destination-image-names"),
    AUTO_TAGGING("auto-tagging"),
    DESTINATION_TAGS("destination-tags"),
}
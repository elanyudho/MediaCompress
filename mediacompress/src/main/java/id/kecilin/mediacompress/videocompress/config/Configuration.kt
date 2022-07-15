package id.kecilin.mediacompress.videocompress.config

import id.kecilin.mediacompress.videocompress.VideoQuality

data class Configuration(
    var quality: VideoQuality = VideoQuality.MEDIUM,
    var isMinBitrateCheckEnabled: Boolean = true,
    var videoBitrate: Int? = null,
    var disableAudio: Boolean = false,
    val keepOriginalResolution: Boolean = false,
    var videoHeight: Double? = null,
    var videoWidth: Double? = null,
)

data class StorageConfiguration(
    var fileName: String? = null,
    var saveAt: String? = null,
    var isExternal: Boolean = true,
)
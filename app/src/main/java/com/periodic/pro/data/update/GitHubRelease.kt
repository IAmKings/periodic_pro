package com.periodic.pro.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    val name: String,
    @SerialName("html_url")
    val htmlUrl: String,
    val body: String,
    val prerelease: Boolean,
    val assets: List<Asset>,
)

@Serializable
data class Asset(
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    val size: Long,
)

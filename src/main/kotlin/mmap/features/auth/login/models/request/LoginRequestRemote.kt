package mmap.features.auth.login.models.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestRemote(
    val userId: String,
    val pinToken: String,
)
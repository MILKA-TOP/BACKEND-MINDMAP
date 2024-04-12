package mmap.features.auth.login.models.request

import kotlinx.serialization.Serializable

@Serializable
data class EnterDataReceiveRemote(
    val email: String,
    val password: String
)

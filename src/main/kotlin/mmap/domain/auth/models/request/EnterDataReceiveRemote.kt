package mmap.domain.auth.models.request

import kotlinx.serialization.Serializable

@Serializable
data class EnterDataReceiveRemote(
    val email: String,
    val password: String
)

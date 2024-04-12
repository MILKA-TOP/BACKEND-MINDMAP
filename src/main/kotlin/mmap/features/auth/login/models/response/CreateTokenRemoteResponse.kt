package mmap.features.auth.login.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateTokenRemoteResponse(
    val token: String
)

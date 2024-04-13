package mmap.domain.auth.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CreateTokenRemoteResponse(
    val token: String
)

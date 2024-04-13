package mmap.domain.auth.models.response

import kotlinx.serialization.Serializable

@Serializable
class SessionResponseRemote(
    val sessionId: String,
    val userId: String,
    val userEmail: String,
)

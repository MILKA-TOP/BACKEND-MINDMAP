package mmap.features.auth

import kotlinx.serialization.Serializable

@Serializable
class SessionResponseRemote(
    val sessionId: String,
    val userId: String,
    val userEmail: String,
)
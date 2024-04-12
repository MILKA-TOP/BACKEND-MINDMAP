package mmap.database.sessions

import java.util.UUID

class SessionDTO(
    val sessionId: UUID,
    val userId: Int,
    val deviceId: String,
    val tokenSalt: String? = null,
)
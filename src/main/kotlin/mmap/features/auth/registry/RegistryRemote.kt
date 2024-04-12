package mmap.features.auth.registry

import kotlinx.serialization.Serializable

@Serializable
data class RegistryReceiveRemote(
    val email: String,
    val password: String
)


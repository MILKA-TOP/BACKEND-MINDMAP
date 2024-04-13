package mmap.database.extensions

data class UpdateRowDTO<T, K>(
    val insert: List<T> = emptyList(),
    val remove: List<K> = emptyList(),
    val update: List<T> = emptyList(),
)

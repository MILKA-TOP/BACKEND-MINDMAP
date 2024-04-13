package mmap.extensions

import java.security.MessageDigest

fun String.salt() = MessageDigest
    .getInstance("SHA-256")
    .digest(this.toByteArray())
    .fold("") { str, it -> str + "%02x".format(it) }

fun String.md5() = MessageDigest
    .getInstance("MD5")
    .digest(this.toByteArray())
    .fold("") { str, it -> str + "%02x".format(it) }

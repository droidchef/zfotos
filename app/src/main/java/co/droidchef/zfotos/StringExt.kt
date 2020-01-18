package co.droidchef.zfotos

import java.math.BigInteger
import java.security.MessageDigest

fun String.md5(): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    return BigInteger(1, messageDigest.digest(toByteArray())).toString(16).padStart(32, '0')
}
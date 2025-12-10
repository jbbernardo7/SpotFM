package pt.ismai.lastfmlogin.utils

import java.security.MessageDigest

object CryptUtils {

    fun generateApiSig(params: Map<String, String>, secret: String): String {
        // 1. Sort parameters alphabetically by key
        val sortedParams = params.toSortedMap()

        // 2. Concatenate key + value
        val sb = StringBuilder()
        for ((key, value) in sortedParams) {
            sb.append(key).append(value)
        }

        // 3. Append the secret
        sb.append(secret)

        // 4. Create MD5 hash
        return md5(sb.toString())
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
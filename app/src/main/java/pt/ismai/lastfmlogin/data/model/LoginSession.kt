package pt.ismai.lastfmlogin.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("session") val session: Session? = null,
    @SerializedName("error") val error: Int? = null,
    @SerializedName("message") val message: String? = null
)

data class Session(
    @SerializedName("name") val name: String,
    @SerializedName("key") val key: String,
    @SerializedName("subscriber") val subscriber: Int
)

data class LastFmError(
    val message: String?,
    val error: Int?
)

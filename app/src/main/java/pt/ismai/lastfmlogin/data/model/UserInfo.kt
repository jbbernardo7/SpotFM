package pt.ismai.lastfmlogin.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// LastFm User

data class UserInfoResponse(
    @SerializedName("user") val user: LastFmUser
)

data class LastFmUser(
    @SerializedName("name") val name: String,
    @SerializedName("realname") val realName: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("playcount") val playcount: String?,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class LastFmImage(
    @SerializedName("#text") val url: String,
    @SerializedName("size") val size: String
)

// Database User

@Serializable
data class UserProfile(
    val username: String, // Primary Key
    val real_name: String?,
    val country: String?,
    val playcount: Int,
    val image_url: String?,

    val is_visible_on_map: Boolean = false, // Defaults to false (Ghost Mode)
    val last_active_at: Long? = null,       // Unix Timestamp of last activity
    val latitude: Double? = null,           // Nullable because user might not have granted permissions yet
    val longitude: Double? = null
)
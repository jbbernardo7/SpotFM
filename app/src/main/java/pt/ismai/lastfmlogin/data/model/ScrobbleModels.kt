package pt.ismai.lastfmlogin.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// --- 1. Supabase Database Entity ---
@Serializable
data class Scrobble(
    val username: String,
    val track_name: String,
    val artist_name: String,
    val album_image: String?,
    val date_uts: Long
)

// --- 2. Last.fm API Response Parsers ---
data class RecentTracksResponse(
    @SerializedName("recenttracks") val recentTracks: RecentTracksContainer
)

data class RecentTracksContainer(
    @SerializedName("track") val tracks: List<LastFmTrack>?
)

data class LastFmTrack(
    @SerializedName("name") val name: String,
    @SerializedName("artist") val artist: LastFmArtist,
    @SerializedName("image") val images: List<LastFmImage>?,
    @SerializedName("date") val date: LastFmDate?, // Null if "Now Playing"
    @SerializedName("@attr") val attributes: LastFmAttributes? // Contains "nowplaying"
)

data class LastFmArtist(
    @SerializedName("#text") val name: String
)

data class LastFmDate(
    @SerializedName("uts") val uts: String
)

data class LastFmAttributes(
    @SerializedName("nowplaying") val nowPlaying: String?
)
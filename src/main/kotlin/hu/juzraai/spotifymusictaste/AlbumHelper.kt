package hu.juzraai.spotifymusictaste

import com.wrapper.spotify.models.*

/**
 * Helper functions to Spotify albums.
 *
 * @author Zsolt Jurányi
 */
class AlbumHelper {

	fun releaseYear(album: Album): Int = album.releaseDate
			.replace(Regex("-.*"), "")
			.replace(Regex("^00"), "20") // e.g. 0013-10-08 (Korn - The Paradigm Shift)
			.toInt()
}
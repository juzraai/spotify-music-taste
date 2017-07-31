package hu.juzraai.spotifymusictaste

import com.wrapper.spotify.models.*

/**
 * Aggregates various features of tracks, albums and artists.
 *
 * @author Zsolt Jurányi
 */
class Aggregator {

	val countByArtist = mutableMapOf<String, Int>()
	val countByArtistDebutDecade = mutableMapOf<String, Int>()
	val countByArtistDebutYear = mutableMapOf<Int, Int>()
	val countByDurationMinute = mutableMapOf<String, Int>()
	val countByGenre = mutableMapOf<String, Int>()
	val countByGenreWord = mutableMapOf<String, Int>()
	val countByReleaseDecade = mutableMapOf<String, Int>()
	val countByReleaseYear = mutableMapOf<Int, Int>()
	var countOfTrack = 0
	var maxOfDurationSecond = 0
	var minOfDurationSecond = Int.MAX_VALUE
	var sumOfDurationSecond = 0

	fun addAlbum(album: Album) {
		val year = AlbumHelper().releaseYear(album)
		val decade = year - (year % 10)
		val decadeStr = "${decade}s"
		inc(countByReleaseDecade, decadeStr)
		inc(countByReleaseYear, year)
	}

	fun addArtist(artist: Artist) {
		inc(countByArtist, artist.name)
		artist.genres.forEach { genre ->
			inc(countByGenre, genre)
			genre.split(' ').forEach { inc(countByGenreWord, it) }
		}
	}

	fun addArtistDebutYear(debutYear: Int) {
		val debutDecade = debutYear - (debutYear % 10)
		val debutDecadeStr = "${debutDecade}s"
		inc(countByArtistDebutDecade, debutDecadeStr)
		inc(countByArtistDebutYear, debutYear)
	}

	fun addArtists(artists: List<Artist>) {
		artists.forEach(this::addArtist)
	}

	fun addTrack(track: Track) {
		++countOfTrack
		val durationSec = track.duration / 1000
		maxOfDurationSecond = Math.max(maxOfDurationSecond, durationSec)
		minOfDurationSecond = Math.min(minOfDurationSecond, durationSec)
		sumOfDurationSecond += durationSec
		val durationMin = durationSec / 60
		val durationMinStr = "$durationMin-${durationMin + 1} min. length"
		inc(countByDurationMinute, durationMinStr)
	}

	fun avgDurationSec(): Double {
		return sumOfDurationSecond.toDouble() / countOfTrack.toDouble()
	}

	fun basicStats(): Map<String, Number> {
		val map = mutableMapOf<String, Number>()
		map.put("Artist count", countByArtist.size)
		map.put("Genre count", countByGenre.size)
		map.put("Track count", countOfTrack)

		map.put("Average track duration in seconds", avgDurationSec())
		map.put("Longest track's duration in seconds", maxOfDurationSecond)
		map.put("Shortest track's duration in seconds", minOfDurationSecond)

		map.put("Oldest track's year", countByReleaseYear.keys.min()!!)
		map.put("Youngest track's year", countByReleaseYear.keys.max()!!)
		map.put("Oldest artist's debut year", countByArtistDebutYear.keys.min()!!)
		map.put("Youngest artist's debut year", countByArtistDebutYear.keys.max()!!)

		return map
	}

	private fun <T> inc(map: MutableMap<T, Int>, key: T) {
		map[key] = (map[key] ?: 0) + 1
	}
}
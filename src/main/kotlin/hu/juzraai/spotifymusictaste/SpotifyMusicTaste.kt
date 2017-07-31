package hu.juzraai.spotifymusictaste

import mu.*
import java.io.*

/**
 * The main features of the application at a high level:
 * read input file, analyze on-the-fly, export results.
 *
 * @author Zsolt Jurányi
 */
class SpotifyMusicTaste(val config: Config) : AutoCloseable {

	companion object : KLogging()

	private val spotify = Spotify(config.clientId!!, config.clientSecret!!)
	var stats = Aggregator()

	fun addTrackId(_trackId: String) {
		val trackId = _trackId.replace(Regex(".*[/:]"), "")

		logger.trace("Adding track ID to analysis: {}", trackId)

		val track = spotify.track(trackId)
		stats.addTrack(track)

		val album = spotify.album(track.album.id)
		stats.addAlbum(album)

		val artists = track.artists.map(spotify::artist).toList()
		artists.forEach { artist ->
			stats.addArtist(artist)
			stats.addArtistDebutYear(spotify.artistDebut(artist.id))
		}
	}

	fun addTrackIdsFromFile(filename: String) {
		logger.info("Adding tracks from file: {}", filename)
		TrackIdReader(File(filename)).use(this::addTrackId)
	}

	fun addTrackIdsFromResourceFile(filename: String) {
		logger.info("Adding tracks from resource file: {}", filename)
		addTrackIdsFromStream(SpotifyMusicTaste::class.java.classLoader.getResourceAsStream(filename))
	}

	fun addTrackIdsFromStream(inputStream: InputStream) {
		logger.info("Adding tracks from stream")
		TrackIdReader(inputStream).use(this::addTrackId)
	}

	fun clear() {
		stats = Aggregator()
	}

	override fun close() {
		spotify.close()
	}

	fun exportResults() {
		logger.info("Exporting results")
		val e = Exporter(config.outputDirectory)
		with(stats) {
			e.csv(
					basicStats(), "basic-stats.csv",
					"Statistic", "Value", false
			)
			e.csv(
					countByArtist, "artists.csv",
					"Artist", "Track count", true
			)
			e.csv(
					countByArtistDebutDecade, "artist-debut-decades.csv",
					"Artist debut decade", "Track count", true
			)
			e.csv(
					countByArtistDebutYear, "artist-debut-years.csv",
					"Artist debut year", "Track count", true
			)
			e.csv(
					countByDurationMinute, "duration-minutes.csv",
					"Duration in minutes", "Track count", true
			)
			e.csv(
					countByGenre, "genres.csv",
					"Genre", "Track count", true
			)
			e.csv(
					countByGenreWord, "genre-wordss.csv",
					"Genre word", "Track count", true
			)
			e.csv(
					countByReleaseDecade, "decades.csv",
					"Decade", "Track count", true
			)
			e.csv(
					countByReleaseYear, "years.csv",
					"Year", "Track count", true
			)

			val wccs = mutableListOf<WordCloudConfig>()
			wccs.add(e.wordCloudConfig(countByArtist, "artists.png", null))
			wccs.add(e.wordCloudConfig(countByGenre, "genres.png", null))
			wccs.add(e.wordCloudConfig(countByGenreWord, "genre-words.png", null))
			/*wccs.add(e.wordCloudConfig(countByGenre, "genre-bipolar.png") {
				it.key.contains(Regex("grunge|punk|rock|metal"))
			})*/
			e.wordCloud(*wccs.toTypedArray())
		}
	}

	fun useAndClose(action: (SpotifyMusicTaste) -> Unit) {
		action(this)
		close()
	}
}
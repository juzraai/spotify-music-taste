package hu.juzraai.spotifymusictaste

import mu.*
import java.io.*
import java.util.*

/**
 * Helps processing streams where each line is a Spotify track ID or URL.
 *
 * @author Zsolt Jurányi
 */
class TrackIdReader(val inputStream: InputStream) {

	companion object : KLogging()

	constructor(file: File) : this(FileInputStream(file))

	fun use(processTrackId: (String) -> Unit) {
		Scanner(inputStream).use {
			while (it.hasNextLine()) {
				processTrackId(it.nextLine().replace(Regex(".*/"), ""))
			}
		}
	}
}
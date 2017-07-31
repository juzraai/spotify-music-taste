package hu.juzraai.spotifymusictaste

import com.beust.jcommander.*

/**
 * Configuration model for SpotifyMusicTaste class.
 *
 * @author Zsolt Jurányi
 */
data class Config(

		@Parameter(
				names = arrayOf("-ci", "--client-id"),
				description = "Client ID for Spotify Web API",
				required = true
		)
		var clientId: String? = null,

		@Parameter(
				names = arrayOf("-cs", "--client-secret"),
				description = "Client secret for Spotify Web API",
				required = true
		)
		var clientSecret: String? = null,

		@Parameter(
				names = arrayOf("-i", "--input-file"),
				description = "Path to the input file. It must contain one track ID/URL per line.",
				required = true
		)
		var inputFile: String? = null,

		@Parameter(
				names = arrayOf("-o", "--output-dir"),
				description = "Path to the directory where to put analysis results (CSVs, word clouds, etc). Program will create directory if necessary.")
		var outputDirectory: String = "output"
)
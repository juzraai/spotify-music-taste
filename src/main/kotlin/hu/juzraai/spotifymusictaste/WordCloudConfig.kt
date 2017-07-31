package hu.juzraai.spotifymusictaste

/**
 * Configuration model for word cloud generation used by Exporter.
 *
 * @author Zsolt Jurányi
 */
data class WordCloudConfig(
		val filename: String,
		val frequencyMap: Map<String, Int>,
		val frequencyMap2: Map<String, Int>?
)
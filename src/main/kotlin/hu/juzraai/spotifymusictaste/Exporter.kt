package hu.juzraai.spotifymusictaste

import mu.*
import java.io.*
import java.nio.charset.*

/**
 * CSV export and word cloud generation functions.
 *
 * @author Zsolt Jurányi
 */
class Exporter(val outputDir: String) {

	companion object : KLogging() {
		val DELIMITER = "\t"
	}

	fun <T : Comparable<T>> csv(
			map: Map<T, Number>,
			filename: String,
			keyColumn: String,
			valueColumn: String,
			sortByValue: Boolean) {

		val sortedMap = if (sortByValue) sortMapByValue(map) else sortMapByKey(map)

		File(outputDir).let { if (!it.exists()) it.mkdirs() }
		val outputFile = File(outputDir, filename)
		logger.debug("Exporting CSV: {}", outputFile.absolutePath)
		val outputStream = FileOutputStream(outputFile)
		val outputStreamWriter = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)

		BufferedWriter(outputStreamWriter).use { w ->

			w.write(keyColumn)
			w.write(DELIMITER)
			w.write(valueColumn)
			w.newLine()

			sortedMap.forEach { e ->
				w.write(e.first.toString())
				w.write(DELIMITER)
				w.write(e.second.toString())
				w.newLine()
			}
		}
	}

	private fun <T : Comparable<T>> stdout(
			map: Map<T, Int>,
			header: String,
			sortByValue: Boolean) {

		val sortedMap = if (sortByValue) sortMapByValue(map) else sortMapByKey(map)

		println(header)
		println(String.format("%${header.length}s", "").replace(' ', '='))
		sortedMap.forEach { (item, count) ->
			println(String.format("  %4dx  %s", count, item))
		}

		println()
	}

	fun wordCloudConfig(
			frequencyMap: Map<String, Int>,
			filename: String,
			divider: ((Map.Entry<String, Int>) -> Boolean)?): WordCloudConfig {

		if (null == divider) {
			return WordCloudConfig(filename, frequencyMap, null)
		} else {
			val fm1 = mutableMapOf<String, Int>()
			val fm2 = mutableMapOf<String, Int>()
			frequencyMap.forEach { p ->
				if (divider(p)) {
					fm1.put(p.key, p.value)
				} else {
					fm2.put(p.key, p.value)
				}
			}
			return WordCloudConfig(filename, fm1, fm2)
		}
	}

	fun wordCloud(vararg wordCloudConfigs: WordCloudConfig) {
		logger.info("Generating {} word clouds", wordCloudConfigs.size)
		File(outputDir).let { if (!it.exists()) it.mkdirs() }

		val threads = mutableListOf<Thread>()
		val wcg = WordCloudGenerator()
		wordCloudConfigs.forEach { c ->
			val f = "$outputDir/${c.filename}"
			if (null == c.frequencyMap2) {
				threads.add(Thread {
					wcg.generate(c.frequencyMap, f)
				})
			} else {
				threads.add(Thread {
					wcg.generate(c.frequencyMap, c.frequencyMap2, f)
				})
			}
		}

		var t = -System.currentTimeMillis()
		threads.forEach(Thread::start)
		threads.forEach(Thread::join)
		t += System.currentTimeMillis()
		logger.debug("Generation time: {} sec", t / 1000)
	}

	private fun <T : Comparable<T>> sortMapByKey(map: Map<T, Number>): Collection<Pair<T, Number>> {
		return map.toList().sortedWith(compareBy({ it.first }))
	}

	private fun <T : Comparable<T>> sortMapByValue(map: Map<T, Number>): Collection<Pair<T, Number>> {
		return map.toList().sortedWith(compareBy({ -it.second.toDouble() }, { it.first }))
	}
}
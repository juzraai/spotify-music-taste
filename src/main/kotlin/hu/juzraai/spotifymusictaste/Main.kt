package hu.juzraai.spotifymusictaste

import com.beust.jcommander.*
import hu.juzraai.toolbox.log.*
import org.apache.log4j.*
import kotlin.system.*

/**
 * @author Zsolt Jurányi
 */
fun main(args: Array<String>) {
	val config = Config()
	val jcommander = JCommander.newBuilder().addObject(config).build()
	try {
		jcommander.parse(*args)
	} catch(e: Exception) {
		println(e.message + '\n')
		jcommander.usage()
		exitProcess(1)
	}

	LoggerSetup.outputOnlyToConsole()
	LoggerSetup.level(Level.OFF)
	LoggerSetup.level("hu.juzraai.spotifymusictaste", Level.INFO)

	SpotifyMusicTaste(config).useAndClose {
		it.addTrackIdsFromFile(config.inputFile!!)
		it.exportResults()
	}
}

package hu.juzraai.spotifymusictaste

import com.google.gson.*
import com.j256.ormlite.dao.*
import com.j256.ormlite.field.*
import com.j256.ormlite.table.*
import hu.juzraai.toolbox.data.*
import hu.juzraai.toolbox.jdbc.*
import mu.*
import org.apache.log4j.lf5.util.*
import java.io.*
import java.util.zip.*

/**
 * Simple key-value cache implemented using SQLite. Value can be any type of object,
 * it will be converted into JSON using GSON. On initialization it decompresses the
 * database, and on `close` it compresses it using GZip. Make sure you call `close`
 * after you finished using this Cache.
 *
 * @author Zsolt Jurányi
 */
class Cache : AutoCloseable {

	@DatabaseTable
	data class Entry(
			@DatabaseField(id = true)
			var id: String = "",
			@DatabaseField
			var json: String = ""
	)

	companion object : KLogging() {
		val FILENAME = "spotify-music-taste.cache"
		val FILENAME_GZ = FILENAME + ".gz"
	}

	val db: OrmLiteDatabase
	val dao: Dao<Entry, String>
	private val gson = Gson()

	init {
		if (File(FILENAME_GZ).exists()) decompressDatabase()
		val cs = ConnectionString.SQLITE().databaseFile(File(FILENAME)).build()
		db = OrmLiteDatabase.build(cs, null, null)
		db.createTables(Entry::class.java)
		dao = db.dao(Entry::class.java) as Dao<Entry, String>
	}

	override fun close() {
		db.close()
		compressDatabase()
	}

	private fun compressDatabase() {
		logger.info("Compressing database")
		val inputStream = FileInputStream(FILENAME)
		val outputStream = GZIPOutputStream(FileOutputStream(FILENAME_GZ))
		StreamUtils.copy(inputStream, outputStream)
		inputStream.close()
		outputStream.finish()
		outputStream.close()
		File(FILENAME).delete()
	}

	private fun decompressDatabase() {
		logger.info("Decompressing database")
		val inputStream = GZIPInputStream(FileInputStream(FILENAME_GZ))
		val outputStream = FileOutputStream(FILENAME)
		StreamUtils.copy(inputStream, outputStream)
		inputStream.close()
		outputStream.close()
		File(FILENAME_GZ).delete()
	}

	fun <T : Any?> fetch(id: String, type: Class<T>): T? {
		val json = dao.queryForId(id)?.json
		return if (null == json) null else gson.fromJson(json, type)
	}

	fun store(id: String, obj: Any) {
		dao.createOrUpdate(Entry(id, gson.toJson(obj)))
	}
}
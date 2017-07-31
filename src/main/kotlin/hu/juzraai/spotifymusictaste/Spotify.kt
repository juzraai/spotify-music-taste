package hu.juzraai.spotifymusictaste

import com.wrapper.spotify.*
import com.wrapper.spotify.models.*
import mu.*

/**
 * Provides caching layer above Spotify Java API.
 *
 * @author Zsolt Jurányi
 */
class Spotify(clientId: String, clientSecret: String) : AutoCloseable {

	companion object : KLogging()

	var api = Api.builder().clientId(clientId).clientSecret(clientSecret).build()
	var init: Boolean = false
	val cache = Cache()

	init {
		api.setAccessToken(api.clientCredentialsGrant().build().get().accessToken)
	}

	fun album(id: String): Album {
		val cacheId = "album/$id"
		var obj = cache.fetch(cacheId, Album::class.java)
		if (null == obj) {
			logger.debug("Querying album data: {}", id)
			obj = api().getAlbum(id).build().get()
			cache.store(cacheId, obj)
		}
		return obj!!
	}

	private fun api(): Api {
		if (!init) {
			logger.info("Initializing Spotify API connection")
			val creds = api.clientCredentialsGrant().build().get()
			api.setAccessToken(creds.accessToken)
			init = true
		}
		return api
	}

	fun artist(id: String): Artist {
		val cacheId = "artist/$id"
		var obj = cache.fetch(cacheId, Artist::class.java)
		if (null == obj) {
			logger.debug("Querying artist data: {}", id)
			obj = api().getArtist(id).build().get()
			cache.store(cacheId, obj)
		}
		return obj!!
	}

	fun artist(simpleArtist: SimpleArtist): Artist {
		return artist(simpleArtist.id)
	}

	fun artistsAlbums(id: String): List<Album> = api().getAlbumsForArtist(id).build().get().items
			.map(SimpleAlbum::getId)
			.map(this::album)

	fun artistDebut(id: String): Int {
		val cacheId = "artist-debut/$id"
		var obj = cache.fetch(cacheId, Int::class.java)
		if (null == obj) {
			logger.debug("Querying artist debut: {}", id)
			obj = artistsAlbums(id).map(AlbumHelper()::releaseYear).min()!!
			cache.store(cacheId, obj)
		}
		return obj!!
	}

	override fun close() {
		cache.close()
	}

	fun track(id: String): Track {
		val cacheId = "track/$id"
		var obj = cache.fetch(cacheId, Track::class.java)
		if (null == obj) {
			logger.debug("Querying track data: {}", id)
			obj = api().getTrack(id).build().get()
			cache.store(cacheId, obj)
		}
		return obj!!
	}
}
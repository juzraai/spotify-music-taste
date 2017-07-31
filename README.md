# Spotify Music Taste

*Tool for a garage-research project of mine, which aims to help me analyze, explore and understand my taste in music. :)*



## Implementation notes

* Works stand-alone, but you can use it as a library and you can customize it.
* Written in [Kotlin](https://kotlinlang.org/), therefore it runs on the JVM.
* Built on top of [thelinmichael/spotify-web-api-java](https://github.com/thelinmichael/spotify-web-api-java).
* Word cloud images are generated via [kennycason/kumo](https://github.com/kennycason/kumo).
* The font used on word clouds is [Kalam](https://fonts.google.com/specimen/Kalam).
* Caches every *Spotify* request into an *SQLite* database and compresses the whole file using *GZip*.



## How it works

* The program's input:
	* *Spotify API* client ID and client secret
	* input text file containing *Spotify* track IDs/URLs per line
* At first it decompresses the cache file if any.
* The program **grabs tracks' data** using [Spotify Web API](https://developer.spotify.com/web-api/):
	* track details
	* album details
	* details of artists
	* details of artists's albums
* The program **caches all requests** to an *SQLite* database (`spotify-music-taste.cache`), therefore no redundant requests are sent to *Spotify*. And also you may use this database for further analysis.
* During data grabbing, the program aggregates the information received from *Spotify*, mainly into key-value maps.
* At the end, the program **generates CSV files and word cloud images** based on these key-value pairs.
* Finally it compresses the cache file.



## Aggregations

* count of unique tracks, artists, genres
* count of tracks by their artists
* count of tracks by their artists' debut year (release year of artist's oldest album)
* count of tracks by their artists' debut decade
* count of tracks by their artists's genres
* count of tracks by words of their artists's genres (e.g. "hard rock" genre will increase counters "hard" and "rock")
* count of tracks by year
* count of tracks by decade
* min/avg/max duration of tracks in seconds
* count of tracks by duration minutes (e.g. a 3:45 track length will increase counter for "3-4 min. tracks")

All of these aggregations are exported by the program into CSV files (with `\t` delimiter).



## Usage



### Acquire Spotify API access 

1. Register to [Spotify Developer](https://developer.spotify.com/).
2. Create an app, with a name of your choice (e.g. "Test app"). You don't have to fill in any fields but *Application name* and *Description*.
3. You will get a *client ID* and a *client secret* string - save it somewehere.



### Create input file

1. Open *Spotify* and go to your saved tracks (Your music -> Songs).
2. Click on any track, then press `CTRL+A` (select all) and `CTRL+C` (copy).
3. Paste the links into a text file (e.g. in Notepad) and save it.

Sample:

```text
https://open.spotify.com/track/5yUpcOldttmTc01ecqSm33
https://open.spotify.com/track/1ZiH8O9Y25AjxM4a25KNzn
https://open.spotify.com/track/1yVQYRNPbBGtL7YvL5H8zz
```

This format also works:

```text
spotify:track:1spuZq5HizgvwQfLn2y4Zt
spotify:track:4D9zxXY112kzDPXXDJtPgV
spotify:track:61i8W3gHpGUAIDlQj5UUAE
```

(And of course, you can mix them. :D)



### Run the stand-alone JAR

Requires JRE.

```bash
#!/usr/bin/env bash

spotifyClientId="..."
spotifyClientSecret="..."
inputFile="..."
outputDir="output"

java -jar spotify-music-taste-1.0-SNAPSHOT-jar-with-dependencies.jar \
	-ci $spotifyClientId \
	-cs $spotifyClientSecret \
	-i $inputFile \
	-o $outputDir
```



### Create JAR file

Requires JDK, Maven.

```
mvn clean package -q
```



### Run program without packaging

Requires JDK, Maven.

```bash
#!/usr/bin/env bash

spotifyClientId="..."
spotifyClientSecret="..."
inputFile="..."
outputDir="output"

mvn clean compile exec:java -q -Dexec.args="\
	-ci $spotifyClientId \
	-cs $spotifyClientSecret \
	-i $inputFile \
	-o $outputDir"
```



## Use as a library (notes)



### Cache

Make sure you **close** the cache after you finished using it. Higher-level classes must be closed too, and they will call lower-level `close()` methods for simplicity:

```text
SpotifyMusicTaste.useAndClose()
 -> this.close()
     -> this.spotify.close()
         -> this.cache.close()
```

This is needed to ensure all database changes are written on the disk and also the cache will **compress the database** at the end.



### Generate a word cloud

```kotlin
fun main(args: Array<String>) {
	
	val m = mapOf<String, Int>(
		"word 1" to 5,
		"word 2" to 3,
		"word 3" to 7,
		"word 4" to 4
		// ...
	)
	
    val e = Exporter("output-dir")
    
    // simple word cloud 
    val c1 = e.wordCloudConfig(m, "simple.png", null)
    
    // bipolar word cloud
    val c2 = e.wordCloudConfig(m, "bipolar.png") {        
        // it: Map.Entry<String, Int>
        it.key.matches(Regex(".*[2468]$")) && it.value > 3        
    }
    
    // generate them
    e.wordCloud(c1, c2)
}
```

Look at the lambda function at `c2`. That is a **divider function**, which will create two distinct maps from the original frequency map. Those entries that got `true` result, will go to the first map, and those which got `false` go to the second map. First map will appear as green, and second map will appear as white on the word cloud image.

`Exporter::wordCloud` will generate word clouds for the given configurations in a **parallel** way.

If you need to customize the word cloud parameters, dig deeper and use *Kumo* itself instead. :)



## Known Issues

* *Spotify* does not include every album from every artist, so *artist debut year* will not be accurate for every artist.
* *Spotify API* may respond with `HTTP 502` after a certain amount of requests. **Solution:** wait for 1-2 min, then start the program again. (Because of the cache, you lose nothing.)



## Future ideas

* Fine-tune log level with command line argument (e.g. -v).
* Enhance connectivity with *Spotify API* to make user be able to login: that way we can grab the user's saved tracks automatically - no need for manually created input file.
* Maybe generate a *master* table with all data in it (Cartesian product), which can be used for OLAP cubes or in BI software. 
* It would be nice to make statistics based on the singers gender, e.g. *do I like female singers more?* The gender of a person can be fetched from *Wikidata*, and the singer of a band can be identified by searching for the "lead vocals" on the band's *Wikipedia* page.
* We can export a full analysis, like an infographic - e.g. as a Jekyll site. :)
* Host the thing somewhere as a service. Heroku? HelioHost?



## License

MIT license, Copyright (c) 2017 Zsolt Jurányi

See `LICENSE*` files for notes for this project and for the dependences' licenses as well.
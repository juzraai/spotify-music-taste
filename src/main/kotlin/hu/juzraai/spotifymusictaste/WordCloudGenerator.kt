package hu.juzraai.spotifymusictaste

import com.kennycason.kumo.*
import com.kennycason.kumo.bg.*
import com.kennycason.kumo.font.*
import com.kennycason.kumo.font.scale.*
import com.kennycason.kumo.image.*
import com.kennycason.kumo.palette.*
import com.kennycason.kumo.wordstart.*
import mu.*
import java.awt.*

/**
 * Generates word cloud images from frequency maps, using Kumo.
 *
 * @author Zsolt Jurányi
 */
class WordCloudGenerator {

	companion object : KLogging() {
		val SPOTIFY_GREEN = Color(30, 215, 96)

		val ANGLE = 0
		val COLOR_1: Color = SPOTIFY_GREEN
		val COLOR_2: Color = Color.WHITE
		val FONT = KumoFont(WordCloudGenerator::class.java.classLoader.getResourceAsStream("font/kalam/Kalam-Bold.ttf"))
		val FONT_SIZE_MIN = 14 // 12
		val FONT_SIZE_MAX = FONT_SIZE_MIN * 4 // 4
		val PADDING = 3
		val RADIUS = 300
	}

	val background = CircleBackground(RADIUS)
	val colorPalette = LinearGradientColorPalette(COLOR_1, COLOR_2, 30)
	val dimension = Dimension(RADIUS * 2, RADIUS * 2)
	val fontScalar = SqrtFontScalar(FONT_SIZE_MIN, FONT_SIZE_MAX)

	fun generate(frequencyMap: Map<String, Int>, filename: String) {
		val wfs = frequencyMap.map { WordFrequency(it.key, it.value) }.toList()
		with(WordCloud(dimension, CollisionMode.PIXEL_PERFECT)) {
			setAngleGenerator(AngleGenerator(ANGLE))
			setBackground(background)
			setColorPalette(colorPalette)
			setFontScalar(fontScalar)
			setKumoFont(FONT)
			setPadding(PADDING)
			setWordStartStrategy(CenterWordStart())
			build(wfs)
			writeToFile(filename)
		}
	}

	fun generate(frequencyMap1: Map<String, Int>, frequencyMap2: Map<String, Int>, filename: String) {
		val wfs1 = frequencyMap1.map { WordFrequency(it.key, it.value) }.toList()
		val wfs2 = frequencyMap2.map { WordFrequency(it.key, it.value) }.toList()
		with(PolarWordCloud(dimension, CollisionMode.PIXEL_PERFECT, PolarBlendMode.BLUR)) {
			setAngleGenerator(AngleGenerator(ANGLE))
			setBackground(background)
			setColorPalette(ColorPalette(SPOTIFY_GREEN))
			setColorPalette2(ColorPalette(Color.WHITE))
			setFontScalar(fontScalar)
			setKumoFont(FONT)
			setPadding(PADDING)
			setWordStartStrategy(CenterWordStart())
			build(wfs1, wfs2)
			writeToFile(filename)
		}
	}

	/*
	Time measurements:
	1 wordcloud (Segoe Print): 23 sec
	2 wordclouds parallel (Segoe Print): 32 sec
	3 wordcloud parallel:
		default font: 25 sec

		latin-ext:
			Lemonada-Bold.ttf: 52 sec
		>>> Kalam-Bold.ttf: 34 sec
			Knewave-Regular.ttf: 30 sec
			ShadowsIntoLightTwo-Regular.ttf: 23 sec
			CaveatBrush-Regular.ttf: 22 sec
			Ranga-Bold.ttf: 19 sec
			Oregano-Regular.ttf: 18 sec
			AmaticSC-Bold.ttf: 17 sec
			BubblegumSans-Regular.ttf: 16 sec
			Bangers-Regular.ttf: 10 sec

		latin:
			Segoe Print: 39 sec
			Chewy.ttf: 21 sec
			Boogaloo-Regular.ttf: 14 sec (LOOKS GOOD!)
			LuckiestGuy.ttf: 4 sec
    */

}
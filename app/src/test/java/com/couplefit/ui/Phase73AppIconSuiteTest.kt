package com.couplefit.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 73: Verification suite for the production Android App Icon and Adaptive Icon hierarchy.
 */
class Phase73AppIconSuiteTest {

    private val resDir = File("src/main/res")
    private val mainDir = File("src/main")

    private fun readPngDimensions(file: File): Pair<Int, Int> {
        val bytes = file.readBytes()
        require(bytes.size >= 24) { "File ${file.name} is not a valid PNG header" }
        val width = ((bytes[16].toInt() and 0xFF) shl 24) or
                ((bytes[17].toInt() and 0xFF) shl 16) or
                ((bytes[18].toInt() and 0xFF) shl 8) or
                (bytes[19].toInt() and 0xFF)
        val height = ((bytes[20].toInt() and 0xFF) shl 24) or
                ((bytes[21].toInt() and 0xFF) shl 16) or
                ((bytes[22].toInt() and 0xFF) shl 8) or
                (bytes[23].toInt() and 0xFF)
        return Pair(width, height)
    }

    @Test
    fun testAdaptiveIconXmlDefinitionsExistAndValid() {
        val launcherXml = File(resDir, "mipmap-anydpi-v26/ic_launcher.xml")
        val launcherRoundXml = File(resDir, "mipmap-anydpi-v26/ic_launcher_round.xml")

        assertTrue("ic_launcher.xml must exist", launcherXml.exists())
        assertTrue("ic_launcher_round.xml must exist", launcherRoundXml.exists())

        val content = launcherXml.readText()
        assertTrue("Must declare adaptive-icon", content.contains("<adaptive-icon"))
        assertTrue("Must declare background", content.contains("@mipmap/ic_launcher_background"))
        assertTrue("Must declare foreground", content.contains("@mipmap/ic_launcher_foreground"))
        assertTrue("Must declare monochrome for Material You", content.contains("@drawable/ic_launcher_monochrome"))
    }

    @Test
    fun testMonochromeVectorDrawableExistsAndValid() {
        val monochromeXml = File(resDir, "drawable/ic_launcher_monochrome.xml")
        assertTrue("ic_launcher_monochrome.xml must exist", monochromeXml.exists())

        val content = monochromeXml.readText()
        assertTrue("Must be a vector drawable", content.contains("<vector"))
        assertTrue("Must specify 108dp width", content.contains("android:width=\"108dp\""))
        assertTrue("Must specify 108dp height", content.contains("android:height=\"108dp\""))
        assertTrue("Must specify 108 viewport", content.contains("android:viewportWidth=\"108\""))
    }

    @Test
    fun testAllDensityMipmapBucketsExistWithCorrectDimensions() {
        data class DensityConfig(
            val folder: String,
            val legacySize: Int,
            val adaptiveSize: Int
        )

        val configs = listOf(
            DensityConfig("mipmap-mdpi", 48, 108),
            DensityConfig("mipmap-hdpi", 72, 162),
            DensityConfig("mipmap-xhdpi", 96, 216),
            DensityConfig("mipmap-xxhdpi", 144, 324),
            DensityConfig("mipmap-xxxhdpi", 192, 432)
        )

        for (cfg in configs) {
            val dir = File(resDir, cfg.folder)
            assertTrue("Density dir ${cfg.folder} must exist", dir.exists())

            // Legacy square
            val legacySquare = File(dir, "ic_launcher.png")
            assertTrue("${legacySquare.path} must exist", legacySquare.exists())
            val (sqW, sqH) = readPngDimensions(legacySquare)
            assertEquals("Width for ${cfg.folder} legacy", cfg.legacySize, sqW)
            assertEquals("Height for ${cfg.folder} legacy", cfg.legacySize, sqH)

            // Legacy round
            val legacyRound = File(dir, "ic_launcher_round.png")
            assertTrue("${legacyRound.path} must exist", legacyRound.exists())
            val (roundW, roundH) = readPngDimensions(legacyRound)
            assertEquals("Width for ${cfg.folder} legacy round", cfg.legacySize, roundW)
            assertEquals("Height for ${cfg.folder} legacy round", cfg.legacySize, roundH)

            // Adaptive foreground
            val fg = File(dir, "ic_launcher_foreground.png")
            assertTrue("${fg.path} must exist", fg.exists())
            val (fgW, fgH) = readPngDimensions(fg)
            assertEquals("Width for ${cfg.folder} foreground", cfg.adaptiveSize, fgW)
            assertEquals("Height for ${cfg.folder} foreground", cfg.adaptiveSize, fgH)

            // Adaptive background
            val bg = File(dir, "ic_launcher_background.png")
            assertTrue("${bg.path} must exist", bg.exists())
            val (bgW, bgH) = readPngDimensions(bg)
            assertEquals("Width for ${cfg.folder} background", cfg.adaptiveSize, bgW)
            assertEquals("Height for ${cfg.folder} background", cfg.adaptiveSize, bgH)
        }
    }

    @Test
    fun testPlayStoreWebGraphicDimensions() {
        val webIcon = File(mainDir, "ic_launcher-web.png")
        assertTrue("ic_launcher-web.png must exist in app/src/main", webIcon.exists())
        val (w, h) = readPngDimensions(webIcon)
        assertEquals("Web icon must be 512px wide", 512, w)
        assertEquals("Web icon must be 512px high", 512, h)
    }

    @Test
    fun testAndroidManifestIconBindings() {
        val manifest = File(mainDir, "AndroidManifest.xml")
        assertTrue("AndroidManifest.xml must exist", manifest.exists())
        val text = manifest.readText()
        assertTrue("Manifest must declare android:icon=@mipmap/ic_launcher", text.contains("android:icon=\"@mipmap/ic_launcher\""))
        assertTrue("Manifest must declare android:roundIcon=@mipmap/ic_launcher_round", text.contains("android:roundIcon=\"@mipmap/ic_launcher_round\""))
    }
}

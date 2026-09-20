package cyberoxo.io.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Singleton sound manager for OxoNeo.
 *
 * All sounds are generated programmatically at runtime via [AudioTrack]
 * (sine-wave synthesis with envelope shaping). No binary asset files needed.
 *
 * Usage:
 *   SoundManager.init(context)          // call once in Application/Activity
 *   SoundManager.playTapX()
 *   SoundManager.playWin()
 *   SoundManager.release()              // call in onDestroy of last Activity
 */
object SoundManager {

    private const val SAMPLE_RATE = 44100

    private var soundPool: SoundPool? = null

    // Sound IDs loaded into SoundPool
    private var idTapX   = 0
    private var idTapO   = 0
    private var idWin    = 0
    private var idDraw   = 0
    private var idButton = 0
    private var idLaunch = 0

    private var loaded = false
    private val scope  = CoroutineScope(Dispatchers.IO)

    // ── Initialise ───────────────────────────────────────────────────────────

    fun init(context: Context) {
        if (loaded) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        scope.launch {
            // Generate PCM → write temp file → load into SoundPool
            val sp = soundPool ?: return@launch

            idTapX   = sp.loadPcm(context, generateTone(880.0,  0.06, Envelope.CLICK))
            idTapO   = sp.loadPcm(context, generateTone(660.0,  0.06, Envelope.CLICK))
            idWin    = sp.loadPcm(context, generateFanfare())
            idDraw   = sp.loadPcm(context, generateDrawSound())
            idButton = sp.loadPcm(context, generateTone(1200.0, 0.04, Envelope.TICK))
            idLaunch = sp.loadPcm(context, generateChime())

            loaded = true
        }
    }

    // ── Play functions ───────────────────────────────────────────────────────

    fun playTapX()   = play(idTapX,   1.0f)
    fun playTapO()   = play(idTapO,   1.0f)
    fun playWin()    = play(idWin,    1.0f)
    fun playDraw()   = play(idDraw,   0.85f)
    fun playButton() = play(idButton, 0.7f)
    fun playLaunch() = play(idLaunch, 0.9f)

    private fun play(soundId: Int, volume: Float) {
        if (!loaded || soundId == 0) return
        soundPool?.play(soundId, volume, volume, 1, 0, 1f)
    }

    // ── Release ──────────────────────────────────────────────────────────────

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
        idTapX = 0; idTapO = 0; idWin = 0
        idDraw = 0; idButton = 0; idLaunch = 0
    }

    // ── PCM generation helpers ────────────────────────────────────────────────

    private enum class Envelope { CLICK, TICK, SMOOTH }

    /**
     * Generates a mono PCM-16 buffer for a single sine-wave tone.
     * @param freq      Frequency in Hz
     * @param duration  Duration in seconds
     * @param env       Amplitude envelope shaping
     */
    private fun generateTone(freq: Double, duration: Double, env: Envelope): ByteArray {
        val samples = (SAMPLE_RATE * duration).toInt()
        val buf     = ByteBuffer.allocate(samples * 2).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until samples) {
            val t     = i.toDouble() / SAMPLE_RATE
            val phase = 2.0 * PI * freq * t

            // Raw sine
            var amp = sin(phase)

            // Envelope shaping
            val progress = i.toDouble() / samples
            amp *= when (env) {
                Envelope.CLICK  -> if (progress < 0.1) progress / 0.1 else (1.0 - progress)
                Envelope.TICK   -> if (progress < 0.05) progress / 0.05 else (1.0 - progress).coerceAtLeast(0.0)
                Envelope.SMOOTH -> sin(PI * progress)
            }

            buf.putShort((amp * Short.MAX_VALUE * 0.8).toInt().toShort())
        }
        return buf.array()
    }

    /**
     * Ascending 3-note fanfare: C5 → E5 → G5, each 120 ms with short gaps.
     */
    private fun generateFanfare(): ByteArray {
        val notes = listOf(523.25 to 0.14, 659.25 to 0.14, 783.99 to 0.28)
        val gap   = generateSilence(0.04)

        return notes.flatMapIndexed { i, (freq, dur) ->
            val tone = generateTone(freq, dur, Envelope.SMOOTH).toList()
            if (i < notes.size - 1) tone + gap.toList() else tone
        }.toByteArray()
    }

    /**
     * Draw sound: descending two-note (G4 → D4).
     */
    private fun generateDrawSound(): ByteArray {
        val n1  = generateTone(392.0, 0.12, Envelope.SMOOTH)
        val gap = generateSilence(0.03)
        val n2  = generateTone(293.66, 0.18, Envelope.SMOOTH)
        return n1 + gap + n2
    }

    /**
     * Launch chime: soft rising tones C4 → E4 → G4 → C5, slightly slower.
     */
    private fun generateChime(): ByteArray {
        val notes = listOf(261.63 to 0.1, 329.63 to 0.1, 392.0 to 0.1, 523.25 to 0.22)
        val gap   = generateSilence(0.02)
        return notes.flatMapIndexed { i, (freq, dur) ->
            val tone = generateTone(freq, dur, Envelope.SMOOTH).toList()
            if (i < notes.size - 1) tone + gap.toList() else tone
        }.toByteArray()
    }

    private fun generateSilence(duration: Double): ByteArray {
        val samples = (SAMPLE_RATE * duration).toInt()
        return ByteArray(samples * 2)
    }

    // ── SoundPool PCM loader ─────────────────────────────────────────────────

    /**
     * Loads a raw PCM-16 mono buffer into [SoundPool] by first writing it
     * to a temp file as a valid WAV, then loading from that file descriptor.
     */
    private fun SoundPool.loadPcm(context: Context, pcm: ByteArray): Int {
        val wavData = wrapPcmAsWav(pcm)
        val tmpFile = java.io.File(context.cacheDir, "snd_${pcm.hashCode()}.wav")
        tmpFile.writeBytes(wavData)
        return load(tmpFile.absolutePath, 1)
    }

    /**
     * Wraps raw PCM-16 mono 44100 Hz bytes in a minimal RIFF/WAV header.
     */
    private fun wrapPcmAsWav(pcm: ByteArray): ByteArray {
        val totalDataLen  = pcm.size + 36
        val byteRate      = SAMPLE_RATE * 2          // 16-bit mono
        val buf = ByteBuffer.allocate(44 + pcm.size).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buf.put("RIFF".toByteArray())
        buf.putInt(totalDataLen)
        buf.put("WAVE".toByteArray())

        // fmt  chunk
        buf.put("fmt ".toByteArray())
        buf.putInt(16)                    // PCM chunk size
        buf.putShort(1)                   // PCM format
        buf.putShort(1)                   // mono
        buf.putInt(SAMPLE_RATE)
        buf.putInt(byteRate)
        buf.putShort(2)                   // block align (1 channel * 16-bit / 8)
        buf.putShort(16)                  // bits per sample

        // data chunk
        buf.put("data".toByteArray())
        buf.putInt(pcm.size)
        buf.put(pcm)

        return buf.array()
    }
}

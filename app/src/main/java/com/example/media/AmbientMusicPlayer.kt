package com.example.media

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.MusicTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Built-in polyphonic ambient music synthesizer using Android AudioTrack.
 * Generates warm chime/piano-like melodies locally so music previews and recipient
 * playback work reliably offline without broken external audio URLs.
 */
class AmbientMusicPlayer {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var playbackJob: Job? = null
  private var audioTrack: AudioTrack? = null
  @Volatile private var volume: Float = 0.55f

  fun play(track: MusicTrack) {
    stop()
    if (track == MusicTrack.NONE) return

    playbackJob =
      scope.launch {
        val sampleRate = 22050
        val bufferSamples = sampleRate / 2
        val minBufBytes =
          AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        val audioBufBytes = maxOf(minBufBytes, bufferSamples * 2)

        val trackObj =
          AudioTrack.Builder()
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            )
            .setAudioFormat(
              AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            )
            .setBufferSizeInBytes(audioBufBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = trackObj
        try {
          trackObj.play()
          val pattern = melodyFor(track)
          val stepDurationSec =
            when (track) {
              MusicTrack.GEMA_SYAWAL -> 0.42
              MusicTrack.KLASIK_KORPORAT -> 0.52
              MusicTrack.MELODI_TRADISI -> 0.55
              MusicTrack.PIANO_MALAM -> 0.65
              MusicTrack.NONE -> 0.5
            }
          val samplesPerStep = (sampleRate * stepDurationSec).toInt()
          val pcm = ShortArray(samplesPerStep)
          var stepIdx = 0

          while (isActive) {
            val (rootFreq, harmonyFreq) = pattern[stepIdx % pattern.size]
            for (i in 0 until samplesPerStep) {
              val t = i.toDouble() / sampleRate
              val attack = (t / 0.025).coerceIn(0.0, 1.0)
              val decay = exp(-t * 3.4)
              val env = attack * decay
              val fundamental = sin(2.0 * PI * rootFreq * t)
              val harmonic = 0.35 * sin(2.0 * PI * rootFreq * 2.0 * t)
              val pad = 0.45 * sin(2.0 * PI * harmonyFreq * t)
              val sample = (fundamental + harmonic + pad) * env * 0.28 * volume
              pcm[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
            }
            if (isActive) {
              trackObj.write(pcm, 0, pcm.size)
            }
            stepIdx++
          }
        } catch (_: Exception) {
          // Graceful stop on release
        } finally {
          try {
            trackObj.stop()
          } catch (_: Exception) {
          }
          try {
            trackObj.release()
          } catch (_: Exception) {
          }
          if (audioTrack === trackObj) {
            audioTrack = null
          }
        }
      }
  }

  fun setVolume(newVolume: Float) {
    volume = newVolume.coerceIn(0f, 1f)
  }

  fun stop() {
    playbackJob?.cancel()
    playbackJob = null
    val existing = audioTrack
    audioTrack = null
    if (existing != null) {
      try {
        existing.pause()
        existing.flush()
      } catch (_: Exception) {
      }
    }
  }

  private fun melodyFor(track: MusicTrack): List<Pair<Double, Double>> =
    when (track) {
      MusicTrack.PIANO_MALAM ->
        listOf(
          261.63 to 130.81, // C4 / C3
          329.63 to 164.81, // E4
          392.00 to 196.00, // G4
          493.88 to 196.00, // B4
          440.00 to 220.00, // A4 / A3
          349.23 to 174.61, // F4
          329.63 to 164.81, // E4
          293.66 to 146.83, // D4
        )
      MusicTrack.MELODI_TRADISI ->
        // Pentatonic / Melayu gamelan-inspired warm intervals
        listOf(
          293.66 to 146.83, // D4
          329.63 to 164.81, // E4
          392.00 to 196.00, // G4
          440.00 to 220.00, // A4
          587.33 to 293.66, // D5
          440.00 to 220.00, // A4
          392.00 to 196.00, // G4
          329.63 to 164.81, // E4
        )
      MusicTrack.GEMA_SYAWAL ->
        listOf(
          392.00 to 196.00, // G4
          440.00 to 220.00, // A4
          493.88 to 246.94, // B4
          587.33 to 293.66, // D5
          523.25 to 261.63, // C5
          493.88 to 246.94, // B4
          440.00 to 220.00, // A4
          392.00 to 196.00, // G4
        )
      MusicTrack.KLASIK_KORPORAT ->
        listOf(
          220.00 to 110.00, // A3
          277.18 to 138.59, // C#4
          329.63 to 164.81, // E4
          440.00 to 220.00, // A4
          392.00 to 196.00, // G4
          329.63 to 164.81, // E4
          293.66 to 146.83, // D4
          277.18 to 138.59, // C#4
        )
      MusicTrack.NONE -> emptyList()
    }
}

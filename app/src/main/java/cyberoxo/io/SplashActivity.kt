package cyberoxo.io

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import cyberoxo.io.databinding.ActivitySplashBinding
import cyberoxo.io.ui.SoundManager


/**
 * Splash screen shown for ~2.5 s on first launch.
 *
 * Animation sequence:
 *   1. Logo icon scale + fade-in (600 ms)
 *   2. OxoNeo wordmark fade-in (500 ms, 200 ms delay)
 *   3. Subtitle fade-in (400 ms, 500 ms delay)
 *   4. Loading bar fade-in + pulse glow (starts at 600 ms)
 *   5. Navigate to ModeSelectionActivity after 2 600 ms total
 *
 * Sound: launch chime plays at 400 ms after logo appears.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make status bar transparent / immersive
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // Initialise sounds early so they're ready when GameActivity launches
        SoundManager.init(applicationContext)

        playEntranceAnimation()
    }

    private fun playEntranceAnimation() {
        // ── 1. Logo fade + scale-in ──────────────────────────────────────────
        val logoScaleX = ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 0.85f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.ivLogo, "scaleY", 0.85f, 1f)
        val logoAlpha  = ObjectAnimator.ofFloat(binding.ivLogo, "alpha",  0f, 1f)
        AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration     = 800
            interpolator = OvershootInterpolator(1.1f)
            start()
        }

        // ── 2. Launch chime after logo pops in ──────────────────────────────
        handler.postDelayed({
            SoundManager.playLaunch()
        }, 400)

        // ── 3. Logo shimmer sweep (translationX on a semi-transparent overlay) ─
        // Achieved by animating the logo's scaleX briefly to simulate a glint
        handler.postDelayed({
            ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 1f, 1.04f, 1f).apply {
                duration     = 600
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }, 900)

        // ── 4. Loading bar ──────────────────────────────────────────────────
        binding.llLoadingContainer.postDelayed({
            ObjectAnimator.ofFloat(binding.llLoadingContainer, "alpha", 0f, 1f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(binding.viewLoadingBar, "scaleX", 0f, 1f).apply {
                duration     = 1800
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }, 500)

        // ── 5. Navigate ─────────────────────────────────────────────────────
        handler.postDelayed({
            navigateToModeSelection()
        }, 2600)
    }

    private fun navigateToModeSelection() {
        startActivity(Intent(this, ModeSelectionActivity::class.java))
        // Smooth fade transition
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

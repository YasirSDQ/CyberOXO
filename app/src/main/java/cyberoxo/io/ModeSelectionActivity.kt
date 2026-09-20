package cyberoxo.io

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cyberoxo.io.databinding.ActivityModeSelectionBinding
import cyberoxo.io.model.Difficulty
import cyberoxo.io.model.GameMode
import cyberoxo.io.ui.GlowAnimator
import cyberoxo.io.ui.SoundManager

/**
 * Mode selection screen – "Arena Stack" redesign.
 *
 * Layout: two full-width vertical tiles (Vs Bot on top, Vs Friend on bottom)
 * separated by a floating "VS" badge.
 *
 * Lets the player pick:
 *   • Vs Bot    – AI with adaptive difficulty (starts Medium, scales with skill)
 *   • Vs Friend – local pass-and-play
 *
 * Launches [GameActivity] with EXTRA_GAME_MODE and EXTRA_DIFFICULTY.
 */
class ModeSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModeSelectionBinding

    // Keep reference so we can cancel in onDestroy
    private var logoFloatAnimator: ObjectAnimator? = null

    companion object {
        const val EXTRA_GAME_MODE   = "EXTRA_GAME_MODE"
        const val EXTRA_DIFFICULTY  = "EXTRA_DIFFICULTY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets so content clears status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, sysBars.top, 0, sysBars.bottom)
            insets
        }

        setupCardAnimations()
        setupClickListeners()
        playEntrance()
    }

    override fun onDestroy() {
        super.onDestroy()
        logoFloatAnimator?.cancel()
    }

    // ── Card hover/press scale animations ────────────────────────────────────
    private fun setupCardAnimations() {
        listOf(binding.cardVsBot, binding.cardVsFriend).forEach { card ->
            card.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.animate()
                        .scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> v.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(220)
                        .setInterpolator(OvershootInterpolator(1.5f))
                        .start()
                }
                false // pass event to onClick
            }
        }
    }

    // ── Button click listeners ───────────────────────────────────────────────
    private fun setupClickListeners() {
        // Bot mode always starts at MEDIUM; GameActivity handles adaptive scaling
        binding.btnStartBot.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(binding.btnStartBot)
            launchGame(GameMode.VS_BOT, Difficulty.MEDIUM)
        }
        binding.btnStartFriend.setOnClickListener {
            SoundManager.playButton()
            GlowAnimator.animateButtonPress(binding.btnStartFriend)
            launchGame(GameMode.VS_FRIEND, Difficulty.EASY)
        }
    }

    private fun launchGame(mode: GameMode, difficulty: Difficulty) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(EXTRA_GAME_MODE,  mode.name)
            putExtra(EXTRA_DIFFICULTY, difficulty.name)
        }
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // ── Entrance stagger animation (vertical arena layout) ───────────────────
    private fun playEntrance() {
        // ── Header: logo + title drop in from top
        listOf(binding.ivLogoLarge, binding.tvTitle).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = -40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(480)
                .setStartDelay((80 + index * 100).toLong())
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    if (view === binding.ivLogoLarge) {
                        logoFloatAnimator = GlowAnimator.startLogoFloat(binding.ivLogoLarge)
                    }
                }
                .start()
        }

        // ── Bot tile slides up from below, with overshoot
        binding.cardVsBot.apply {
            alpha = 0f
            translationY = 120f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(580)
                .setStartDelay(320)
                .setInterpolator(OvershootInterpolator(1.1f))
                .start()
        }

        // ── Friend tile slides up from below (slightly later)
        binding.cardVsFriend.apply {
            alpha = 0f
            translationY = 160f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(580)
                .setStartDelay(440)
                .setInterpolator(OvershootInterpolator(1.1f))
                .start()
        }

        // ── VS badge pops in last with a scale punch
        binding.tvVsDivider.apply {
            alpha = 0f
            scaleX = 0.4f
            scaleY = 0.4f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(620)
                .setInterpolator(OvershootInterpolator(2.0f))
                .start()
        }
    }
}

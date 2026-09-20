package cyberoxo.io.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import cyberoxo.io.model.Player

/**
 * Centralised animation helper.
 * All visual effects for marks, player cards and win highlights live here
 * so GameActivity stays clean and focused on game logic.
 */
object GlowAnimator {

    // ── Mark entrance (scale-pop + alpha) ───────────────────────────────────
    /**
     * Animates a mark TextView appearing with a spring-pop effect.
     * Uses OvershootInterpolator for that tactile, snappy feel.
     */
    fun animateMarkEntrance(view: View) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha  = 0f

        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f).apply {
            duration     = 320
            interpolator = OvershootInterpolator(2.5f)
        }
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f).apply {
            duration     = 320
            interpolator = OvershootInterpolator(2.5f)
        }
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = 180
        }

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
        }
    }

    // ── Cell press feedback (scale-down then back) ───────────────────────────
    fun animateCellPress(view: View) {
        view.animate()
            .scaleX(0.88f)
            .scaleY(0.88f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .setInterpolator(OvershootInterpolator(1.5f))
                    .start()
            }.start()
    }

    // ── Winning line pulse ──────────────────────────────────────────────────
    /**
     * Pulses the alpha of each winning cell to draw attention.
     * Repeats 3 times then leaves cells fully visible.
     */
    fun animateWinningLine(cells: List<View>) {
        cells.forEach { cell ->
            val pulse = ObjectAnimator.ofFloat(cell, View.ALPHA, 1f, 0.35f, 1f).apply {
                duration     = 400
                repeatCount  = 3
                repeatMode   = ValueAnimator.REVERSE
                startDelay   = 100
            }
            pulse.start()
        }
    }

    // ── Win celebration: scale-pop spring on each winning cell ───────────────
    /**
     * Bounces each winning cell outward with an overshoot spring,
     * staggered slightly so they pop one after another.
     */
    fun animateWinCelebration(cells: List<View>) {
        cells.forEachIndexed { i, cell ->
            val delay = (i * 80).toLong()

            val scaleX = ObjectAnimator.ofFloat(cell, View.SCALE_X, 1f, 1.22f, 1f).apply {
                duration     = 400
                startDelay   = delay
                interpolator = OvershootInterpolator(3f)
            }
            val scaleY = ObjectAnimator.ofFloat(cell, View.SCALE_Y, 1f, 1.22f, 1f).apply {
                duration     = 400
                startDelay   = delay
                interpolator = OvershootInterpolator(3f)
            }
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                start()
            }
        }
    }

    // ── Board shake on draw ──────────────────────────────────────────────────
    /**
     * Shakes the entire game board left-right to signal a draw.
     * Uses CycleInterpolator to naturally oscillate.
     */
    fun animateBoardShake(board: View) {
        val shake = ObjectAnimator.ofFloat(board, View.TRANSLATION_X, 0f, 18f, -18f, 12f, -12f, 6f, -6f, 0f).apply {
            duration     = 480
            interpolator = DecelerateInterpolator()
        }
        shake.start()
    }

    // ── Player card bounce on turn switch ────────────────────────────────────
    /**
     * Bounces the newly-active player card upward slightly to draw attention.
     */
    fun animateCardBounce(card: View) {
        card.animate()
            .translationY(-8f)
            .setDuration(120)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                card.animate()
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(2f))
                    .start()
            }.start()
    }

    // ── Player card active highlight ────────────────────────────────────────
    /**
     * Animates a player card's alpha to indicate whose turn it is.
     * Active player → fully opaque; inactive player → dimmed.
     */
    fun setCardActive(card: View, active: Boolean) {
        card.animate()
            .alpha(if (active) 1f else 0.45f)
            .setDuration(280)
            .start()
    }

    // ── Score change number flash ───────────────────────────────────────────
    fun animateScoreUpdate(scoreView: TextView, newScore: Int) {
        scoreView.animate()
            .scaleX(1.4f)
            .scaleY(1.4f)
            .setDuration(120)
            .withEndAction {
                scoreView.text = newScore.toString()
                scoreView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .setInterpolator(OvershootInterpolator(2f))
                    .start()
            }.start()
    }

    // ── Turn indicator flip ─────────────────────────────────────────────────
    /**
     * Flips the arrow horizontally to point at the active player card.
     * [pointLeft] = true → arrow points left (toward Player X card).
     */
    fun animateTurnArrow(arrow: View, pointLeft: Boolean) {
        val targetScaleX = if (pointLeft) -1f else 1f
        arrow.animate()
            .scaleX(0f)
            .setDuration(100)
            .withEndAction {
                arrow.scaleX = targetScaleX
                arrow.animate()
                    .scaleX(if (pointLeft) -1f else 1f)
                    .setDuration(150)
                    .setInterpolator(OvershootInterpolator(1.5f))
                    .start()
            }.start()
    }

    // ── Apply glow text shadow to mark ──────────────────────────────────────
    /**
     * Applies a multi-layer neon glow to a mark TextView using setShadowLayer.
     * Android only supports a single shadow layer in XML so we call it
     * programmatically with the player's brand colour.
     */
    fun applyMarkGlow(textView: TextView, player: Player) {
        val (color, glowColor) = when (player) {
            Player.X -> Pair(Color.parseColor("#FF2D55"), Color.parseColor("#FF2D55"))
            Player.O -> Pair(Color.parseColor("#00F5FF"), Color.parseColor("#00F5FF"))
        }
        textView.setTextColor(color)
        textView.setShadowLayer(24f, 0f, 0f, glowColor)
    }

    // ── Winning cell glow colour flash ──────────────────────────────────────
    fun applyWinCellGlow(cell: View, player: Player) {
        val glowColor = when (player) {
            Player.X -> Color.parseColor("#33FF2D55")
            Player.O -> Color.parseColor("#3300F5FF")
        }
        cell.setBackgroundColor(glowColor)
    }

    // ── Splash logo glow pulse ───────────────────────────────────────────────
    fun startLogoGlowPulse(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.7f, 1f).apply {
            duration    = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode  = ValueAnimator.REVERSE
            start()
        }
    }

    // ── Logo idle float (mode selection screen) ──────────────────────────────
    /**
     * Continuous gentle vertical float for the mode-selection logo.
     * Creates a breathing / levitating effect.
     */
    fun startLogoFloat(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -10f, 0f).apply {
            duration     = 2600
            repeatCount  = ObjectAnimator.INFINITE
            repeatMode   = ValueAnimator.REVERSE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            start()
        }
    }

    // ── Result dialog icon animations ────────────────────────────────────────

    /**
     * Win: bounces the result mark icon with a repeating spring.
     * Draw: slowly rotates the emoji for a whimsical effect.
     */
    fun animateResultIcon(view: View, isWin: Boolean) {
        if (isWin) {
            // Repeating bounce: scale 1→1.15→1 infinitely
            val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.18f).apply {
                duration    = 500
                repeatCount = ObjectAnimator.INFINITE
                repeatMode  = ValueAnimator.REVERSE
                interpolator = OvershootInterpolator(2f)
                startDelay  = 300
            }
            val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.18f).apply {
                duration    = 500
                repeatCount = ObjectAnimator.INFINITE
                repeatMode  = ValueAnimator.REVERSE
                interpolator = OvershootInterpolator(2f)
                startDelay  = 300
            }
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                start()
            }
        } else {
            // Slow spin for the handshake emoji
            ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f).apply {
                duration    = 2400
                repeatCount = ObjectAnimator.INFINITE
                interpolator = android.view.animation.LinearInterpolator()
                startDelay  = 200
                start()
            }
        }
    }

    // ── Button press ripple-scale ────────────────────────────────────────────
    /**
     * Quick scale-down then spring-back on any button / card tap.
     * Can be called on button clicks for a tactile pressed feel.
     */
    fun animateButtonPress(view: View) {
        view.animate()
            .scaleX(0.94f)
            .scaleY(0.94f)
            .setDuration(70)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(2f))
                    .start()
            }.start()
    }

    // ── Idle cell shimmer (during bot think) ─────────────────────────────────
    /**
     * Subtle repeating alpha pulse on empty cells to show the game is "alive"
     * while the bot is computing its move.
     * Returns the animators so they can be cancelled when the bot plays.
     */
    fun startCellIdleShimmer(cells: List<View>): List<ObjectAnimator> {
        return cells.mapIndexed { i, cell ->
            ObjectAnimator.ofFloat(cell, View.ALPHA, 1f, 0.65f).apply {
                duration    = 700
                startDelay  = (i * 120).toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode  = ValueAnimator.REVERSE
                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    fun stopCellIdleShimmer(animators: List<ObjectAnimator>, cells: List<View>) {
        animators.forEach { it.cancel() }
        cells.forEach { it.alpha = 1f }
    }
}

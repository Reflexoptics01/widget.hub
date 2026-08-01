package com.reflex.widgethub

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.reflex.widgethub.data.CounterStore
import com.reflex.widgethub.domain.CounterState
import com.reflex.widgethub.domain.increment
import com.reflex.widgethub.domain.resetCurrent
import com.reflex.widgethub.reminders.ReminderScheduler
import com.reflex.widgethub.reminders.ReminderSettings
import com.reflex.widgethub.reminders.ReminderStore
import com.reflex.widgethub.reminders.ReminderType
import com.reflex.widgethub.ui.completedCycleLabel
import com.reflex.widgethub.ui.expressiveProgress
import com.reflex.widgethub.ui.isGoalPulse

class MainActivity : AppCompatActivity() {
    private lateinit var counterStore: CounterStore
    private lateinit var reminderStore: ReminderStore
    private lateinit var scheduler: ReminderScheduler
    private lateinit var lifetimeValue: TextView
    private lateinit var cycleValue: TextView
    private lateinit var currentValue: TextView
    private lateinit var goalValue: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        counterStore = CounterStore(this)
        reminderStore = ReminderStore(this)
        scheduler = ReminderScheduler(this)
        buildUi()
        renderCounter(counterStore.load())
        requestNotificationPermissionIfNeeded()

        if (intent.getBooleanExtra(EXTRA_OPEN_REMINDER, false)) root.post { root.requestFocus() }
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.BLACK)
            setPadding(dp(20), 0, dp(20), 0)
        }
        val baseLeft = root.paddingLeft
        val baseRight = root.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(baseLeft + bars.left, bars.top, baseRight + bars.right, bars.bottom)
            insets
        }

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(18), 0, dp(24))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val title = label("TASBEEH", 13f, android.graphics.Color.LTGRAY).apply {
            letterSpacing = 0.18f
        }
        content.addView(title, fullWidth())

        val summary = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundResource(R.drawable.bg_expressive_card)
        }
        val lifetimeBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        lifetimeBlock.addView(label("LIFETIME TOTAL", 11f, android.graphics.Color.GRAY))
        lifetimeValue = label("0", 24f, android.graphics.Color.WHITE)
        lifetimeBlock.addView(lifetimeValue)
        summary.addView(lifetimeBlock, LinearLayout.LayoutParams(0, -2, 1f))
        cycleValue = label("", 16f, getColorCompat(com.reflex.widgethub.R.color.red_accent)).apply {
            gravity = Gravity.CENTER
        }
        summary.addView(cycleValue, LinearLayout.LayoutParams(dp(54), -1))
        content.addView(summary, fullWidth().apply { topMargin = dp(12) })

        currentValue = label("0", 72f, android.graphics.Color.WHITE).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, dp(4))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        content.addView(currentValue, fullWidth())
        val reciteLabel = label("CURRENT COUNT", 11f, android.graphics.Color.GRAY).apply {
            gravity = Gravity.CENTER
        }
        content.addView(reciteLabel, fullWidth())
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            progressTintList = android.content.res.ColorStateList.valueOf(getColorCompat(R.color.red_accent))
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(getColorCompat(R.color.surface_high))
        }
        content.addView(progressBar, fullWidth().apply {
            topMargin = dp(16)
            leftMargin = dp(18)
            rightMargin = dp(18)
        })

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val goalButton = Button(this).apply {
            text = "GOAL"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 12f
            background = getDrawableCompat(R.drawable.bg_control)
            stateListAnimator = null
        }
        goalValue = label("GOAL 33", 12f, android.graphics.Color.WHITE).apply { gravity = Gravity.CENTER }
        goalButton.setOnClickListener { showGoalPicker() }
        controls.addView(goalButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        val tapButton = Button(this).apply {
            text = "RECITE +1"
            textSize = 13f
            setTextColor(android.graphics.Color.WHITE)
            background = getDrawableCompat(R.drawable.bg_expressive_control)
            stateListAnimator = null
            setOnClickListener { counterStore.update(::increment).also { renderCounter(it); TasbeehWidgetProvider.refreshAllWidgets(this@MainActivity) } }
        }
        controls.addView(tapButton, LinearLayout.LayoutParams(0, dp(48), 1.2f))
        val resetButton = Button(this).apply {
            text = "RESET"
            textSize = 12f
            setTextColor(android.graphics.Color.WHITE)
            background = getDrawableCompat(R.drawable.bg_control)
            stateListAnimator = null
            setOnClickListener { counterStore.update(::resetCurrent).also { renderCounter(it); TasbeehWidgetProvider.refreshAllWidgets(this@MainActivity) } }
        }
        controls.addView(resetButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        content.addView(controls, fullWidth().apply { topMargin = dp(12) })
        content.addView(goalValue, fullWidth().apply { topMargin = dp(4) })

        content.addView(sectionTitle("DAILY REMINDERS"), fullWidth().apply { topMargin = dp(28) })
        content.addView(reminderRow(ReminderType.TASBIH_FATIMA, "Tasbih-e-Fatima", "Subhan Allah 33 • Alhamdulillah 33 • Allahu Akbar 34"), fullWidth())
        content.addView(reminderRow(ReminderType.DUROOD, "Durood — 100 times", "Allahumma salli wa sallim 'ala Muhammad"), fullWidth().apply { topMargin = dp(8) })
        val note = label("Short wording shown for convenience; the longer Durood Ibrahimiyyah is also a complete form.", 11f, android.graphics.Color.GRAY)
        note.setPadding(dp(4), dp(10), dp(4), 0)
        content.addView(note, fullWidth())

        setContentView(root)
        ViewCompat.requestApplyInsets(root)
    }

    private fun reminderRow(type: ReminderType, title: String, subtitle: String): View {
        val settings = reminderStore.get(type)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundResource(R.drawable.bg_expressive_card)
        }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val textBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        textBlock.addView(label(title, 15f, android.graphics.Color.WHITE))
        textBlock.addView(label(subtitle, 11f, android.graphics.Color.GRAY))
        top.addView(textBlock, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = Switch(this).apply {
            isChecked = settings.enabled
            setOnCheckedChangeListener { _, enabled ->
                val updated = reminderStore.get(type).copy(enabled = enabled)
                reminderStore.set(type, updated)
                if (enabled) scheduler.schedule(type, updated) else scheduler.cancel(type)
            }
        }
        top.addView(toggle)
        card.addView(top)
        lateinit var timeButton: Button
        timeButton = Button(this).apply {
            text = formatTime(settings)
            textSize = 12f
            setTextColor(android.graphics.Color.WHITE)
            background = getDrawableCompat(R.drawable.bg_control)
            stateListAnimator = null
            setOnClickListener { showTimePicker(type, timeButton) }
        }
        card.addView(timeButton, LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(8) })
        return card
    }

    private fun showTimePicker(type: ReminderType, button: Button) {
        val current = reminderStore.get(type)
        TimePickerDialog(this, { _, hour, minute ->
            val updated = current.copy(hour = hour, minute = minute)
            reminderStore.set(type, updated)
            button.text = formatTime(updated)
            if (updated.enabled) scheduler.schedule(type, updated)
        }, current.hour, current.minute, false).show()
    }

    private fun showGoalPicker() {
        val presets = arrayOf("33", "99", "100", "1000", "Custom")
        AlertDialog.Builder(this).setTitle("Choose goal").setItems(presets) { _, which ->
            if (which == presets.lastIndex) {
                val input = android.widget.EditText(this).apply {
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    hint = "Enter goal"
                }
                AlertDialog.Builder(this).setTitle("Custom goal").setView(input)
                    .setPositiveButton("Save") { _, _ -> setGoal(input.text.toString().toLongOrNull()) }
                    .setNegativeButton("Cancel", null).show()
            } else setGoal(presets[which].toLong())
        }.show()
    }

    private fun setGoal(goal: Long?) {
        if (goal == null || goal < 1) return
        counterStore.save(counterStore.load().copy(goal = goal))
        renderCounter(counterStore.load())
        TasbeehWidgetProvider.refreshAllWidgets(this)
    }

    fun renderCounter(state: CounterState) {
        if (!::currentValue.isInitialized) return
        currentValue.text = state.currentCount.toString()
        lifetimeValue.text = state.lifetimeTotal.toString()
        goalValue.text = "GOAL ${state.goal}"
        cycleValue.text = completedCycleLabel(state).orEmpty()
        progressBar.progress = expressiveProgress(state)
        animateCount(state)
    }

    private fun animateCount(state: CounterState) {
        currentValue.animate().cancel()
        currentValue.scaleX = 0.92f
        currentValue.scaleY = 0.92f
        currentValue.animate().scaleX(1f).scaleY(1f).setDuration(220).start()
        if (isGoalPulse(state)) {
            currentValue.setTextColor(getColorCompat(R.color.red_accent))
            currentValue.animate().setDuration(360).withEndAction {
                currentValue.setTextColor(android.graphics.Color.WHITE)
            }.start()
        } else {
            currentValue.setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
    }

    private fun formatTime(settings: ReminderSettings): String = "%02d:%02d".format(settings.hour, settings.minute)
    private fun sectionTitle(text: String) = label(text, 12f, getColorCompat(R.color.red_accent)).apply { letterSpacing = 0.12f }
    private fun label(text: String, size: Float, color: Int) = TextView(this).apply { this.text = text; textSize = size; setTextColor(color) }
    private fun fullWidth() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun getColorCompat(id: Int) = androidx.core.content.ContextCompat.getColor(this, id)
    private fun getDrawableCompat(id: Int) = androidx.core.content.ContextCompat.getDrawable(this, id)

    companion object {
        const val EXTRA_OPEN_REMINDER = "open_reminder"
    }
}

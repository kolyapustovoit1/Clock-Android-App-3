package stu.cn.ua.clock.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import stu.cn.ua.clock.R
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * TimerFragment handles the timer functionality. It provides start, pause, lap, and stop features.
 * Time counting is managed using ScheduledExecutorService.
 */
class TimerFragment : BaseFragment(R.layout.fragment_timer) {

    // UI components
    private lateinit var textTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnLap: Button
    private lateinit var listLaps: ListView

    private val lapTimes = mutableListOf<String>()

    // Timer-related variables
    private var scheduler: ScheduledExecutorService? = null
    private var elapsedTime: Long = 0L
    private var lastLapTime: Long = 0L
    private var isTimerRunning = false
    private var isTimerPaused = false
    private var lapCount = 0

    /**
     * Initializes the UI components and sets up button listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeUI(view)
        setupButtonListeners()
    }

    /**
     * Initializes UI components and sets up the lap times adapter.
     */
    private fun initializeUI(view: View) {
        textTimer = view.findViewById(R.id.text_timer)
        btnStart = view.findViewById(R.id.btn_start)
        btnPause = view.findViewById(R.id.btn_pause)
        btnStop = view.findViewById(R.id.btn_stop)
        btnLap = view.findViewById(R.id.btn_lap)
        listLaps = view.findViewById(R.id.list_laps)

        listLaps.adapter = ArrayAdapter(requireContext(), R.layout.timer_list_item, lapTimes)
    }

    /**
     * Sets up listeners for all timer control buttons.
     */
    private fun setupButtonListeners() {
        btnStart.setOnClickListener { startTimer() }
        btnPause.setOnClickListener { togglePauseResumeTimer() }
        btnStop.setOnClickListener { stopTimer() }
        btnLap.setOnClickListener { handleLapOrClearAction() }
    }

    /**
     * Starts the timer and updates the UI states.
     */
    private fun startTimer() {
        if (isTimerRunning) return

        isTimerRunning = true
        isTimerPaused = false

        scheduler = Executors.newScheduledThreadPool(1)
        scheduler?.scheduleAtFixedRate({
            if (!isTimerPaused) {
                elapsedTime += 10
                updateTimerUI(elapsedTime)
            }
        }, 0, 10, TimeUnit.MILLISECONDS)

        updateButtonStates()
    }

    /**
     * Toggles between pausing and resuming the timer, updating UI states accordingly.
     */
    private fun togglePauseResumeTimer() {
        isTimerPaused = !isTimerPaused
        updateButtonStates()
    }

    /**
     * Stops the timer, shuts down the executor, and resets UI states.
     */
    private fun stopTimer() {
        scheduler?.shutdownNow()
        scheduler = null
        resetTimerState()
    }

    /**
     * Handles lap recording or clearing all lap times based on the current button state.
     */
    private fun handleLapOrClearAction() {
        if (btnLap.text == "Lap") {
            recordLap()
        } else if (btnLap.text == "Clear") {
            clearLaps()
        }
    }

    /**
     * Records a lap time and updates the list view.
     */
    private fun recordLap() {
        lapCount++
        val lapTime = elapsedTime - lastLapTime
        lastLapTime = elapsedTime
        lapTimes.add("Lap $lapCount - ${formatTime(lapTime)}")
        (listLaps.adapter as ArrayAdapter<*>).notifyDataSetChanged()
    }

    /**
     * Clears all lap times and resets the UI.
     */
    @SuppressLint("SetTextI18n")
    private fun clearLaps() {
        lapTimes.clear()
        textTimer.text = "00:00:00.000"
        (listLaps.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        resetTimerState()
    }

    /**
     * Updates the UI to display the current elapsed time.
     *
     * @param time Time in milliseconds to display.
     */
    private fun updateTimerUI(time: Long) {
        requireActivity().runOnUiThread {
            textTimer.text = formatTime(time)
        }
    }

    /**
     * Updates the states of control buttons based on the timer status.
     */
    @SuppressLint("SetTextI18n")
    private fun updateButtonStates() {
        btnPause.text = if (isTimerRunning && isTimerPaused) "Continue" else "Pause"
        btnLap.text = if (isTimerRunning) "Lap" else "Clear"

        btnPause.isEnabled = isTimerRunning
        btnStart.isEnabled = !isTimerRunning
        btnStop.isEnabled = isTimerRunning
    }

    /**
     * Resets the timer state and updates the UI.
     */
    private fun resetTimerState() {
        isTimerRunning = false
        isTimerPaused = false
        elapsedTime = 0L
        lastLapTime = 0L
        lapCount = 0
        updateButtonStates()
    }

    /**
     * Formats the time from milliseconds to HH:mm:ss.SSS.
     *
     * @param milliseconds Time in milliseconds.
     * @return Formatted time string.
     */
    private fun formatTime(milliseconds: Long): String {
        val hundredths = (milliseconds % 1000) / 10 // Вираховуємо соті частки секунди
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d:%02d", minutes, seconds, hundredths)
    }
}
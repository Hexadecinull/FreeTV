package io.github.ssmg4.freetv.player

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.ssmg4.freetv.R
import io.github.ssmg4.freetv.databinding.PlaybackSpeedBinding

class PlaybackSpeedControlsDialogFragment(
    private val currentSpeed: Float,
    private val onChange: (Float) -> Unit,
) : DialogFragment() {

    private lateinit var binding: PlaybackSpeedBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = PlaybackSpeedBinding.inflate(layoutInflater)

        return activity?.let { activity ->
            binding.apply {
                speedText.text = currentSpeed.toString()
                speed.value = currentSpeed.round(1)

                speed.addOnChangeListener { _, _, _ ->
                    val newSpeed = speed.value.round(1)
                    onChange(newSpeed)
                    speedText.text = newSpeed.toString()
                }
                incSpeed.setOnClickListener {
                    if (speed.value < 4.0f) {
                        speed.value = (speed.value + 0.1f).round(1)
                    }
                }
                decSpeed.setOnClickListener {
                    if (speed.value > 0.2f) {
                        speed.value = (speed.value - 0.1f).round(1)
                    }
                }
                resetSpeed.setOnClickListener { speed.value = 1.0f }
            }

            val builder = MaterialAlertDialogBuilder(activity)
            builder.setTitle(getString(R.string.select_playback_speed))
                .setView(binding.root)
                .create()
        } ?: throw IllegalStateException(getString(R.string.activity_cannot_be_null))
    }
}

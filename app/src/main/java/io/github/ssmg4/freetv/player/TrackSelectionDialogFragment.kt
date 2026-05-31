package io.github.ssmg4.freetv.player

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.media3.common.C
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.ssmg4.freetv.R
import java.util.Locale

@UnstableApi
class TrackSelectionDialogFragment(
    private val type: @C.TrackType Int,
    private val tracks: Tracks,
    private val onTrackSelected: (trackIndex: Int) -> Unit,
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        when (type) {
            C.TRACK_TYPE_AUDIO -> {
                return activity?.let { activity ->
                    val audioTracks = tracks.groups
                        .filter { it.type == C.TRACK_TYPE_AUDIO && it.isSupported }

                    val trackNames = audioTracks.mapIndexed { index, trackGroup ->
                        trackGroup.mediaTrackGroup.getName(type, index)
                    }.toTypedArray()

                    val selectedTrackIndex = audioTracks
                        .indexOfFirst { it.isSelected }.takeIf { it != -1 } ?: audioTracks.size

                    MaterialAlertDialogBuilder(activity).apply {
                        setTitle(getString(R.string.select_audio_track))
                        if (trackNames.isNotEmpty()) {
                            setSingleChoiceItems(
                                arrayOf(*trackNames, getString(R.string.disable)),
                                selectedTrackIndex,
                            ) { dialog, trackIndex ->
                                onTrackSelected(trackIndex.takeIf { it < trackNames.size } ?: -1)
                                dialog.dismiss()
                            }
                        } else {
                            setMessage(getString(R.string.no_audio_tracks_found))
                        }
                    }.create()
                } ?: throw IllegalStateException(getString(R.string.activity_cannot_be_null))
            }

            C.TRACK_TYPE_TEXT -> {
                return activity?.let { activity ->
                    val textTracks = tracks.groups
                        .filter { it.type == C.TRACK_TYPE_TEXT && it.isSupported }

                    val trackNames = textTracks.mapIndexed { index, trackGroup ->
                        trackGroup.mediaTrackGroup.getName(type, index)
                    }.toTypedArray()

                    val selectedTrackIndex = textTracks
                        .indexOfFirst { it.isSelected }.takeIf { it != -1 } ?: textTracks.size

                    MaterialAlertDialogBuilder(activity).apply {
                        setTitle(getString(R.string.select_subtitle_track))
                        if (trackNames.isNotEmpty()) {
                            setSingleChoiceItems(
                                arrayOf(*trackNames, getString(R.string.disable)),
                                selectedTrackIndex,
                            ) { dialog, trackIndex ->
                                onTrackSelected(trackIndex.takeIf { it < trackNames.size } ?: -1)
                                dialog.dismiss()
                            }
                        } else {
                            setMessage(getString(R.string.no_subtitle_tracks_found))
                        }
                        /* setPositiveButton("open_subtitle") { dialog, _ ->
                             dialog.dismiss()
                             onOpenLocalTrackClicked()
                         }*/
                    }.create()
                } ?: throw IllegalStateException(getString(R.string.activity_cannot_be_null))
            }

            else -> {
                throw IllegalArgumentException(
                    getString(R.string.track_type_not_supported),
                )
            }
        }
    }
}


@UnstableApi
fun TrackGroup.getName(trackType: @C.TrackType Int, index: Int): String {
    val format = this.getFormat(0)
    val language = format.language
    val label = format.label
    return buildString {
        if (label != null) {
            append(label)
        }
        if (isEmpty()) {
            if (trackType == C.TRACK_TYPE_TEXT) {
                append("Subtitle Track #${index + 1}")
            } else {
                append("Audio Track #${index + 1}")
            }
        }
        if (language != null && language != "und") {
            append(" - ")
            append(Locale(language).displayLanguage)
        }
    }
}

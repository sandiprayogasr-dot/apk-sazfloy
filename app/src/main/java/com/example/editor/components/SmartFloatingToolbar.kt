package com.example.editor.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SazfloyAccentPurple
import com.example.ui.theme.SazfloyBeatMarker
import com.example.ui.theme.SazfloyBorderSubtle
import com.example.ui.theme.SazfloyCapsuleToolbarBg
import com.example.ui.theme.SazfloyPink
import com.example.ui.theme.SazfloySecondaryPurple
import com.example.ui.theme.SazfloyTextMuted
import com.example.ui.theme.SazfloyTextPrimary
import com.example.ui.theme.SazfloyTextSecondary

enum class SelectionContext(val label: String) {
    GLOBAL("Menu Utama"),
    VIDEO("Video Clip"),
    AUDIO("Audio Track"),
    TEXT("Teks / Subtitle"),
    BEAT_MARKER("Beat Marker")
}

data class ToolbarActionItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val isPrimary: Boolean = false,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun SmartFloatingCapsuleToolbar(
    context: SelectionContext,
    onDeselect: () -> Unit,
    // Global Actions
    onSplit: () -> Unit,
    onBeat: () -> Unit,
    onSpeed: () -> Unit,
    onEffects: () -> Unit,
    onOverlay: () -> Unit,
    onAdjust: () -> Unit,
    onExport: () -> Unit,
    onText: () -> Unit,
    onAudio: () -> Unit,
    onSubtitles: () -> Unit,
    // Video Clip Actions
    onTrim: () -> Unit,
    onCrop: () -> Unit,
    onAnimation: () -> Unit,
    onVolume: () -> Unit,
    onDeleteClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onTransition: () -> Unit,
    // Audio Actions
    onFadeIn: () -> Unit,
    onFadeOut: () -> Unit,
    onNoiseReduction: () -> Unit,
    onDeleteAudio: () -> Unit,
    // Text Actions
    onFont: () -> Unit,
    onColor: () -> Unit,
    onSize: () -> Unit,
    onPosition: () -> Unit,
    onDuplicateText: () -> Unit,
    onDeleteText: () -> Unit,
    // Beat Marker Actions
    onEditMarker: () -> Unit,
    onMoveMarkerLeft: () -> Unit,
    onMoveMarkerRight: () -> Unit,
    onDeleteMarker: () -> Unit,
    onClearAllMarkers: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate context-sensitive action items
    val actions = when (context) {
        SelectionContext.GLOBAL -> listOf(
            ToolbarActionItem("split", "Split", Icons.Default.CallSplit, onClick = onSplit),
            ToolbarActionItem("beat", "Beat", Icons.Default.MusicNote, onClick = onBeat),
            ToolbarActionItem("speed", "Speed", Icons.Default.Speed, onClick = onSpeed),
            ToolbarActionItem("effects", "Effects", Icons.Default.AutoAwesome, onClick = onEffects),
            ToolbarActionItem("overlay", "Overlay", Icons.Default.Layers, onClick = onOverlay),
            ToolbarActionItem("adjust", "Adjust", Icons.Default.Tune, onClick = onAdjust),
            ToolbarActionItem("export", "Export", Icons.Default.FileDownload, isPrimary = true, onClick = onExport),
            ToolbarActionItem("text", "Text", Icons.Default.TextFields, onClick = onText),
            ToolbarActionItem("audio", "Audio", Icons.Default.Audiotrack, onClick = onAudio),
            ToolbarActionItem("subtitles", "Subtitles", Icons.Default.Subtitles, onClick = onSubtitles)
        )

        SelectionContext.VIDEO -> listOf(
            ToolbarActionItem("split", "Split", Icons.Default.CallSplit, onClick = onSplit),
            ToolbarActionItem("trim", "Trim", Icons.Default.ContentCut, onClick = onTrim),
            ToolbarActionItem("crop", "Crop", Icons.Default.Crop, onClick = onCrop),
            ToolbarActionItem("speed", "Speed", Icons.Default.Speed, onClick = onSpeed),
            ToolbarActionItem("anim", "Animation", Icons.Default.MotionPhotosOn, onClick = onAnimation),
            ToolbarActionItem("effects", "Effects", Icons.Default.AutoAwesome, onClick = onEffects),
            ToolbarActionItem("adjust", "Adjust", Icons.Default.Tune, onClick = onAdjust),
            ToolbarActionItem("volume", "Volume", Icons.Default.VolumeUp, onClick = onVolume),
            ToolbarActionItem("duplicate", "Duplicate", Icons.Default.ContentCopy, onClick = onDuplicateClip),
            ToolbarActionItem("transition", "Transition", Icons.Default.Transform, onClick = onTransition),
            ToolbarActionItem("delete", "Delete", Icons.Default.Delete, isDestructive = true, onClick = onDeleteClip)
        )

        SelectionContext.AUDIO -> listOf(
            ToolbarActionItem("trim", "Trim", Icons.Default.ContentCut, onClick = onTrim),
            ToolbarActionItem("volume", "Volume", Icons.Default.VolumeUp, onClick = onVolume),
            ToolbarActionItem("fade_in", "Fade In", Icons.Default.TrendingUp, onClick = onFadeIn),
            ToolbarActionItem("fade_out", "Fade Out", Icons.Default.TrendingDown, onClick = onFadeOut),
            ToolbarActionItem("beat", "Beat", Icons.Default.MusicNote, onClick = onBeat),
            ToolbarActionItem("noise_red", "Noise Red", Icons.Default.GraphicEq, onClick = onNoiseReduction),
            ToolbarActionItem("delete", "Delete", Icons.Default.Delete, isDestructive = true, onClick = onDeleteAudio)
        )

        SelectionContext.TEXT -> listOf(
            ToolbarActionItem("font", "Font", Icons.Default.FontDownload, onClick = onFont),
            ToolbarActionItem("color", "Color", Icons.Default.Palette, onClick = onColor),
            ToolbarActionItem("size", "Size", Icons.Default.FormatSize, onClick = onSize),
            ToolbarActionItem("position", "Position", Icons.Default.OpenWith, onClick = onPosition),
            ToolbarActionItem("anim", "Animation", Icons.Default.MotionPhotosOn, onClick = onAnimation),
            ToolbarActionItem("duplicate", "Duplicate", Icons.Default.ContentCopy, onClick = onDuplicateText),
            ToolbarActionItem("delete", "Delete", Icons.Default.Delete, isDestructive = true, onClick = onDeleteText)
        )

        SelectionContext.BEAT_MARKER -> listOf(
            ToolbarActionItem("edit", "Edit", Icons.Default.Edit, onClick = onEditMarker),
            ToolbarActionItem("move_left", "-100ms", Icons.AutoMirrored.Filled.ArrowBack, onClick = onMoveMarkerLeft),
            ToolbarActionItem("move_right", "+100ms", Icons.AutoMirrored.Filled.ArrowForward, onClick = onMoveMarkerRight),
            ToolbarActionItem("delete", "Hapus", Icons.Default.Delete, isDestructive = true, onClick = onDeleteMarker),
            ToolbarActionItem("clear_all", "Hapus Semua", Icons.Default.DeleteSweep, isDestructive = true, onClick = onClearAllMarkers)
        )
    }

    // Floating Capsule Container with soft blur-look dark background
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = SazfloyCapsuleToolbarBg,
        border = BorderStroke(1.dp, SazfloyBorderSubtle),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_floating_toolbar")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Context indicator tag when an item is selected
            if (context != SelectionContext.GLOBAL) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SazfloySecondaryPurple.copy(alpha = 0.35f))
                        .border(1.dp, SazfloyAccentPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Fokus: ${context.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SazfloyAccentPurple
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onDeselect() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Deselect",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Horizontal Scrollable Action Buttons Row
            AnimatedContent(
                targetState = actions,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "toolbar_actions_anim"
            ) { targetActions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    targetActions.forEach { action ->
                        ToolbarIconButton(
                            label = action.label,
                            icon = action.icon,
                            isPrimary = action.isPrimary,
                            isDestructive = action.isDestructive,
                            onClick = action.onClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val iconColor = when {
            isDestructive -> SazfloyPink
            isPrimary -> SazfloyAccentPurple
            else -> Color.White
        }
        val iconBg = when {
            isDestructive -> SazfloyPink.copy(alpha = 0.15f)
            isPrimary -> SazfloySecondaryPurple.copy(alpha = 0.45f)
            else -> Color.White.copy(alpha = 0.08f)
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg)
                .border(
                    1.dp,
                    if (isPrimary) SazfloyAccentPurple.copy(alpha = 0.6f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isDestructive) SazfloyPink else if (isPrimary) SazfloyAccentPurple else SazfloyTextSecondary,
            maxLines = 1
        )
    }
}

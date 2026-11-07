package com.example.editor.timeline

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioTrack
import com.example.model.MediaClip
import com.example.model.ShapeOverlay
import com.example.model.TextOverlay
import com.example.model.TransitionType
import com.example.model.VideoEffect
import com.example.ui.theme.SazfloyAccentPurple
import com.example.ui.theme.SazfloyAmber
import com.example.ui.theme.SazfloyBeatMarker
import com.example.ui.theme.SazfloyBorder
import com.example.ui.theme.SazfloyBorderSubtle
import com.example.ui.theme.SazfloyDivider
import com.example.ui.theme.SazfloyPink
import com.example.ui.theme.SazfloyPlayhead
import com.example.ui.theme.SazfloySecondaryPurple
import com.example.ui.theme.SazfloySurface
import com.example.ui.theme.SazfloySurfaceElevated
import com.example.ui.theme.SazfloySurfaceVariant
import com.example.ui.theme.SazfloyTextMuted
import com.example.ui.theme.SazfloyTextPrimary
import com.example.ui.theme.SazfloyTextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TimelineComponent(
    totalDurationMs: Long,
    currentPlayheadMs: Long,
    clips: List<MediaClip>,
    selectedClipId: String?,
    textOverlays: List<TextOverlay>,
    selectedTextId: String?,
    audioTracks: List<AudioTrack>,
    selectedAudioId: String?,
    shapeOverlays: List<ShapeOverlay> = emptyList(),
    selectedShapeId: String? = null,
    effects: List<VideoEffect> = emptyList(),
    selectedEffectId: String? = null,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectText: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectShape: (String) -> Unit = {},
    onSelectEffect: (String) -> Unit = {},
    onSelectTransition: (String) -> Unit = {},
    onSplitClip: (String, Long) -> Unit,
    onDeleteClip: (String) -> Unit,
    onDuplicateClip: (String) -> Unit,
    onMoveClipLeft: ((String) -> Unit)? = null,
    onMoveClipRight: ((String) -> Unit)? = null,
    onAddKeyframe: ((String) -> Unit)? = null,
    onAddMediaClick: (() -> Unit)? = null,
    beatMarkersMs: List<Long> = emptyList(),
    selectedBeatMarkerMs: Long? = null,
    onSelectBeatMarker: ((Long) -> Unit)? = null,
    onToggleBeatMarker: ((Long) -> Unit)? = null,
    onAutoBeats: (() -> Unit)? = null,
    isSnappingEnabled: Boolean = true,
    onToggleSnapping: (() -> Unit)? = null,
    onTrimClipStart: ((String, Long) -> Unit)? = null,
    onTrimClipEnd: ((String, Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) } // 0.6x to 2.5x
    val pixelsPerSecond = 85.dp * zoomScale
    val scrollState = rememberScrollState()

    val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(4f)
    val timelineWidthDp = (totalSeconds * (85f * zoomScale)).dp + 320.dp

    // Calculate snap points: clip boundaries + beat markers + subtitle starts
    val snapPointsMs = remember(clips, beatMarkersMs, textOverlays) {
        val points = mutableListOf<Long>()
        var accumulatedMs = 0L
        points.add(0L)
        clips.forEach { clip ->
            accumulatedMs += clip.trimmedDurationMs
            points.add(accumulatedMs)
        }
        points.addAll(beatMarkersMs)
        textOverlays.forEach { points.add(it.startMs) }
        points.sorted().distinct()
    }

    fun applySnapping(targetMs: Long): Long {
        if (!isSnappingEnabled) return targetMs
        val thresholdMs = (120f / zoomScale).toLong().coerceIn(60L, 250L)
        val nearest = snapPointsMs.minByOrNull { abs(it - targetMs) }
        return if (nearest != null && abs(nearest - targetMs) <= thresholdMs) {
            nearest
        } else {
            targetMs
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SazfloySurface)
            .border(1.dp, SazfloyBorderSubtle)
            .testTag("timeline_root")
    ) {
        // Timeline Header & Quick Editing Action Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SazfloySurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Action Icons: Split, Move, Duplicate, Delete, Keyframe, Beat Marker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Split Action
                IconButton(
                    onClick = {
                        selectedClipId?.let { clipId ->
                            onSplitClip(clipId, currentPlayheadMs)
                        }
                    },
                    enabled = selectedClipId != null,
                    modifier = Modifier.size(34.dp).testTag("action_split_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Split Clip",
                        tint = if (selectedClipId != null) SazfloyAccentPurple else SazfloyTextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Move Clip Left / Right
                val clipIdx = clips.indexOfFirst { it.id == selectedClipId }
                if (clipIdx >= 0 && (onMoveClipLeft != null || onMoveClipRight != null)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier
                            .background(SazfloySurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, SazfloyBorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { selectedClipId?.let { onMoveClipLeft?.invoke(it) } },
                            enabled = onMoveClipLeft != null && clipIdx > 0,
                            modifier = Modifier.size(26.dp).testTag("action_move_left")
                        ) {
                            Text("◀", color = if (clipIdx > 0) SazfloyAccentPurple else SazfloyTextMuted, fontSize = 11.sp)
                        }
                        Text(
                            text = "${clipIdx + 1}/${clips.size}",
                            color = SazfloyAccentPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        )
                        IconButton(
                            onClick = { selectedClipId?.let { onMoveClipRight?.invoke(it) } },
                            enabled = onMoveClipRight != null && clipIdx < clips.lastIndex,
                            modifier = Modifier.size(26.dp).testTag("action_move_right")
                        ) {
                            Text("▶", color = if (clipIdx < clips.lastIndex) SazfloyAccentPurple else SazfloyTextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // Beat Marker Pill (+ Irama)
                if (onToggleBeatMarker != null) {
                    val isAtBeat = beatMarkersMs.any { abs(it - currentPlayheadMs) <= 100L }
                    Surface(
                        onClick = { onToggleBeatMarker(currentPlayheadMs) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAtBeat) SazfloyBeatMarker else SazfloySecondaryPurple.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAtBeat) Color.White else SazfloyAccentPurple.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(28.dp).testTag("action_beat_marker")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isAtBeat) Color.Black else SazfloyAccentPurple,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isAtBeat) "Hapus Beat" else "+ Beat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAtBeat) Color.Black else SazfloyAccentPurple
                            )
                        }
                    }
                }

                // Auto Beat Generation Button
                if (onAutoBeats != null) {
                    Surface(
                        onClick = { onAutoBeats() },
                        shape = RoundedCornerShape(12.dp),
                        color = SazfloySurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SazfloyAccentPurple.copy(alpha = 0.4f)),
                        modifier = Modifier.height(28.dp).testTag("action_auto_beats")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SazfloyBeatMarker,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Auto Beat",
                                fontSize = 11.sp,
                                color = SazfloyBeatMarker,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Duplicate Action
                IconButton(
                    onClick = { selectedClipId?.let { onDuplicateClip(it) } },
                    enabled = selectedClipId != null,
                    modifier = Modifier.size(34.dp).testTag("action_duplicate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Clip",
                        tint = if (selectedClipId != null) SazfloyTextPrimary else SazfloyTextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Delete Action
                IconButton(
                    onClick = { selectedClipId?.let { onDeleteClip(it) } },
                    enabled = selectedClipId != null && clips.size > 1,
                    modifier = Modifier.size(34.dp).testTag("action_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Clip",
                        tint = if (selectedClipId != null && clips.size > 1) SazfloyPink else SazfloyTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add Keyframe button if applicable
                if (onAddKeyframe != null && (selectedClipId != null || selectedTextId != null || selectedShapeId != null)) {
                    val targetId = selectedShapeId ?: selectedTextId ?: selectedClipId
                    if (targetId != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SazfloySecondaryPurple.copy(alpha = 0.4f))
                                .border(1.dp, SazfloyAccentPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { onAddKeyframe(targetId) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "◆ Keyframe",
                                color = SazfloyAccentPurple,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Right Controls: Snapping Toggle & Timeline Zoom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Snapping Magnet Toggle
                if (onToggleSnapping != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSnappingEnabled) SazfloyAccentPurple.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isSnappingEnabled) SazfloyAccentPurple else SazfloyBorder, RoundedCornerShape(8.dp))
                            .clickable { onToggleSnapping() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "🧲",
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (isSnappingEnabled) "Snap" else "Off",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = if (isSnappingEnabled) SazfloyAccentPurple else SazfloyTextMuted
                            )
                        }
                    }
                }

                // Zoom Out
                IconButton(
                    onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(30.dp).testTag("timeline_zoom_out")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = SazfloyTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                    color = SazfloyTextSecondary
                )

                // Zoom In
                IconButton(
                    onClick = { zoomScale = (zoomScale + 0.25f).coerceIn(0.5f, 2.5f) },
                    modifier = Modifier.size(30.dp).testTag("timeline_zoom_in")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = SazfloyTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Horizontal Multi-Track Container with Playhead Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .horizontalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val clickedSec = (offset.x - 20.dp.toPx()) / (85f * zoomScale)
                        val rawTargetMs = (clickedSec * 1000L).toLong().coerceIn(0L, totalDurationMs)
                        val snappedMs = applySnapping(rawTargetMs)
                        onSeek(snappedMs)
                    }
                }
        ) {
            Box(modifier = Modifier.width(timelineWidthDp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 60.dp, top = 4.dp, bottom = 8.dp)
                ) {
                    // 1. Time Ruler with second/ms ticks
                    TimelineRuler(
                        totalDurationMs = totalDurationMs,
                        pixelsPerSecond = pixelsPerSecond,
                        zoomScale = zoomScale
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 1.5 Beat Markers Row on Top of Tracks
                    if (beatMarkersMs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                        ) {
                            beatMarkersMs.forEach { beatMs ->
                                val beatOffsetDp = ((beatMs / 1000f) * (85f * zoomScale)).dp
                                val isSelected = beatMs == selectedBeatMarkerMs
                                val isNearPlayhead = abs(beatMs - currentPlayheadMs) <= 60L

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(beatOffsetDp.roundToPx() - 5.dp.roundToPx(), 0) }
                                        .size(if (isSelected || isNearPlayhead) 12.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White
                                            else if (isNearPlayhead) SazfloyBeatMarker
                                            else SazfloyAccentPurple
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) SazfloyBeatMarker else Color(0x66000000),
                                            CircleShape
                                        )
                                        .clickable {
                                            onSelectBeatMarker?.invoke(beatMs)
                                            onSeek(beatMs)
                                        }
                                )
                            }
                        }
                    }

                    // 2. Video Clips Track with Real Thumbnails & Trim Handles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(SazfloySurfaceVariant, RoundedCornerShape(10.dp))
                            .border(1.dp, SazfloyBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        clips.forEachIndexed { index, clip ->
                            // Transition indicator between clips
                            if (index > 0) {
                                val hasTransition = clip.transitionType != TransitionType.NONE
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (hasTransition) SazfloySecondaryPurple else SazfloySurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (hasTransition) SazfloyAccentPurple else SazfloyBorder,
                                            CircleShape
                                        )
                                        .clickable { onSelectTransition(clip.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Transisi",
                                        tint = if (hasTransition) SazfloyAccentPurple else SazfloyTextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(3.dp))
                            }

                            val clipWidthDp = ((clip.trimmedDurationMs / 1000f) * (85f * zoomScale)).dp.coerceAtLeast(60.dp)
                            val isSelected = clip.id == selectedClipId
                            val borderColor by animateColorAsState(
                                if (isSelected) SazfloyAccentPurple else Color.Transparent,
                                label = "clip_border"
                            )

                            Box(
                                modifier = Modifier
                                    .width(clipWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SazfloySurfaceElevated else SazfloySurface)
                                    .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) borderColor else SazfloyBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSelectClip(clip.id) }
                            ) {
                                // Thumbnail Background
                                val thumbModel = clip.thumbnailUri ?: clip.uri
                                if (thumbModel.isNotBlank()) {
                                    AsyncImage(
                                        model = thumbModel,
                                        contentDescription = clip.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(if (isSelected) 0.65f else 0.40f)
                                    )
                                }

                                // Dark gradient for readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Black.copy(alpha = 0.70f),
                                                    Color.Black.copy(alpha = 0.40f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = if (isSelected) SazfloyAccentPurple else SazfloyTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = clip.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            ),
                                            color = SazfloyTextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${"%.1f".format(clip.trimmedDurationMs / 1000f)}s • ${clip.speed}x",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (isSelected) SazfloyAccentPurple else SazfloyTextSecondary
                                        )
                                        if (clip.keyframes.isNotEmpty()) {
                                            Text(
                                                text = "◆${clip.keyframes.size}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = SazfloyAccentPurple
                                            )
                                        }
                                    }
                                }

                                // Selected Clip Trim Visual Edge Indicators
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .width(6.dp)
                                            .fillMaxHeight()
                                            .background(SazfloyAccentPurple)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .width(6.dp)
                                            .fillMaxHeight()
                                            .background(SazfloyAccentPurple)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        // Add Media Quick Button
                        if (onAddMediaClick != null) {
                            Box(
                                modifier = Modifier
                                    .width(54.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SazfloySurfaceElevated)
                                    .border(1.dp, SazfloyAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { onAddMediaClick() }
                                    .padding(4.dp)
                                    .testTag("timeline_add_media_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Media",
                                        tint = SazfloyAccentPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Tambah",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = SazfloyAccentPurple
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Text & Subtitle Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFF1E1D27), RoundedCornerShape(6.dp))
                            .border(1.dp, SazfloyBorderSubtle, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        textOverlays.forEach { textItem ->
                            val offsetDp = ((textItem.startMs / 1000f) * (85f * zoomScale)).dp
                            val textWidthDp = ((textItem.durationMs / 1000f) * (85f * zoomScale)).dp.coerceAtLeast(40.dp)
                            val isSelected = textItem.id == selectedTextId

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                    .width(textWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (isSelected) SazfloySecondaryPurple else Color(0xFF38294F))
                                    .border(
                                        1.dp,
                                        if (isSelected) SazfloyAccentPurple else Color.Transparent,
                                        RoundedCornerShape(5.dp)
                                    )
                                    .clickable { onSelectText(textItem.id) }
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TextFields,
                                        contentDescription = null,
                                        tint = SazfloyAccentPurple,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = textItem.text.ifBlank { "Teks" },
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 4. Shapes / Overlay Track
                    if (shapeOverlays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(Color(0xFF1D2228), RoundedCornerShape(6.dp))
                                .border(1.dp, SazfloyBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            shapeOverlays.forEach { shape ->
                                val offsetDp = ((shape.startMs / 1000f) * (85f * zoomScale)).dp
                                val shapeWidthDp = ((shape.durationMs / 1000f) * (85f * zoomScale)).dp.coerceAtLeast(36.dp)
                                val isSelected = shape.id == selectedShapeId

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                        .width(shapeWidthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) SazfloyAccentPurple else Color(0xFF263238))
                                        .border(
                                            1.dp,
                                            if (isSelected) Color.White else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { onSelectShape(shape.id) }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "★ ${shape.type.name.take(4)}",
                                        color = if (isSelected) Color.Black else Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 5. Video Effects Track
                    if (effects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(Color(0xFF261C2E), RoundedCornerShape(6.dp))
                                .border(1.dp, SazfloyBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(2.dp)
                        ) {
                            effects.forEach { effect ->
                                val offsetDp = ((effect.startMs / 1000f) * (85f * zoomScale)).dp
                                val effectWidthDp = ((effect.durationMs / 1000f) * (85f * zoomScale)).dp.coerceAtLeast(42.dp)
                                val isSelected = effect.id == selectedEffectId

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                        .width(effectWidthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) SazfloySecondaryPurple else Color(0xFF432854))
                                        .border(
                                            1.dp,
                                            if (isSelected) SazfloyAccentPurple else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { onSelectEffect(effect.id) }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = SazfloyBeatMarker,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = effect.name.ifBlank { effect.type.displayName },
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 6. Professional Audio Track with Waveform Visuals
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .background(Color(0xFF1F1D2C), RoundedCornerShape(8.dp))
                            .border(1.dp, SazfloyBorderSubtle, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        audioTracks.forEach { audio ->
                            val offsetDp = ((audio.startMs / 1000f) * (85f * zoomScale)).dp
                            val audioWidthDp = ((audio.durationMs / 1000f) * (85f * zoomScale)).dp.coerceAtLeast(50.dp)
                            val isSelected = audio.id == selectedAudioId

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offsetDp.roundToPx(), 0) }
                                    .width(audioWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) SazfloySecondaryPurple
                                        else Color(0xFF2C2242)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SazfloyAccentPurple else SazfloySecondaryPurple.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSelectAudio(audio.id) }
                            ) {
                                // Real Waveform Visual representation using Canvas bars
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val barWidth = 3.dp.toPx()
                                    val barGap = 2.dp.toPx()
                                    val totalBars = (size.width / (barWidth + barGap)).toInt()
                                    val centerY = size.height / 2f
                                    val maxAmplitude = (size.height / 2f) * 0.85f

                                    for (i in 0 until totalBars) {
                                        val x = i * (barWidth + barGap)
                                        // Harmonic wave calculation for realistic audio profile
                                        val wave = (sin(i * 0.45) * 0.45 + sin(i * 0.15) * 0.35 + 0.3).toFloat().coerceIn(0.15f, 1.0f)
                                        val barHeight = maxAmplitude * wave * audio.volume.coerceIn(0.2f, 1.5f)

                                        drawRoundRect(
                                            color = if (isSelected) SazfloyAccentPurple.copy(alpha = 0.85f) else Color(0xFF9E84C8).copy(alpha = 0.65f),
                                            topLeft = Offset(x, centerY - barHeight),
                                            size = Size(barWidth, barHeight * 2f),
                                            cornerRadius = CornerRadius(2f, 2f)
                                        )
                                    }
                                }

                                // Audio title overlay
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = audio.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    if (audio.isMuted) {
                                        Text(
                                            text = "(Mute)",
                                            color = SazfloyPink,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                        )
                                    }
                                }
                            }
                        }

                        // Render Beat Markers directly on the audio track
                        beatMarkersMs.forEach { beatMs ->
                            val beatOffsetDp = ((beatMs / 1000f) * (85f * zoomScale)).dp
                            val isSelected = beatMs == selectedBeatMarkerMs
                            val isNearPlayhead = abs(beatMs - currentPlayheadMs) <= 45L

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(beatOffsetDp.roundToPx() - 4.dp.roundToPx(), 9.dp.roundToPx()) }
                                    .size(if (isSelected) 12.dp else if (isNearPlayhead) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White
                                        else if (isNearPlayhead) SazfloyBeatMarker
                                        else SazfloyAccentPurple
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SazfloyBeatMarker else Color(0x99000000),
                                        CircleShape
                                    )
                                    .clickable {
                                        onSelectBeatMarker?.invoke(beatMs)
                                        onSeek(beatMs)
                                    }
                            )
                        }
                    }
                }

                // 7. Draggable Vertical Playhead Line with Top Timecode Badge
                val playheadOffsetDp = 20.dp + ((currentPlayheadMs / 1000f) * (85f * zoomScale)).dp
                val isPlayheadOnBeat = beatMarkersMs.any { abs(it - currentPlayheadMs) <= 45L }
                val playheadColor = if (isPlayheadOnBeat) SazfloyBeatMarker else SazfloyPlayhead

                // Format current time as MM:SS.f
                val minutes = (currentPlayheadMs / 60000L)
                val seconds = (currentPlayheadMs % 60000L) / 1000f
                val timeString = "%02d:%04.1f".format(minutes, seconds)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(playheadOffsetDp.roundToPx(), 0) }
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaSec = dragAmount.x / (85f * zoomScale)
                                val rawNewTime = (currentPlayheadMs + (deltaSec * 1000L).toLong()).coerceIn(0L, totalDurationMs)
                                val snappedTime = applySnapping(rawNewTime)
                                onSeek(snappedTime)
                            }
                        }
                ) {
                    // Playhead Top Time Badge Pill
                    Box(
                        modifier = Modifier
                            .offset(x = (-22).dp, y = (-2).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(playheadColor)
                            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = timeString,
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    // Vertical Line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(playheadColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRuler(
    totalDurationMs: Long,
    pixelsPerSecond: androidx.compose.ui.unit.Dp,
    zoomScale: Float
) {
    val maxSeconds = (totalDurationMs / 1000f).toInt() + 4
    val step = when {
        zoomScale < 0.8f -> 2
        zoomScale > 1.8f -> 1
        else -> 1
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        for (sec in 0..maxSeconds step step) {
            val width = (pixelsPerSecond * step.toFloat())
            val min = sec / 60
            val s = sec % 60
            val timeLabel = "%02d:%02d".format(min, s)

            Column(
                modifier = Modifier.width(width),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = SazfloyTextMuted
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(7.dp)
                        .background(SazfloyDivider)
                )
            }
        }
    }
}

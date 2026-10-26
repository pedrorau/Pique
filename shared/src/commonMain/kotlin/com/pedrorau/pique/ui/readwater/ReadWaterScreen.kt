package com.pedrorau.pique.ui.readwater

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.ic_pause
import com.pedrorau.pique.resources.ic_play
import com.pedrorau.pique.resources.pozo_animation_description
import com.pedrorau.pique.resources.pozo_intro
import com.pedrorau.pique.resources.pozo_legend_1_body
import com.pedrorau.pique.resources.pozo_legend_1_title
import com.pedrorau.pique.resources.pozo_legend_2_body
import com.pedrorau.pique.resources.pozo_legend_2_title
import com.pedrorau.pique.resources.pozo_legend_3_body
import com.pedrorau.pique.resources.pozo_legend_3_title
import com.pedrorau.pique.resources.pozo_legend_4_body
import com.pedrorau.pique.resources.pozo_legend_4_title
import com.pedrorau.pique.resources.pozo_legend_5_body
import com.pedrorau.pique.resources.pozo_legend_5_title
import com.pedrorau.pique.resources.pozo_legend_6_body
import com.pedrorau.pique.resources.pozo_legend_6_title
import com.pedrorau.pique.resources.pozo_legend_7_body
import com.pedrorau.pique.resources.pozo_legend_7_title
import com.pedrorau.pique.resources.pozo_legend_title
import com.pedrorau.pique.resources.pozo_pause
import com.pedrorau.pique.resources.pozo_play
import com.pedrorau.pique.resources.pozo_rip_moderate
import com.pedrorau.pique.resources.pozo_rip_strength
import com.pedrorau.pique.resources.pozo_rip_strong
import com.pedrorau.pique.resources.pozo_rip_weak
import com.pedrorau.pique.resources.pozo_safety
import com.pedrorau.pique.resources.pozo_show_fish
import com.pedrorau.pique.resources.pozo_show_flow
import com.pedrorau.pique.resources.pozo_show_marks
import com.pedrorau.pique.resources.pozo_spot_body
import com.pedrorau.pique.resources.pozo_spot_title
import com.pedrorau.pique.resources.pozo_tide
import com.pedrorau.pique.resources.pozo_tide_high
import com.pedrorau.pique.resources.pozo_tide_low
import com.pedrorau.pique.resources.pozo_tide_mid
import com.pedrorau.pique.resources.pozo_tide_note_high
import com.pedrorau.pique.resources.pozo_tide_note_low
import com.pedrorau.pique.resources.pozo_tide_note_mid
import com.pedrorau.pique.resources.pozo_title
import com.pedrorau.pique.resources.pozo_waves
import com.pedrorau.pique.resources.pozo_waves_big
import com.pedrorau.pique.resources.pozo_waves_medium
import com.pedrorau.pique.resources.pozo_waves_small
import com.pedrorau.pique.resources.read_water_title
import com.pedrorau.pique.ui.components.PiqueTopBar
import com.pedrorau.pique.ui.theme.PiqueTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

private enum class Tide(val level: Float, val label: StringResource, val note: StringResource) {
    LOW(-0.3f, Res.string.pozo_tide_low, Res.string.pozo_tide_note_low),
    MID(0f, Res.string.pozo_tide_mid, Res.string.pozo_tide_note_mid),
    HIGH(0.3f, Res.string.pozo_tide_high, Res.string.pozo_tide_note_high)
}

private enum class Swell(val height: Float, val label: StringResource) {
    SMALL(0.7f, Res.string.pozo_waves_small),
    MEDIUM(1.2f, Res.string.pozo_waves_medium),
    BIG(1.8f, Res.string.pozo_waves_big)
}

private val legend = listOf(
    Res.string.pozo_legend_1_title to Res.string.pozo_legend_1_body,
    Res.string.pozo_legend_2_title to Res.string.pozo_legend_2_body,
    Res.string.pozo_legend_3_title to Res.string.pozo_legend_3_body,
    Res.string.pozo_legend_4_title to Res.string.pozo_legend_4_body,
    Res.string.pozo_legend_5_title to Res.string.pozo_legend_5_body,
    Res.string.pozo_legend_6_title to Res.string.pozo_legend_6_body,
    Res.string.pozo_legend_7_title to Res.string.pozo_legend_7_body
)

private const val HIGHLIGHT_MS = 3_000L

@Composable
fun ReadWaterScreen(onBack: () -> Unit) {
    var tide by rememberSaveable { mutableStateOf(Tide.LOW) }
    var swell by rememberSaveable { mutableStateOf(Swell.MEDIUM) }
    var playing by rememberSaveable { mutableStateOf(true) }
    var showFlow by rememberSaveable { mutableStateOf(true) }
    var showFish by rememberSaveable { mutableStateOf(true) }
    var showMarks by rememberSaveable { mutableStateOf(true) }
    var highlight by remember { mutableIntStateOf(0) }

    val sim = remember {
        RipSimulation().apply {
            tideTarget = tide.level
            waveHeight = swell.height
            prewarm()
        }
    }
    sim.tideTarget = tide.level
    sim.waveHeight = swell.height

    // Frame loop: the tide keeps easing while paused; only the simulation step stops.
    var frameNanos by remember { mutableLongStateOf(0L) }
    val isPlaying by rememberUpdatedState(playing)
    LaunchedEffect(sim) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else min(0.05f, (now - last) / 1_000_000_000f)
                last = now
                sim.updateTide(dt)
                if (isPlaying) sim.step(dt)
                frameNanos = now
            }
        }
    }
    LaunchedEffect(highlight) {
        if (highlight != 0) {
            delay(HIGHLIGHT_MS)
            highlight = 0
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PiqueTopBar(title = stringResource(Res.string.read_water_title), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(Res.string.pozo_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = stringResource(Res.string.pozo_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val animationDescription = stringResource(Res.string.pozo_animation_description)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(RipSimulation.W / RipSimulation.H)
                    .clip(MaterialTheme.shapes.medium)
                    .background(PiqueTheme.colors.seaDeep)
                    .semantics { contentDescription = animationDescription }
            ) {
                RipCurrentCanvas(
                    sim = sim,
                    frameNanos = { frameNanos },
                    showFlow = showFlow,
                    showFish = showFish,
                    showMarks = showMarks,
                    highlight = highlight,
                    modifier = Modifier.fillMaxSize()
                )
                FilledIconButton(
                    onClick = { playing = !playing },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(if (playing) Res.drawable.ic_pause else Res.drawable.ic_play),
                        contentDescription = stringResource(if (playing) Res.string.pozo_pause else Res.string.pozo_play)
                    )
                }
            }

            val strength = sim.ripStrength(tide.level)
            val strengthLabel = stringResource(
                when {
                    strength < 0.8f -> Res.string.pozo_rip_weak
                    strength < 1.3f -> Res.string.pozo_rip_moderate
                    else -> Res.string.pozo_rip_strong
                }
            )
            Text(
                text = stringResource(Res.string.pozo_rip_strength, strengthLabel),
                style = PiqueTheme.typography.data,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ChoiceRow(
                label = stringResource(Res.string.pozo_tide),
                options = Tide.entries,
                selected = tide,
                optionLabel = { stringResource(it.label) },
                onSelect = { tide = it }
            )
            Text(
                text = stringResource(tide.note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ChoiceRow(
                label = stringResource(Res.string.pozo_waves),
                options = Swell.entries,
                selected = swell,
                optionLabel = { stringResource(it.label) },
                onSelect = { swell = it }
            )

            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showFlow,
                    onClick = { showFlow = !showFlow },
                    label = { Text(stringResource(Res.string.pozo_show_flow)) },
                    colors = chipColors
                )
                FilterChip(
                    selected = showFish,
                    onClick = { showFish = !showFish },
                    label = { Text(stringResource(Res.string.pozo_show_fish)) },
                    colors = chipColors
                )
                FilterChip(
                    selected = showMarks,
                    onClick = { showMarks = !showMarks },
                    label = { Text(stringResource(Res.string.pozo_show_marks)) },
                    colors = chipColors
                )
            }

            Text(
                text = stringResource(Res.string.pozo_legend_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                legend.forEachIndexed { index, (title, body) ->
                    LegendItem(
                        number = index + 1,
                        title = stringResource(title),
                        body = stringResource(body),
                        selected = highlight == index + 1,
                        onClick = { highlight = index + 1 }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = stringResource(Res.string.pozo_spot_title), style = MaterialTheme.typography.titleLarge)
                Text(text = stringResource(Res.string.pozo_spot_body), style = MaterialTheme.typography.bodyLarge)
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Text(
                    text = stringResource(Res.string.pozo_safety),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    icon = {}
                ) {
                    Text(optionLabel(option), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    number: Int,
    title: String,
    body: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isTarget = number == TARGET_MARK
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    if (isTarget) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = PiqueTheme.typography.data,
                fontWeight = FontWeight.Medium,
                color = if (isTarget) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.surface
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

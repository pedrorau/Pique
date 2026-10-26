package com.pedrorau.pique.ui.knots

import com.pedrorau.pique.knots.Knot
import com.pedrorau.pique.knots.KnotFrames
import com.pedrorau.pique.knots.KnotRepository
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.ic_back
import com.pedrorau.pique.resources.ic_chevron_right
import com.pedrorau.pique.resources.ic_pause
import com.pedrorau.pique.resources.ic_play
import com.pedrorau.pique.resources.knot_animation_description
import com.pedrorau.pique.resources.knot_next_step
import com.pedrorau.pique.resources.knot_pause
import com.pedrorau.pique.resources.knot_play
import com.pedrorau.pique.resources.knot_previous_step
import com.pedrorau.pique.resources.knot_replay
import com.pedrorau.pique.resources.knot_step_of
import com.pedrorau.pique.resources.nav_knots
import com.pedrorau.pique.ui.components.PiqueTopBar
import com.pedrorau.pique.ui.theme.PiqueTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MORPH_MS = 1_400
private const val HOLD_MS = 2_200L

@Composable
fun KnotDetailScreen(knotId: String, onBack: () -> Unit) {
    val knot by produceState<Knot?>(initialValue = null, knotId) { value = KnotRepository.byId(knotId) }

    Column(modifier = Modifier.fillMaxSize()) {
        PiqueTopBar(title = stringResource(Res.string.nav_knots), onBack = onBack)
        val loaded = knot
        if (loaded == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            KnotDetailContent(loaded)
        }
    }
}

@Composable
private fun KnotDetailContent(knot: Knot) {
    val frames = remember(knot) { KnotFrames(knot) }
    val lastStep = knot.steps.lastIndex
    var step by rememberSaveable(knot.id) { mutableIntStateOf(0) }
    var fromStep by remember { mutableIntStateOf(step) }
    var playing by rememberSaveable(knot.id) { mutableStateOf(false) }
    val morph = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    fun goTo(target: Int) {
        val clamped = target.coerceIn(0, lastStep)
        if (clamped == step) return
        // Start the morph from wherever the drawing is now.
        fromStep = if (morph.value >= 0.5f) step else fromStep
        step = clamped
        scope.launch {
            morph.snapTo(0f)
            morph.animateTo(1f, tween(MORPH_MS, easing = FastOutSlowInEasing))
        }
    }

    // Playing advances one step after each morph and hold, and stops at the finished knot.
    LaunchedEffect(playing) {
        while (playing) {
            if (step == lastStep) {
                playing = false
                break
            }
            delay(if (morph.value < 1f) MORPH_MS.toLong() else 0L)
            delay(HOLD_MS)
            if (playing) goTo(step + 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = knot.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = knot.name, style = MaterialTheme.typography.headlineMedium)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(knot.difficulty, knot.strength, knot.lines).forEach { InfoChip(it) }
        }

        val description = stringResource(Res.string.knot_animation_description, knot.name, step + 1, knot.steps.size)
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = description }
        ) {
            KnotCanvas(
                frames = frames,
                from = fromStep,
                to = step,
                progress = { morph.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(KNOT_BOX_WIDTH / KNOT_BOX_HEIGHT)
                    .padding(8.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.knot_step_of, step + 1, knot.steps.size),
                style = PiqueTheme.typography.data
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                knot.steps.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
            Text(
                text = knot.steps[step].text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.heightIn(min = 78.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedIconButton(
                onClick = { playing = false; goTo(step - 1) },
                enabled = step > 0,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(painterResource(Res.drawable.ic_back), contentDescription = stringResource(Res.string.knot_previous_step))
            }
            val atEnd = step == lastStep
            Button(
                onClick = {
                    if (atEnd && !playing) {
                        fromStep = lastStep
                        step = 0
                        scope.launch { morph.snapTo(1f) }
                        playing = true
                    } else {
                        playing = !playing
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(
                    painter = painterResource(if (playing) Res.drawable.ic_pause else Res.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(
                        when {
                            playing -> Res.string.knot_pause
                            atEnd -> Res.string.knot_replay
                            else -> Res.string.knot_play
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            OutlinedIconButton(
                onClick = { playing = false; goTo(step + 1) },
                enabled = step < lastStep,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(painterResource(Res.drawable.ic_chevron_right), contentDescription = stringResource(Res.string.knot_next_step))
            }
        }

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            Text(
                text = knot.tip,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

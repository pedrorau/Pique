package com.pedrorau.pique.ui.knots

import com.pedrorau.pique.knots.Knot
import com.pedrorau.pique.knots.KnotFrames
import com.pedrorau.pique.knots.KnotRepository
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.ic_chevron_right
import com.pedrorau.pique.resources.knots_intro
import com.pedrorau.pique.resources.nav_knots
import com.pedrorau.pique.ui.components.PiqueTopBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun KnotsScreen(onOpenKnot: (String) -> Unit) {
    val knots by produceState<List<Knot>?>(initialValue = null) { value = KnotRepository.all() }

    Column(modifier = Modifier.fillMaxSize()) {
        PiqueTopBar(title = stringResource(Res.string.nav_knots))
        val list = knots
        if (list == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(Res.string.knots_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(list, key = { it.id }) { knot ->
                KnotRow(knot = knot, onClick = { onOpenKnot(knot.id) })
            }
        }
    }
}

@Composable
private fun KnotRow(knot: Knot, onClick: () -> Unit) {
    val frames = remember(knot) { KnotFrames(knot) }
    val last = knot.steps.lastIndex
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail: the finished knot.
            KnotCanvas(
                frames = frames,
                from = last,
                to = last,
                progress = { 1f },
                accent = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(KNOT_BOX_WIDTH / KNOT_BOX_HEIGHT)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = knot.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = knot.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = knot.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

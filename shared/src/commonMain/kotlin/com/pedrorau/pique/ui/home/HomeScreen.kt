package com.pedrorau.pique.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.app_name
import com.pedrorau.pique.resources.home_featured_body
import com.pedrorau.pique.resources.home_featured_overline
import com.pedrorau.pique.resources.home_featured_title
import com.pedrorau.pique.resources.home_overline
import com.pedrorau.pique.resources.home_question
import com.pedrorau.pique.resources.home_section_baits
import com.pedrorau.pique.resources.home_section_knots_subtitle
import com.pedrorau.pique.resources.home_section_modalities
import com.pedrorau.pique.resources.home_section_modalities_subtitle
import com.pedrorau.pique.resources.home_section_next_version
import com.pedrorau.pique.resources.home_section_rigs
import com.pedrorau.pique.resources.home_section_safety
import com.pedrorau.pique.resources.home_section_safety_subtitle
import com.pedrorau.pique.resources.home_section_species_subtitle
import com.pedrorau.pique.resources.ic_fish
import com.pedrorau.pique.resources.ic_knot
import com.pedrorau.pique.resources.ic_rod
import com.pedrorau.pique.resources.ic_shield
import com.pedrorau.pique.resources.nav_knots
import com.pedrorau.pique.resources.nav_species
import com.pedrorau.pique.ui.theme.PiqueTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onOpenReadWater: () -> Unit,
    onOpenKnots: () -> Unit,
    onOpenModalities: (() -> Unit)?,
    onOpenSpecies: (() -> Unit)?,
    onOpenSafety: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.home_overline),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(Res.string.home_question),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        FeaturedReadWaterCard(onClick = onOpenReadWater)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionRow {
                SectionCard(
                    title = stringResource(Res.string.nav_knots),
                    subtitle = stringResource(Res.string.home_section_knots_subtitle),
                    icon = Res.drawable.ic_knot,
                    onClick = onOpenKnots,
                    modifier = Modifier.weight(1f)
                )
                SectionCard(
                    title = stringResource(Res.string.home_section_modalities),
                    subtitle = stringResource(Res.string.home_section_modalities_subtitle),
                    icon = Res.drawable.ic_rod,
                    onClick = onOpenModalities,
                    modifier = Modifier.weight(1f)
                )
            }
            SectionRow {
                SectionCard(
                    title = stringResource(Res.string.nav_species),
                    subtitle = stringResource(Res.string.home_section_species_subtitle),
                    icon = Res.drawable.ic_fish,
                    onClick = onOpenSpecies,
                    modifier = Modifier.weight(1f)
                )
                SectionCard(
                    title = stringResource(Res.string.home_section_safety),
                    subtitle = stringResource(Res.string.home_section_safety_subtitle),
                    icon = Res.drawable.ic_shield,
                    onClick = onOpenSafety,
                    modifier = Modifier.weight(1f)
                )
            }
            SectionRow {
                UpcomingSectionCard(
                    title = stringResource(Res.string.home_section_rigs),
                    modifier = Modifier.weight(1f)
                )
                UpcomingSectionCard(
                    title = stringResource(Res.string.home_section_baits),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FeaturedReadWaterCard(onClick: () -> Unit) {
    val colors = PiqueTheme.colors
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = colors.seaDeep,
        contentColor = colors.foam,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            BeachThumbnail(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            )
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.home_featured_overline),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.foam.copy(alpha = 0.8f)
                )
                Text(
                    text = stringResource(Res.string.home_featured_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(Res.string.home_featured_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.foam.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Cards in a row share the tallest card's height.
@Composable
private fun SectionRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/** A section; with a null [onClick] it isn't released yet and shows as upcoming. */
@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    icon: DrawableResource,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (onClick == null) {
        UpcomingSectionCard(title = title, modifier = modifier)
        return
    }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpcomingSectionCard(title: String, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(Res.string.home_section_next_version),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

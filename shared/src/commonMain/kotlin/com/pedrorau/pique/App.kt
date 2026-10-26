package com.pedrorau.pique

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pedrorau.pique.resources.Res
import com.pedrorau.pique.resources.app_name
import com.pedrorau.pique.resources.home_overline
import com.pedrorau.pique.resources.home_status
import com.pedrorau.pique.resources.home_tagline
import com.pedrorau.pique.ui.theme.PiqueTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                text = stringResource(Res.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(Res.string.home_status),
                style = PiqueTheme.typography.data,
                color = PiqueTheme.colors.accent
            )
        }
    }
}

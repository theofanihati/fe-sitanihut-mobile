package com.dishut_lampung.sitanihut.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import androidx.compose.runtime.getValue
import com.dishut_lampung.sitanihut.presentation.shared.theme.Dimens.ScreenPadding

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        selectedNetwork = SyncNetworkType.ANY_NETWORK,
        onNetworkChanged = {}
    )
}

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedNetwork by viewModel.networkPreference.collectAsState()
    SettingsScreen(
        selectedNetwork = selectedNetwork,
        onNetworkChanged = viewModel::onNetworkPreferenceChanged
    )
}

@Composable
fun SettingsScreen(
    selectedNetwork: SyncNetworkType,
    onNetworkChanged: (SyncNetworkType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = ScreenPadding, vertical = 100.dp)
    ) {
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sinkronisasi Data",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedNetwork == SyncNetworkType.ANY_NETWORK,
                onClick = { onNetworkChanged(SyncNetworkType.ANY_NETWORK) }
            )
            Text(text = "Semua Jaringan (Data Seluler & Wi-Fi)")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedNetwork == SyncNetworkType.WIFI_ONLY,
                onClick = { onNetworkChanged(SyncNetworkType.WIFI_ONLY) }
            )
            Text(text = "Hanya Wi-Fi (Hemat Kuota)")
        }
    }
}
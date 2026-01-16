package com.dishut_lampung.sitanihut.presentation.shared.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dishut_lampung.sitanihut.domain.model.ReportSummary

enum class SummaryRole {
    PETANI,
    PENYULUH,
    PENANGGUNG_JAWAB
}

@Composable
fun ReportSummaryCard(
    modifier: Modifier = Modifier,
    summary: ReportSummary,
    role: SummaryRole,
) {
    val (
        displayPending, displayVerified, displayApproved, displayRejected,
        showPending, showVerified, showApproved, showRejected
    ) = remember(summary, role)
    {
        when (role) {
            SummaryRole.PETANI -> {
                SummaryStatus(
                    Menunggu = summary.pendingCount + summary.verifiedcount,
                    Diverifikasi = 0,
                    Disetujui = summary.approvedCount,
                    Ditolak = summary.rejectedCount,
                    showPending = true,
                    showVerified = false,
                    showApproved = true,
                    ShowRejected = true,
                )
            }
            SummaryRole.PENYULUH -> {
                SummaryStatus(
                    Menunggu = summary.pendingCount,
                    Diverifikasi = summary.verifiedcount,
                    Disetujui = 0,
                    Ditolak = 0,
                    showPending = true,
                    showVerified = true,
                    showApproved = false,
                    ShowRejected = false,
                )
            }
            SummaryRole.PENANGGUNG_JAWAB -> {
                SummaryStatus(
                    Menunggu = 0,
                    Diverifikasi = summary.verifiedcount,
                    Disetujui = summary.approvedCount,
                    Ditolak = 0,
                    showPending = false,
                    showVerified = true,
                    showApproved = true,
                    ShowRejected = false
                )
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            . padding(horizontal = 40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showPending) {
                SummaryItem(
                    count = displayPending,
                    label = if (role == SummaryRole.PETANI) "Diproses" else "Menunggu",
                    countColor = Color.White
                )
            }

            if (showVerified) {
                SummaryItem(
                    count = displayVerified,
                    label = if (role == SummaryRole.PENYULUH) "Terverifikasi" else "Diverifikasi penyuluh" ,
                    countColor = Color.White
                )
            }

            if (showApproved) {
                SummaryDivider()
                SummaryItem(
                    count = displayApproved,
                    label = "Disetujui",
                    countColor = Color.White
                )
            }

            if (showRejected) {
                SummaryDivider()
                SummaryItem(
                    count = displayRejected,
                    label = "Ditolak",
                    countColor = Color.White
                )
            }
        }
    }
}

private data class SummaryStatus<A, B, C, D, E, F, G, H>(
    val Menunggu: A,
    val Diverifikasi: B,
    val Disetujui: C,
    val Ditolak: D,
    val showPending: E,
    val showVerified: F,
    val showApproved: G,
    val ShowRejected: H,
)

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    countColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = countColor
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.8f))
    )
}
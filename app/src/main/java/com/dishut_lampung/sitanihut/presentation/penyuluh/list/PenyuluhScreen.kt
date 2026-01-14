package com.dishut_lampung.sitanihut.presentation.penyuluh.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.card.PenyuluhCard
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomSearchTextField
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhEvent
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhUiState
import com.dishut_lampung.sitanihut.presentation.ui.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.ui.theme.SitanihutTheme
import kotlinx.coroutines.launch

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PenyuluhScreenPreview() {
    SitanihutTheme {
        val dummyPenyuluh = listOf(
            Penyuluh(
                id = "1",
                name = "Ahmad Ganteng",
                identityNumber = "198801012014021001",
                position = "Penyuluh Ahli Muda",
                gender = "pria",
                kphId = "1",
                kphName = "KPH Batutegi"
            ),
            Penyuluh(
                id = "2",
                name = "Ani Sirani",
                identityNumber = "199002022015032002",
                position = "Penyuluh Terampil",
                gender = "wanita",
                kphId = "1",
                kphName = "KPH Batutegi"
            )
        )

        val state = PenyuluhUiState(
            isLoading = false,
            penyuluhList = dummyPenyuluh
        )

        PenyuluhScreen(
            state = state,
            onEvent = {},
            onNavigateToDetail = {},
            onRefresh = {}
        )
    }
}

@Composable
fun PenyuluhRoute(
    viewModel: PenyuluhViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PenyuluhScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        onRefresh = {
            viewModel.onEvent(PenyuluhEvent.OnRefresh)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenyuluhScreen(
    state: PenyuluhUiState,
    modifier: Modifier = Modifier,
    onEvent: (PenyuluhEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isRefreshing = state.isRefreshing

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ScreenPadding)
            ) {

                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    text = "Data Penyuluh",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomSearchTextField(
                        modifier = Modifier.weight(1f),
                        query = state.searchQuery,
                        onQueryChange = { onEvent(PenyuluhEvent.OnSearchQueryChange(it)) },
                        placeholder = "Cari penyuluh..."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!state.isLoading && state.penyuluhList.isEmpty()) {
                        item {
                            EmptyPenyuluhStateView()
                        }
                    }

                    items(
                        items = state.penyuluhList,
                        key = { it.id }
                    ) { penyuluh ->
                        PenyuluhCard(
                            item = penyuluh,
                            onClick = onNavigateToDetail
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 92.dp, end = 28.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Ke Atas")
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CustomCircularProgressIndicator()
            }
        }

        if (state.error != null) {
            AnimatedMessage(
                isVisible = state.error != null,
                message = state.error ?: "",
                messageType = MessageType.Error,
                onDismiss = { onEvent(PenyuluhEvent.OnDismissError) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 80.dp)
            )
        }
    }
}

@Composable
fun EmptyPenyuluhStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_data_image),
            contentDescription = "Kosong",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada data penyuluh",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Data penyuluh akan tampil disini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
package com.dishut_lampung.sitanihut.presentation.commodity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.presentation.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.components.card.CommodityCard
import com.dishut_lampung.sitanihut.presentation.components.textfield.CustomSearchTextField
import com.dishut_lampung.sitanihut.presentation.shared.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.shared.theme.SitanihutTheme
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CommodityScreenPreview() {
    SitanihutTheme {
        val dummyCommodity = listOf(
            Commodity(
                id = "1",
                code = "JG01",
                name = "Jagung",
                category = "buah buahan"
            ),
            Commodity(
                id = "2",
                code = "PD01",
                name = "Padi",
                category = "pangan"
            )
        )

        val state = CommodityUiState(
            query = "",
            isLoading = false,
            items = dummyCommodity
        )

        CommodityScreen(
            state = state,
            onEvent = {},
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun EmptyStatePreview() {
//    SitanihutTheme {
//        CommodityScreen(
//            state = CommodityUiState(),
//            onEvent = {},
//        )
//    }
//}

@Composable
fun CommodityRoute(
    modifier: Modifier = Modifier,
    viewModel: CommodityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CommodityScreen(
        state = state,
        modifier = modifier,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun CommodityScreen(
    state: CommodityUiState,
    modifier: Modifier = Modifier,
    onEvent: (CommodityEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ScreenPadding)
        ) {

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "Data Komoditas",
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
                    query = state.query,
                    onQueryChange = { onEvent(CommodityEvent.OnSearchQueryChange(it)) },
                    placeholder = "Cari komoditas..."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (!state.isLoading && state.items.isEmpty()) {
                    item {
                        EmptyStateView()
                    }
                }

                items(
                    items = state.items,
                    key = { it.id }
                ) { commodity ->
                    CommodityCard(
                        item = commodity,
                        onClick = {}

                    )
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

        AnimatedMessage(
            isVisible = state.successMessage != null,
            message = state.successMessage ?: "",
            messageType = MessageType.Success,
            onDismiss = { onEvent(CommodityEvent.OnDismissSuccessMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )

        AnimatedMessage(
            isVisible = state.errorMessage != null,
            message = state.errorMessage ?: "",
            messageType = MessageType.Error,
            onDismiss = { onEvent(CommodityEvent.OnDismissError) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )
    }
}

@Composable
fun EmptyStateView() {
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
            text = "Belum ada data komoditas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tunggu ya, kalau sudah ada akan tampil disini!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
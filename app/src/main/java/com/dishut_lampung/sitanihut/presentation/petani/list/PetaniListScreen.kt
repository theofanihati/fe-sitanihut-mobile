package com.dishut_lampung.sitanihut.presentation.petani.list

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dishut_lampung.sitanihut.R
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.presentation.shared.components.CustomCircularProgressIndicator
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.AnimatedMessage
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.shared.components.bottomsheet.GenericActionBottomSheet
import com.dishut_lampung.sitanihut.presentation.shared.components.card.PetaniCard
import com.dishut_lampung.sitanihut.presentation.shared.components.dialog.CustomConfirmationDialog
import com.dishut_lampung.sitanihut.presentation.shared.components.textfield.CustomSearchTextField
import com.dishut_lampung.sitanihut.presentation.shared.theme.Dimens.ScreenPadding
import com.dishut_lampung.sitanihut.presentation.shared.theme.SitanihutTheme
import com.dishut_lampung.sitanihut.util.openFileByPath
import kotlinx.coroutines.launch

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PetaniListScreenPreview() {
    SitanihutTheme {
        val dummyPetani = listOf(
            Petani(
                id = "1", name = "Petani Cowo", identityNumber = "1871000000", kphName = "KPH Batutegi", kthName = "KTH Makmur"
            ),
            Petani(
                id = "2", name = "Petani Cewe", identityNumber = "1872000000", kphName = "KPH Batutegi", kthName = "KTH Sejahtera"
            )
        )

        val state = PetaniListUiState(
            query = "",
            isLoading = false,
            petaniList = dummyPetani,
            userRole = "penyuluh"
        )

        PetaniListScreen(
            state = state,
            onEvent = {},
            onRefresh = {},
            onNavigateToDetail = {},
            onNavigateToEdit = {},
            onNavigateToAddPetani = {}
        )
    }
}

@Composable
fun PetaniListRoute(
    modifier: Modifier = Modifier,
    viewModel: PetaniListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddPetani: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(PetaniEvent.OnRefresh)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
// =============== klo export dari BACK END ====================
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            if (msg.contains("File tersimpan:")) {
                val path = msg.substringAfter("File tersimpan: ").trim()
                openFileByPath(context, path) { message, type ->
                    }
            }
        }
    }

    PetaniListScreen(
        state = state,
        modifier = modifier,
        onEvent = viewModel::onEvent,
        onRefresh = { viewModel.onEvent(PetaniEvent.OnRefresh) },
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToAddPetani = onNavigateToAddPetani,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaniListScreen(
    state: PetaniListUiState,
    modifier: Modifier = Modifier,
    onEvent: (PetaniEvent) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddPetani: () -> Unit,
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
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ScreenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Data Petani",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onEvent(PetaniEvent.OnExportList) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Data Petani",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(visible = !state.isOnline) {
                    Text(
                        text = "Mode Offline (Data Lokal)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomSearchTextField(
                        modifier = Modifier.weight(1f),
                        query = state.query,
                        onQueryChange = { onEvent(PetaniEvent.OnSearchQueryChange(it)) },
                        placeholder = "Cari Petani..."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!state.isLoading && state.petaniList.isEmpty()) {
                        item { EmptyStatePetaniView() }
                    }

                    items(
                        items = state.petaniList,
                        key = { it.id }
                    ) { petani ->
                        PetaniCard(
                            item = petani,
                            isOnline = state.isOnline,
                            onClick = { onNavigateToDetail(petani.id) },
                            onActionClick = { onEvent(PetaniEvent.OnMoreOptionClick(petani.id)) }
                        )
                    }
                }
            }
        }

        if (state.isBottomSheetVisible) {
            GenericActionBottomSheet(
                onDismiss = { onEvent(PetaniEvent.OnBottomSheetDismiss) },
                onDetailClick = {
                    onEvent(PetaniEvent.OnBottomSheetDismiss)
                    state.selectedPetaniId?.let { onNavigateToDetail(it) }
                },
                onEditClick = {
                    onEvent(PetaniEvent.OnBottomSheetDismiss)
                    state.selectedPetaniId?.let { onNavigateToEdit(it) }
                },
                onExportClick = {
                    onEvent(PetaniEvent.OnExportDetail)
                },
                onDeleteClick = {
                    if (state.isOnline && state.userRole != "penanggung jawab") {
                        onEvent(PetaniEvent.OnDeleteClick)
                    }
                },
                isEditable = state.isOnline && state.userRole != "penanggung jawab",
            )
        }

        if (state.isDeleteDialogVisible) {
            CustomConfirmationDialog(
                title = "Hapus Data Petani?",
                supportingText = "Data yang dihapus tidak dapat dikembalikan.",
                confirmButtonText = "Hapus",
                dismissButtonText = "Batal",
                onConfirm = { onEvent(PetaniEvent.OnDeleteConfirm) },
                onDismiss = { onEvent(PetaniEvent.OnDismissDeleteDialog) },
                confirmColor = MaterialTheme.colorScheme.error
            )
        }

        if (state.userRole != "penanggung jawab") {
            FloatingActionButton(
                onClick = {
                    if (state.isOnline) {
                        onNavigateToAddPetani()
                    } else {
                        onEvent(
                            PetaniEvent.OnShowUserMessage(
                                "Mode Offline: Tidak bisa menambah data",
                                MessageType.Error
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Petani")
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
            onDismiss = { onEvent(PetaniEvent.OnDismissSuccessMessage) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )

        AnimatedMessage(
            isVisible = state.errorMessage != null,
            message = state.errorMessage ?: "",
            messageType = MessageType.Error,
            onDismiss = { onEvent(PetaniEvent.OnDismissError) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 80.dp)
        )
    }
}

@Composable
fun EmptyStatePetaniView() {
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
            text = "Belum ada data Petani",
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
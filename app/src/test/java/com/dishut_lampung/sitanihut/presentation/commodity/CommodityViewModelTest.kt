package com.dishut_lampung.sitanihut.presentation.commodity

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.usecase.commodity.GetCommoditiesUseCase
import com.dishut_lampung.sitanihut.domain.usecase.commodity.SyncCommodityDataUseCase
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhEvent
import com.dishut_lampung.sitanihut.presentation.penyuluh.list.PenyuluhViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommodityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    private val useCase: GetCommoditiesUseCase = mockk(relaxed = true)
    private val syncUseCase: SyncCommodityDataUseCase = mockk(relaxed = true)
    private var connectivityObserver: ConnectivityObserver = mockk(relaxed = true)
    private lateinit var viewModel: CommodityViewModel

    private val dummyCommodities = listOf(
        Commodity("1", "JG01", "Jagung", "buah buahan"),
        Commodity("2", "PD01", "Padi", "biji bijian")
    )

    @Test
    fun `init should load commodities with empty query and update state to Success`() = runTest {
        coEvery { useCase("") } returns flowOf(Resource.Success(dummyCommodities))
        viewModel = CommodityViewModel(useCase, syncUseCase, connectivityObserver)

        viewModel.uiState.test {
            val item = awaitItem()
            assertFalse(item.isLoading)
            assertEquals(dummyCommodities, item.items)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { useCase("") }
    }

    @Test
    fun `onSearch should call useCase with correct query`() = runTest {
        val query = "Jagung"
        val searchResult = listOf(dummyCommodities[0])

        coEvery { useCase("") } returns flowOf(Resource.Success(dummyCommodities))
        coEvery { useCase(query) } returns flow {
            emit(Resource.Loading())
            delay(10)
            emit(Resource.Success(searchResult))
        }
        viewModel = CommodityViewModel(useCase, syncUseCase, connectivityObserver)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(CommodityEvent.OnSearchQueryChange(query))

            val queryUpdateItem = awaitItem()
            assertEquals(query, queryUpdateItem.query)
            assertFalse(queryUpdateItem.isLoading)

            val loadingItem = awaitItem()
            assertTrue(loadingItem.isLoading)

            val resultItem = awaitItem()
            assertFalse(resultItem.isLoading)
            assertEquals(searchResult, resultItem.items)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { useCase(query) }
    }

    @Test
    fun `when useCase returns Error, state should show error message`() = runTest {
        val errorMessage = "No Internet Connection"
        coEvery { useCase("") } returns flowOf(Resource.Error(errorMessage))
        viewModel = CommodityViewModel(useCase, syncUseCase, connectivityObserver)

        viewModel.uiState.test {
            val item = awaitItem()
            assertFalse(item.isLoading)
            assertEquals(errorMessage, item.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent OnRefresh should refresh data correctly`() = runTest {
        val dummyList = listOf(
            Commodity("1", "123","Jagung",  "Buah"),
            Commodity("2", "124","Nangka",  "Buah"),
        )
        every { useCase("") } returns flowOf(Resource.Success(dummyList))

        viewModel = CommodityViewModel(useCase, syncUseCase, connectivityObserver)
        advanceUntilIdle()

        viewModel.onEvent(CommodityEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertFalse(state.isLoading)
        assertEquals(2, state.items.size)

        coVerify(exactly = 1) { useCase("") }
        coVerify(exactly = 1) { syncUseCase() }
    }
}
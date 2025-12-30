package com.dishut_lampung.sitanihut.presentation.kth

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.presentation.kth.list.KthListViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KthListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getKthListUseCase: GetKthListUseCase
    private lateinit var userPreferences: UserPreferences
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var viewModel: KthListViewModel

    @Before
    fun setUp() {
        getKthListUseCase = mockk()
        userPreferences = mockk()
        connectivityObserver = mockk()
    }

    @Test
    fun `init should load KTH list and observe connectivity`() = runTest {
        val dummyKth = listOf(Kth("1", "KTH Mawar", "Desa A", "Kec A","Kab A", "Koor A", "089785983784", "KPH A"),)

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase("penyuluh", "") } returns flowOf(Resource.Success(dummyKth))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = KthListViewModel(getKthListUseCase, userPreferences, connectivityObserver)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(dummyKth, state.kthList)
            assertTrue(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when connectivity is lost, isOnline should be false`() = runTest {
        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        viewModel = KthListViewModel(getKthListUseCase, userPreferences, connectivityObserver)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isOnline)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search should filter data locally`() = runTest {
        val listData = listOf(
            Kth("1", "KTH Mawar", "Desa A", "Kec A","Kab A", "Koor A", "089785983784", "KPH A"),
            Kth("2", "KTH Melati", "Desa B", "Kec A","Kab B", "Koor A", "088877278837",  "KPH B")
        )

        every { userPreferences.userRole } returns flowOf("penyuluh")
        every { getKthListUseCase("penyuluh", "") } returns flowOf(Resource.Success(listData))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        viewModel = KthListViewModel(getKthListUseCase, userPreferences, connectivityObserver)

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(KthEvent.OnSearchQueryChange("Mawar"))

            val filteredState = awaitItem()
            assertEquals(1, filteredState.kthList.size)
            assertEquals("KTH Mawar", filteredState.kthList.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
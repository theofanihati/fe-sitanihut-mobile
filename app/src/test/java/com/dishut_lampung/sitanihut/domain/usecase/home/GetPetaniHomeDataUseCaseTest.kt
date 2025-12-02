package com.dishut_lampung.sitanihut.domain.usecase.home

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPetaniHomeDataUseCaseTest {
    private lateinit var getPetaniHomeDataUseCase: GetPetaniHomeDataUseCase
    private val mockHomeRepository: HomeRepository = mockk<HomeRepository>(relaxed = true)

    @Before
    fun setUp() {
        getPetaniHomeDataUseCase = GetPetaniHomeDataUseCase(mockHomeRepository)
    }

    @Test
    fun `invoke should combine all data from repository and emit single FarmerHomeData`() = runTest {
        val expectedProfile = UserProfile("Budi Santoso", "Petani", "http://example.com/pic.jpg")
        val expectedSummary = ReportSummary(pendingCount = 5, approvedCount = 10, verifiedcount = 2, rejectedCount = 2)
        val expectedReports = listOf(
            Report(
                id = "1",
                period = 2025,
                monthPeriod = "Mei",
                submissionDate = "25-05-2025",
                totalTransaction = 8000.0,
                status = ReportStatus.PENDING
            )
        )

        every { mockHomeRepository.getUserProfile() } returns flowOf(expectedProfile)
        every { mockHomeRepository.getReportSummary() } returns flowOf(expectedSummary)
        every { mockHomeRepository.getLatestReports() } returns flowOf(expectedReports)

        val resultFlow = getPetaniHomeDataUseCase()

        resultFlow.test {
            val actualEmission = awaitItem()

            assertEquals(expectedProfile, actualEmission.userProfile)
            assertEquals(expectedSummary, actualEmission.summary)
            assertEquals(expectedReports, actualEmission.latestReports)

//            assertTrue("Report list should not be empty", actualEmission.latestReports.isNotEmpty())
//            assertEquals(1, actualEmission.latestReports.size)

            awaitComplete()
        }
    }
}

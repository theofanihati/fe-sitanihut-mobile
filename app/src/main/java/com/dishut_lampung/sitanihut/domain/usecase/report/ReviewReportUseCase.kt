package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ReviewReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(
        reportId: String,
        newStatus: ReportStatus,
        userRole: String
    ): Resource<Unit> {
        if (userRole.equals("petani", ignoreCase = true)) {
            return Resource.Error("Anda tidak memiliki akses")
        }
        val reportResult = repository.getReportById(reportId).firstOrNull()

        if (reportResult is Resource.Success && reportResult.data != null) {
            val currentStatus = reportResult.data.status

            if (userRole.equals("penanggung jawab", ignoreCase = true) &&
                currentStatus.equals(ReportStatus.APPROVED) &&
                newStatus.equals(ReportStatus.APPROVED)
            ) {
                return Resource.Error("Laporan harus diverifikasi Penyuluh terlebih dahulu")
            }
            return repository.updateReportStatus(reportId, newStatus)
        } else if (reportResult is Resource.Error) {
            return Resource.Error(reportResult.message ?: "Data tidak ditemukan")
        }

        return Resource.Error("Gagal memuat data laporan")
    }
}
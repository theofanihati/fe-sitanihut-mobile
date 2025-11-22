package com.dishut_lampung.sitanihut.domain.usecase.information

import com.dishut_lampung.sitanihut.domain.repository.CompanyRepository
import javax.inject.Inject

class DownloadStructureImageUseCase @Inject constructor(
    private val repository: CompanyRepository
) {
    suspend operator fun invoke(): Result<String> {
        return repository.saveStructureImageToGallery()
    }
}
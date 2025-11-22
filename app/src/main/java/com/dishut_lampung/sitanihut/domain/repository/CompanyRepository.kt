package com.dishut_lampung.sitanihut.domain.repository

interface CompanyRepository {
    suspend fun saveStructureImageToGallery(): Result<String>
}
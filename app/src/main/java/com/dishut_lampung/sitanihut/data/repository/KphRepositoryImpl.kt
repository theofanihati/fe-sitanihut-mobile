package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KphDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.KphApiService
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.repository.KphRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class KphRepositoryImpl @Inject constructor(
    private val apiService: KphApiService,
    private val dao: KphDao
) : KphRepository {

    override fun getKphList(): Flow<List<Kph>> {
        return dao.getAllKph().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncKphData(): Resource<Unit> {
        return try {
            val pageOneResponse = apiService.getKphList(page = 1)

            if (pageOneResponse.statusCode == 200 && pageOneResponse.data != null) {
                val paginationData = pageOneResponse.data
                val allKphData = paginationData.data.toMutableList()
                val totalPages = paginationData.totalPages

                if (totalPages > 1) {
                    for (page in 2..totalPages) {
                        try {
                            val nextResponse = apiService.getKphList(page = page)
                            if (nextResponse.statusCode == 200 && nextResponse.data != null) {
                                allKphData.addAll(nextResponse.data.data)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if (allKphData.isNotEmpty()) {
                    val kphEntities = allKphData.map { it.toEntity() }

                    dao.deleteAll()
                    dao.insertAll(kphEntities)
//                    android.util.Log.d("REPO_KPH", "Total data tersimpan: ${kphEntities.size}")
                    Resource.Success(Unit)
                } else {
                    Resource.Success(Unit)
                }
            } else {
                Resource.Error(pageOneResponse.message ?: "Gagal mengambil data KPH")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }
}
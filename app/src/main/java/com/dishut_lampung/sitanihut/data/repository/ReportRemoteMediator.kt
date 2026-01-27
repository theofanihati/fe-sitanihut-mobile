package com.dishut_lampung.sitanihut.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.entity.RemoteKeys
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import androidx.paging.LoadType.*
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import retrofit2.HttpException
import java.io.IOException
import java.util.Collections.emptyList
import java.util.Locale

@OptIn(ExperimentalPagingApi::class)
class ReportRemoteMediator(
    private val apiService: ReportApiService,
    private val db: SitanihutDatabase,
    private val query: String,
    private val status: String?
) : RemoteMediator<Int, ReportEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ReportEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = apiService.getReports(
                page = page,
                limit = state.config.pageSize,
                search = query,
                status = status
            )

            val reportsDto = response.data?.data ?: emptyList()
            val totalPages = response.data?.totalPages ?: 1
            val isEndOfList = reportsDto.isEmpty() || page >= response.data.totalPages

            db.withTransaction {
                val isFullRefresh = (loadType == LoadType.REFRESH) && query.isBlank() && status == null
                if (isFullRefresh) {
                    db.remoteKeysDao().clearRemoteKeys()
                    // db.reportDao().clearSyncedReports()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (isEndOfList) null else page + 1

                val keys = reportsDto.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                db.remoteKeysDao().insertAll(keys)
                val skeletons = reportsDto.map { it.toEntity() }
                db.reportDao().upsertPartialBatch(skeletons)

//                val entities = reportsDto.map { dto ->
//                    val oldData = db.reportDao().getReportById(dto.id)
//                    val skeleton = dto.toEntity()
//
//                    if (oldData != null) {
//                        oldData.copy(
//                            status = skeleton.status,
//                            nte = skeleton.nte,
//                            date = skeleton.date,
//                            period = skeleton.period,
//                            month = skeleton.month,
//
//                            plantingDetailsJson = oldData.plantingDetailsJson,
//                            harvestDetailsJson = oldData.harvestDetailsJson,
//                            attachmentsJson = oldData.attachmentsJson,
//                            userName = oldData.userName,
//                            userNik = oldData.userNik,
//                            userGender = oldData.userGender,
//                            userAddress = oldData.userAddress,
//                            userKphName = oldData.userKphName,
//                            userKthName = oldData.userKthName,
//                            farmerNotes = oldData.farmerNotes,
//                            penyuluhNotes = oldData.penyuluhNotes,
//                            modal = oldData.modal,
//
//                            createdAt = oldData.createdAt,
//                            verifiedAt = oldData.verifiedAt,
//                            acceptedAt = oldData.acceptedAt,
//                            syncStatus = if (oldData.syncStatus != SyncStatus.SYNCED) oldData.syncStatus else SyncStatus.SYNCED,
//                            jsonPayload = oldData.jsonPayload
//                        )
//                    } else {
//                        skeleton
//                    }
//                }
//                db.reportDao().insertOrIgnorePending(entities)
            }
            return MediatorResult.Success(endOfPaginationReached = isEndOfList)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ReportEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { repo -> db.remoteKeysDao().getRemoteKeysId(repo.id) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ReportEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                db.remoteKeysDao().getRemoteKeysId(repoId)
            }
        }
    }
}
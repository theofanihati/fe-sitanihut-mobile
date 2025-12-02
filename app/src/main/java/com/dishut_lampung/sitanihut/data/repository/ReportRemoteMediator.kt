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
        TODO("Not yet implemented")
    }
}
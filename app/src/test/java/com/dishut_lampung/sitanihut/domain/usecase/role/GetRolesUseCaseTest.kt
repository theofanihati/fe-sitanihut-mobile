package com.dishut_lampung.sitanihut.domain.usecase.role

import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.repository.RoleRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRolesUseCaseTest {
    private val repository: RoleRepository = mockk(relaxed = true)
    private val useCase  = GetRolesUseCase(repository)

    @Test
    fun `should return list of roles from repository`() = runTest {
        val dummyList = listOf(Role("1", "petani"), Role("2", "penyuluh"))
        every { repository.getRoles() } returns flowOf(Resource.Success(dummyList))

        val result = useCase().first()

        assertEquals(Resource.Success(dummyList), result)
        verify { repository.getRoles() }
    }
}
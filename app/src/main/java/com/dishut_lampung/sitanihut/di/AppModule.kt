package com.dishut_lampung.sitanihut.di

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.remote.AuthApiService
import com.dishut_lampung.sitanihut.data.repository.AuthRepositoryImpl
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.dishut_lampung.sitanihut.domain.use_case.ForgotPasswordUseCase
import com.dishut_lampung.sitanihut.domain.use_case.LoginStatusUseCase
import com.dishut_lampung.sitanihut.domain.use_case.LoginUseCase
import com.dishut_lampung.sitanihut.domain.use_case.LogoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{

    @Provides
    @Singleton
    fun provideAuthApiService(): AuthApiService {
        return Retrofit.Builder()
            .baseUrl("http://api-sipetahut.palum.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: AuthApiService,
        userPreferences: UserPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, userPreferences)
    }

    // USE CASE
    @Provides
    @Singleton
    fun provideLoginUseCase(repository: AuthRepository): LoginUseCase {
        return LoginUseCase(repository)
    }
    @Provides
    @Singleton
    fun provideForgotPasswordUseCase(repository: AuthRepository): ForgotPasswordUseCase {
        return ForgotPasswordUseCase(repository)
    }
    @Provides
    @Singleton
    fun provideLogoutUseCase(repository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(repository)
    }
    @Provides
    @Singleton
    fun provideCheckLoginStatusUseCase(repository: AuthRepository): LoginStatusUseCase {
        return LoginStatusUseCase(repository)
    }

}
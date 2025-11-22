package com.dishut_lampung.sitanihut.di

import android.content.Context
import androidx.room.Room
import com.dishut_lampung.sitanihut.BuildConfig
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.remote.api.AuthApiService
import com.dishut_lampung.sitanihut.data.remote.api.HomeApiService
import com.dishut_lampung.sitanihut.data.repository.AuthRepositoryImpl
import com.dishut_lampung.sitanihut.data.repository.CompanyRepositoryImpl
import com.dishut_lampung.sitanihut.data.repository.HomeRepositoryImpl
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.dishut_lampung.sitanihut.domain.repository.CompanyRepository
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.ForgotPasswordUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.LoginStatusUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.LoginUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.ValidateEmailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.ValidatePasswordUseCase
import com.dishut_lampung.sitanihut.domain.usecase.home.GetPetaniHomeDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{

    @Provides
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApiService(retrofit: Retrofit): HomeApiService {
        return retrofit.create(HomeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: AuthApiService,
        userPreferences: UserPreferences,
        reportDao: ReportDao
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, userPreferences, reportDao)
    }

    @Provides
    @Singleton
    fun provideHomeRepository(
        apiService: HomeApiService,
        userPreferences: UserPreferences,
        reportDao: ReportDao
    ): HomeRepository {
        return HomeRepositoryImpl(apiService, reportDao, userPreferences)
    }

    @Provides
    @Singleton
    fun provideCompanyRepository(
        @ApplicationContext context: Context
    ): CompanyRepository {
        return CompanyRepositoryImpl(context)
    }

    // DATABASE
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SitanihutDatabase {
        return Room.databaseBuilder(
            context,
            SitanihutDatabase::class.java,
            "sitanihut_database"
        ).fallbackToDestructiveMigration() // reset db kalau ada perubahan struktur
            .build()
    }

    @Provides
    @Singleton
    fun provideReportDao(database: SitanihutDatabase): ReportDao {
        return database.reportDao()
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
    fun provideLoginStatusUseCase(repository: AuthRepository): LoginStatusUseCase {
        return LoginStatusUseCase(repository)
    }
    @Provides
    @Singleton
    fun provideValidateEmailUseCase(): ValidateEmailUseCase {
        return ValidateEmailUseCase()
    }
    @Provides
    @Singleton
    fun provideValidatePasswordUseCase(): ValidatePasswordUseCase {
        return ValidatePasswordUseCase()
    }
    @Provides
    @Singleton
    fun getFarmerHomeDataUseCase(repository: HomeRepository): GetPetaniHomeDataUseCase {
        return GetPetaniHomeDataUseCase(repository)
    }
}
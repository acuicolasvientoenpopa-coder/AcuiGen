package com.nfctags.app.di

import android.content.Context
import androidx.room.Room
import com.nfctags.app.auth.AuthApiService
import com.nfctags.app.auth.SupabaseAuthInterceptor
import com.nfctags.app.data.dao.TagDao
import com.nfctags.app.data.dao.ValueHistoryDao
import com.nfctags.app.data.database.AppDatabase
import com.nfctags.app.data.remote.SupabaseApiService
import com.nfctags.app.sync.SyncManager
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
object AppModule {

    private const val SUPABASE_URL = "https://smvjffbeshxcfltjoolm.supabase.co/rest/v1/"
    private const val SUPABASE_AUTH_URL = "https://smvjffbeshxcfltjoolm.supabase.co/auth/v1/"
    private const val SUPABASE_ANON_KEY = "sb_publishable_EQRvreJDv4d-wYZmaMY3Bg_x2D3kM_v"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nfc_tags_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    @Singleton
    fun provideValueHistoryDao(db: AppDatabase): ValueHistoryDao = db.valueHistoryDao()

    @Provides
    @Singleton
    fun provideSyncManager(@ApplicationContext context: Context): SyncManager {
        return SyncManager(context)
    }

    @DataOkHttpClient
    @Provides
    @Singleton
    fun provideDataOkHttpClient(
        authInterceptor: SupabaseAuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @DataRetrofit
    @Provides
    @Singleton
    fun provideDataRetrofit(@DataOkHttpClient client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseApi(@DataRetrofit retrofit: Retrofit): SupabaseApiService {
        return retrofit.create(SupabaseApiService::class.java)
    }

    @AuthOkHttpClient
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(
        authInterceptor: SupabaseAuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @AuthRetrofit
    @Provides
    @Singleton
    fun provideAuthRetrofit(@AuthOkHttpClient client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SUPABASE_AUTH_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@AuthRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}

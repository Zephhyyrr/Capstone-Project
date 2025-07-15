package com.firman.capstone.urvoice.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.firman.capstone.urvoice.BuildConfig
import com.firman.capstone.urvoice.data.remote.service.AnalyzeService
import com.firman.capstone.urvoice.data.remote.service.ArticleService
import com.firman.capstone.urvoice.data.remote.service.HistoryService
import com.firman.capstone.urvoice.data.remote.service.UserService
import com.firman.capstone.urvoice.data.remote.service.LoginService
import com.firman.capstone.urvoice.data.remote.service.RegisterService
import com.firman.capstone.urvoice.data.remote.service.SpeechService
import com.firman.capstone.urvoice.utils.ApiConstant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context).build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(chuckerInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstant.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLoginService(retrofit: Retrofit): LoginService {
        return retrofit.create(LoginService::class.java)
    }

    @Provides
    @Singleton
    fun provideRegisterService(retrofit: Retrofit): RegisterService{
        return retrofit.create(RegisterService::class.java)
    }

    @Provides
    @Singleton
    fun provideArticleService(retrofit: Retrofit):ArticleService {
        return retrofit.create(ArticleService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Singleton
    @Provides
    fun provideSpeechService(retrofit: Retrofit): SpeechService {
        return retrofit.create(SpeechService::class.java)
    }

    @Singleton
    @Provides
    fun provideAnalyzeService(retrofit: Retrofit): AnalyzeService {
        return retrofit.create(AnalyzeService::class.java)
    }

    @Singleton
    @Provides
    fun provideHistoryService(retrofit: Retrofit): HistoryService {
        return retrofit.create(HistoryService::class.java)
    }
}

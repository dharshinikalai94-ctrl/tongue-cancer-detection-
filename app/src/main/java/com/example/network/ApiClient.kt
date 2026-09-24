package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
  }

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

  @Volatile
  private var currentBaseUrl: String = "http://10.0.2.2:8000/"

  @Volatile
  private var currentService: TongueDetectionApiService? = null

  fun getService(baseUrl: String = currentBaseUrl): TongueDetectionApiService {
    val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    if (currentService == null || currentBaseUrl != normalizedUrl) {
      synchronized(this) {
        currentBaseUrl = normalizedUrl
        val retrofit = Retrofit.Builder()
          .baseUrl(currentBaseUrl)
          .client(okHttpClient)
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
        currentService = retrofit.create(TongueDetectionApiService::class.java)
      }
    }
    return currentService!!
  }

  fun updateBaseUrl(newUrl: String) {
    var formatted = newUrl.trim()
    if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
      formatted = "http://$formatted"
    }
    if (!formatted.endsWith("/")) {
      formatted = "$formatted/"
    }
    currentBaseUrl = formatted
    currentService = null
  }

  fun getBaseUrl(): String = currentBaseUrl
}

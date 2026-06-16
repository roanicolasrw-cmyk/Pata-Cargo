package com.example.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface MercadoPagoApi {
    @POST("checkout/preferences")
    suspend fun createPreference(
        @Header("Authorization") authorization: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("v1/payments/search")
    suspend fun searchPayments(
        @Header("Authorization") authorization: String,
        @Query("external_reference") externalReference: String
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("v1/customers/search")
    suspend fun searchCustomer(
        @Header("Authorization") authorization: String,
        @Query("email") email: String
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("v1/customers")
    suspend fun createCustomer(
        @Header("Authorization") authorization: String,
        @Body body: Map<String, String>
    ): Response<Map<String, @JvmSuppressWildcards Any>>
}

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val mercadoPagoApi: MercadoPagoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mercadopago.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MercadoPagoApi::class.java)
    }
}

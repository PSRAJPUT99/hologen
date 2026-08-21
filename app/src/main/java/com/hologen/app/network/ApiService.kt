package com.hologen.app.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateScanRequest(
    val text: String,
    val attachments: List<NetworkAttachment>
)

data class NetworkAttachment(
    val type: String,
    val uri: String
)

data class CreateScanResponse(
    val scanId: String
)

interface ApiService {
    @POST("api/v1/scans")
    suspend fun createScan(@Body request: CreateScanRequest): CreateScanResponse

    @GET("api/v1/scans/{scan_id}/stream")
    suspend fun streamScan(@Path("scan_id") scanId: String): retrofit2.Response<Unit>
}
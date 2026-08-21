package com.hologen.app.repository

import com.hologen.app.data.Attachment
import com.hologen.app.data.ScanProgress
import com.hologen.app.network.ApiService
import com.hologen.app.network.CreateScanRequest
import com.hologen.app.network.NetworkAttachment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

interface ChatRepository {
    suspend fun createScan(text: String, attachments: List<Attachment>): String
    fun streamScan(scanId: String): Flow<ScanProgress>
}

class ApiChatRepository(private val apiService: ApiService) : ChatRepository {
    override suspend fun createScan(text: String, attachments: List<Attachment>): String {
        return apiService.createScan(
            CreateScanRequest(
                text = text,
                attachments = attachments.map { NetworkAttachment(it.type.name.lowercase(), it.uri) }
            )
        ).scanId
    }

    override fun streamScan(scanId: String): Flow<ScanProgress> = emptyFlow()
}

class MockApiRepository : ChatRepository {
    override suspend fun createScan(text: String, attachments: List<Attachment>): String = "mock-scan"

    override fun streamScan(scanId: String): Flow<ScanProgress> = flow {
        val updates = listOf(
            ScanProgress("Recognizing object...", 20),
            ScanProgress("Researching parts on the web...", 50),
            ScanProgress("Generating 3D hologram mesh...", 80),
            ScanProgress("Hologram ready. Tap to explore.", 100)
        )
        updates.forEach { update ->
            delay(1250)
            emit(update)
        }
    }
}
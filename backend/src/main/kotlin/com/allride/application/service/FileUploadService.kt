package com.allride.application.service

import com.allride.domain.event.FileUploadedEvent
import com.allride.infrastructure.messaging.EventChannel
import com.allride.infrastructure.storage.FileStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileUploadService(
    private val fileStorageService: FileStorageService,
    private val eventChannel: EventChannel
) {
    suspend fun handleFileUpload(file: MultipartFile, originalFileName: String): String {
        // Store the file temporarily
        val filePath = withContext(Dispatchers.IO) {
            fileStorageService.store(file)
        }
        
        // Publish event to the message queue
        val event = FileUploadedEvent(
            filePath = filePath,
            fileName = originalFileName
        )
        
        eventChannel.publish(event)
        
        return filePath
    }
}


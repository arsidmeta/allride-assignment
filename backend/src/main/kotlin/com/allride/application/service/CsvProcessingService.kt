package com.allride.application.service

import com.allride.domain.event.FileUploadedEvent
import com.allride.infrastructure.csv.CsvParser
import com.allride.infrastructure.messaging.EventChannel
import com.allride.infrastructure.storage.FileStorageService
import com.allride.infrastructure.storage.InMemoryUserRepository
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import jakarta.annotation.PreDestroy

@Service
class CsvProcessingService(
    private val eventChannel: EventChannel,
    private val csvParser: CsvParser,
    private val fileStorageService: FileStorageService,
    private val userRepository: InMemoryUserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val processingScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var processingJob: Job? = null
    
    @EventListener(ApplicationReadyEvent::class)
    fun startProcessing() {
        logger.info("Starting CSV processing worker...")
        processingJob = processingScope.launch {
            val channel = eventChannel.subscribe()
            
            while (isActive) {
                try {
                    val event = channel.receive()
                    processFileUploadEvent(event)
                } catch (e: CancellationException) {
                    logger.info("CSV processing worker stopped")
                    break
                } catch (e: Exception) {
                    logger.error("Error processing event: ${e.message}", e)
                }
            }
        }
    }
    
    private suspend fun processFileUploadEvent(event: FileUploadedEvent) = withContext(Dispatchers.IO) {
        logger.info("Processing file upload event: ${event.fileName} at ${event.filePath}")
        
        try {
            // Parse CSV file
            val parseResult = csvParser.parse(event.filePath)
            
            // Log errors if any
            if (parseResult.errors.isNotEmpty()) {
                logger.warn("Encountered ${parseResult.errors.size} error(s) while parsing CSV:")
                parseResult.errors.forEach { error ->
                    logger.warn("  Row ${error.rowNumber}: ${error.message}")
                }
            }
            
            // Store valid users
            if (parseResult.users.isNotEmpty()) {
                userRepository.saveAll(parseResult.users)
                logger.info("Successfully imported ${parseResult.users.size} user(s) from ${event.fileName}")
                
                // Log imported users (as per requirement to log parsed data)
                parseResult.users.forEach { user ->
                    logger.info("Imported user: id=${user.id}, firstName=${user.firstName}, lastName=${user.lastName}, email=${user.email}")
                }
            } else {
                logger.warn("No valid users found in file: ${event.fileName}")
            }
            
            // Clean up the uploaded file after processing
            fileStorageService.deleteFile(event.filePath)
            logger.info("Processing completed for file: ${event.fileName}")
            
        } catch (e: Exception) {
            logger.error("Failed to process file ${event.fileName}: ${e.message}", e)
            // Clean up file even on error
            try {
                fileStorageService.deleteFile(event.filePath)
            } catch (cleanupException: Exception) {
                logger.error("Failed to cleanup file: ${cleanupException.message}")
            }
        }
    }
    
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down CSV processing service...")
        processingJob?.cancel()
        processingScope.cancel()
    }
}


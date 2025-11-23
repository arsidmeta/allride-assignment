package com.allride.presentation.controller

import com.allride.application.service.FileUploadService
import com.allride.presentation.dto.UploadResponse
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = ["http://localhost:3000"]) // Allow React dev server
class FileUploadController(
    private val fileUploadService: FileUploadService
) {
    
    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadResponse> {
        return try {
            // Validate file
            if (file.isEmpty) {
                return ResponseEntity.badRequest()
                    .body(UploadResponse(success = false, message = "File is empty"))
            }
            
            val originalFileName = file.originalFilename ?: "unknown.csv"
            
            // Validate file type
            if (!originalFileName.endsWith(".csv", ignoreCase = true)) {
                return ResponseEntity.badRequest()
                    .body(UploadResponse(success = false, message = "Only CSV files are allowed"))
            }
            
            // Process file upload (this will publish the event)
            runBlocking {
                fileUploadService.handleFileUpload(file, originalFileName)
            }
            
            ResponseEntity.ok(
                UploadResponse(
                    success = true,
                    message = "File uploaded successfully, processing started"
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UploadResponse(success = false, message = "Upload failed: ${e.message}"))
        }
    }
}


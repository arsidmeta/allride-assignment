package com.allride.infrastructure.storage

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
class FileStorageService(
    @Value("\${app.upload.dir:uploads}") private val uploadDir: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val uploadPath: Path by lazy {
        // Use absolute path to avoid path resolution issues
        val baseDir = Paths.get(System.getProperty("user.dir"))
        val path = baseDir.resolve(uploadDir).toAbsolutePath().normalize()
        
        if (!Files.exists(path)) {
            Files.createDirectories(path)
            logger.info("Created upload directory: $path")
        }
        
        logger.info("Upload directory: $path")
        path
    }
    
    fun store(file: MultipartFile): String {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }
        
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        val filePath = uploadPath.resolve(fileName).normalize()
        
        // Ensure parent directory exists
        Files.createDirectories(filePath.parent)
        
        // Transfer file and ensure it's written to disk
        file.transferTo(filePath.toFile())
        
        // Verify file was written
        if (!Files.exists(filePath)) {
            throw IllegalStateException("File was not saved: $filePath")
        }
        
        val absolutePath = filePath.toAbsolutePath().toString()
        logger.info("File stored successfully: $absolutePath (size: ${Files.size(filePath)} bytes)")
        
        return absolutePath
    }
    
    fun getFile(filePath: String): File {
        val path = Paths.get(filePath).toAbsolutePath().normalize()
        val file = path.toFile()
        
        if (!file.exists()) {
            throw java.io.FileNotFoundException("File not found: $filePath (resolved to: ${path})")
        }
        
        return file
    }
    
    fun deleteFile(filePath: String) {
        try {
            val path = Paths.get(filePath).toAbsolutePath().normalize()
            if (Files.deleteIfExists(path)) {
                logger.info("Deleted file: $path")
            } else {
                logger.warn("File not found for deletion: $path")
            }
        } catch (e: Exception) {
            logger.error("Failed to delete file: $filePath - ${e.message}", e)
        }
    }
}


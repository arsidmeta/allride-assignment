package com.allride.infrastructure.csv

import com.allride.domain.model.User
import com.allride.infrastructure.storage.FileStorageService
import com.opencsv.CSVReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.FileReader
import java.io.IOException

data class CsvParseResult(
    val users: List<User>,
    val errors: List<RowError>
)

data class RowError(
    val rowNumber: Int,
    val message: String
)

@Component
class CsvParser(
    private val fileStorageService: FileStorageService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun parse(filePath: String): CsvParseResult {
        val users = mutableListOf<User>()
        val errors = mutableListOf<RowError>()
        
        try {
            // Use FileStorageService to get the file with proper path resolution
            val file = fileStorageService.getFile(filePath)
            logger.debug("Reading CSV file: ${file.absolutePath}")
            
            FileReader(file).use { reader ->
                CSVReader(reader).use { csvReader ->
                    val rows = csvReader.readAll()
                    
                    // Skip header row (index 0)
                    rows.drop(1).forEachIndexed { index, row ->
                        val rowNumber = index + 2 // +2 because we dropped header and 1-indexed
                        
                        try {
                            if (row.size < 4) {
                                errors.add(RowError(rowNumber, "Row has insufficient columns. Expected 4 (id, firstName, lastName, email)"))
                                return@forEachIndexed
                            }
                            
                            val id = row[0].trim()
                            val firstName = row[1].trim()
                            val lastName = row[2].trim()
                            val email = row[3].trim()
                            
                            // Basic validation
                            if (id.isBlank() || firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
                                errors.add(RowError(rowNumber, "Row contains empty required fields"))
                                return@forEachIndexed
                            }
                            
                            if (!isValidEmail(email)) {
                                errors.add(RowError(rowNumber, "Invalid email format: $email"))
                                return@forEachIndexed
                            }
                            
                            users.add(User(id, firstName, lastName, email))
                        } catch (e: Exception) {
                            errors.add(RowError(rowNumber, "Error parsing row: ${e.message}"))
                        }
                    }
                }
            }
        } catch (e: java.io.FileNotFoundException) {
            logger.error("File not found: $filePath", e)
            errors.add(RowError(0, "File not found: ${e.message}"))
        } catch (e: IOException) {
            logger.error("Failed to read CSV file: $filePath", e)
            errors.add(RowError(0, "Failed to read CSV file: ${e.message}"))
        } catch (e: Exception) {
            logger.error("Unexpected error parsing CSV file: $filePath", e)
            errors.add(RowError(0, "Unexpected error: ${e.message}"))
        }
        
        return CsvParseResult(users, errors)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"))
    }
}


package com.allride.domain.event

data class FileUploadedEvent(
    val filePath: String,
    val fileName: String,
    val uploadedAt: Long = System.currentTimeMillis()
)


package com.allride.infrastructure.messaging

import com.allride.domain.event.FileUploadedEvent
import kotlinx.coroutines.channels.Channel
import org.springframework.stereotype.Component

/**
 * In-memory event channel using Kotlin Coroutines Channels for pub/sub mechanism.
 * This is a simple, lightweight solution suitable for local development.
 * 
 * In production, this would be replaced with GCP Pub/Sub, RabbitMQ, or Kafka.
 */
@Component
class EventChannel {
    private val channel = Channel<FileUploadedEvent>(Channel.UNLIMITED)
    
    suspend fun publish(event: FileUploadedEvent) {
        channel.send(event)
    }
    
    suspend fun subscribe(): Channel<FileUploadedEvent> {
        return channel
    }
    
    fun close() {
        channel.close()
    }
}


package com.allride.infrastructure.storage

import com.allride.domain.model.User
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository for storing user data.
 * In production, this would be replaced with a proper database.
 */
@Repository
class InMemoryUserRepository {
    private val users = ConcurrentHashMap<String, User>()
    
    fun save(user: User) {
        users[user.id] = user
    }
    
    fun saveAll(userList: List<User>) {
        userList.forEach { save(it) }
    }
    
    fun findAll(): List<User> {
        return users.values.toList()
    }
    
    fun findById(id: String): User? {
        return users[id]
    }
    
    fun count(): Int {
        return users.size
    }
    
    fun clear() {
        users.clear()
    }
}


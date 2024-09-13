package com.example.services

import com.example.models.User
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.atomic.AtomicInteger

class UserService {
    private val users = mutableListOf<User>()
    private val idCounter = AtomicInteger()

    fun addUser(email: String, password: String, firstName: String, lastName: String): User {
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val newUser = User(id = idCounter.incrementAndGet(), email = email, passwordHash = passwordHash, firstName = firstName, lastName = lastName)
        users.add(newUser)
        return newUser
    }

    fun getUserByEMail(email: String): User? {
        return users.find { it.email == email }
    }

    fun verifyUser(email: String, password: String): Boolean {
        val user = getUserByEMail(email)
        return user != null && BCrypt.checkpw(password, user.passwordHash)
    }

    fun getAllUsers(): List<User> {
        return users.toList()
    }

    fun updateUserName(userId: String, name: String) {
        // Implement this to update a user's name
    }
}
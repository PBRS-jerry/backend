package com.example.userservice.service

import com.example.userservice.model.User
import com.example.userservice.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUser(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun updateUser(id: Long, updatedUser: User): User? {
        val user = userRepository.findById(id).orElse(null) ?: return null
        user.username = updatedUser.username
        if (user.password != updatedUser.password) {
            user.password = updatedUser.password
        }
        return userRepository.save(user)
    }

    @Transactional
    fun softDeleteUser(id: Long) {
        val user = userRepository.findById(id).orElse(null) ?: return
        user.isActive = false
        userRepository.save(user)
    }
}
package com.example.userservice.controller

import com.example.userservice.model.User
import com.example.userservice.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.example.userservice.utils.AppLogger

@RestController
@RequestMapping("/user-service")
class UserController(private val userService: UserService, private val appLogger: AppLogger) {

    @GetMapping("/public/users")
    fun getUsers(): String {
        return "Hello"
    }

    @GetMapping("/protected/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<User?> {
        appLogger.info("Getting user with id: $id")
        val user = userService.getUser(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }

    @PutMapping("/protected/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody updatedUser: User): ResponseEntity<User?> {
        val user = userService.updateUser(id, updatedUser) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/protected/{id}")
    fun softDeleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.softDeleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
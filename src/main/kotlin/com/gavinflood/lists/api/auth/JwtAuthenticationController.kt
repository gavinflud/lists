package com.gavinflood.lists.api.auth

import com.gavinflood.lists.api.service.AppUserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller used to authenticate users and provide them with a JWT.
 */
@RestController
@RequestMapping("/api/authenticate")
class JwtAuthenticationController(

    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val userDetailsService: AppUserService

) {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationController::class.java)

    /**
     * Authenticate a set of credentials and produce a JWT.
     */
    @PostMapping
    fun authenticate(@RequestBody request: JwtAuthenticationRequest): ResponseEntity<JwtResponse> {
        return try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(request.username, request.password))
            val userDetails = userDetailsService.loadUserByUsername(request.username)
            val accessToken = jwtUtil.generateAccessToken(userDetails)
            val refreshToken = jwtUtil.generateRefreshToken(userDetails)
            ResponseEntity.ok(JwtResponse(accessToken, refreshToken))
        } catch (exception: Exception) {
            logger.warn("'${request.username}' failed to authenticate due to '${exception.message}'")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    /**
     * Attempt to refresh an access token using a refresh token.
     */
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: JwtRefreshRequest): ResponseEntity<JwtResponse> {
        return try {
            val username = jwtUtil.getUsernameFromToken(request.refreshToken)
            val userDetails = userDetailsService.loadUserByUsername(username)
            val accessToken = jwtUtil.generateAccessToken(userDetails)
            val refreshToken = jwtUtil.generateRefreshToken(userDetails)
            ResponseEntity.ok(JwtResponse(accessToken, refreshToken))
        } catch (exception: Exception) {
            logger.warn("'${request.refreshToken}' failed to authenticate due to '${exception.message}'")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

}

/**
 * Simple class to store a request with the user's credentials as the body.
 */
data class JwtAuthenticationRequest(val username: String, val password: String)

/**
 * Simple class to store a request with the refresh token as the body.
 */
data class JwtRefreshRequest(val refreshToken: String)

/**
 * Simple class to store a response with the user's JWTs as the body.
 */
data class JwtResponse(val accessToken: String, val refreshToken: String)
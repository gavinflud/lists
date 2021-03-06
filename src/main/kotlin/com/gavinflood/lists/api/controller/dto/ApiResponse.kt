package com.gavinflood.lists.api.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

/**
 * DTO for a response from the API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse(val body: Any?) {

    var errorCode: String? = null
    var errorDescription: String? = null
    var timestamp = Date()

    /**
     * @param errorCode the error that occurred (optional)
     * @param errorDescription describes the error and any steps that can be taken to rectify it (optional)
     */
    constructor(errorCode: String, errorDescription: String) : this(null) {
        this.errorCode = errorCode
        this.errorDescription = errorDescription
    }

    companion object {
        const val ERROR_NOT_FOUND = "L1000"
        const val ERROR_CONFLICT = "L1001"
        const val ERROR_UNAUTHORIZED = "L1002"
        const val ERROR_BAD_OPERATION = "L1003"
    }

}
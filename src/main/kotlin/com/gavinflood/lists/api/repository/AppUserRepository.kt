package com.gavinflood.lists.api.repository

import com.gavinflood.lists.api.domain.AppUser
import com.gavinflood.lists.api.domain.Credential
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository for the users table.
 */
@Repository
interface AppUserRepository : JpaRepository<AppUser, Long> {

    /**
     * Find a single [AppUser] given their [credential].
     */
    fun findDistinctByCredentialAndRetiredIsFalse(credential: Credential): Optional<AppUser>

}
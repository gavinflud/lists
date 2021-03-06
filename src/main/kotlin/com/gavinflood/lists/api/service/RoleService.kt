package com.gavinflood.lists.api.service

import com.gavinflood.lists.api.domain.Role
import com.gavinflood.lists.api.exception.AlreadyExistsException
import com.gavinflood.lists.api.exception.NoMatchFoundException
import com.gavinflood.lists.api.repository.RoleRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Business logic for roles.
 */
@Service
class RoleService(private val roleRepository: RoleRepository) {

    private val logger: Logger = LoggerFactory.getLogger(RoleService::class.java)

    /**
     * Find a distinct [Role] by its [code].
     */
    fun findByCode(code: String): Role {
        return roleRepository.findDistinctByCodeAndRetiredIsFalse(code).orElseThrow {
            logger.warn("Could not find role with code '$code'")
            NoMatchFoundException("Could not find role with code '$code'")
        }
    }

    /**
     * Find multiple roles matching a set of [codes].
     */
    fun findMultiple(codes: Set<String>): Set<Role> {
        return roleRepository.findAllByCodeInAndRetiredIsFalse(codes)
    }

    /**
     * Find all roles.
     */
    fun findAll(pageable: Pageable): Page<Role> {
        return roleRepository.findAllByRetiredIsFalse(pageable)
    }

    /**
     * Create a new [Role].
     */
    fun create(role: Role): Role {
        if (roleRepository.findDistinctByCodeAndRetiredIsFalse(role.code).isEmpty) {
            return roleRepository.save(role)
        }

        throw AlreadyExistsException("Can't create role with code '${role.code}' as it already exists")
    }

    /**
     * Update an existing [Role] identified by its code.
     */
    fun update(code: String, updatedRole: Role): Role {
        val existingRole = findByCode(code)
        val updatedRoleCode = updatedRole.code

        if (code != updatedRoleCode && roleRepository.findDistinctByCodeAndRetiredIsFalse(updatedRoleCode).isPresent) {
            throw AlreadyExistsException("Can't update permission with code '$updatedRoleCode' as it already exists")
        }

        existingRole.code = updatedRole.code
        existingRole.description = updatedRole.description
        return roleRepository.save(existingRole)
    }

    /**
     * Retire an existing [Role] identified by its code.
     */
    fun retire(code: String) {
        try {
            val role = findByCode(code)
            role.retire()
            logger.info("Retiring permission ${role.id}")
            roleRepository.save(role)
        } catch (exception: NoMatchFoundException) {
            logger.warn("Cannot retire a role as none exists with the code '$code'")
            throw exception
        }
    }

}
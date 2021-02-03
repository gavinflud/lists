package com.gavinflood.lists.api.service

import com.gavinflood.lists.api.domain.AppUser
import com.gavinflood.lists.api.domain.Team
import com.gavinflood.lists.api.exception.NoMatchFoundException
import com.gavinflood.lists.api.repository.TeamRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

/**
 * Business logic for teams.
 */
@Service
class TeamService(

    private val teamRepository: TeamRepository,
    private val appUserService: AppUserService

) {

    private val logger = LoggerFactory.getLogger(TeamService::class.java)

    /**
     * Create a new team and add the current authenticated user as its only initial member.
     *
     * @param team the team to be created
     * @return the persisted team
     */
    fun create(team: Team): Team {
        val users = mutableSetOf(appUserService.getCurrentAuthenticatedUser())
        return create(team, users)
    }

    /**
     * Create a new team and add multiple users as members.
     *
     * @param team the team to be created
     * @param users initial members of the team
     * @return the persisted team
     */
    protected fun create(team: Team, users: Collection<AppUser>): Team {
        team.members.addAll(users)
        return teamRepository.save(team)
    }

    /**
     * Find a team by its unique ID.
     *
     * @param id identifies the team
     * @return the team if it exists
     * @throws NoMatchFoundException if a team with that ID was not found
     */
    fun findById(id: Long): Team {
        val team = teamRepository.findById(id)

        if (team.isEmpty) {
            logger.warn("No team was found with ID '$id'")
            throw NoMatchFoundException("No team was found with ID '$id'")
        }

        return team.get()
    }

    /**
     * Find the teams a user is a member of.
     *
     * @param userId identifies the user
     * @param pageable defines the page index and size
     * @return a paginated list of teams the user is a member of
     * @throws NoMatchFoundException if a user with that ID was not found
     */
    fun findTeamsForUser(userId: Long, pageable: Pageable): Page<Team> {
        try {
            val user = appUserService.findById(userId)
            return teamRepository.findAllByMembersContains(user, pageable)
        } catch (exception: NoMatchFoundException) {
            logger.warn("Cannot find teams as no user exists with the ID '$userId'")
            throw exception
        }
    }

    /**
     * Update a team.
     *
     * @param id identifies the team
     * @param updatedTeam the updated team details
     * @return the persisted team
     * @throws NoMatchFoundException if a team with that ID was not found
     */
    fun update(id: Long, updatedTeam: Team): Team {
        try {
            val team = findById(id)
            team.name = updatedTeam.name
            logger.info("Updating team ${team.id}")
            return teamRepository.save(team)
        } catch (exception: NoMatchFoundException) {
            logger.warn("Cannot update a team as none exists with the ID '$id'")
            throw exception
        }
    }

    /**
     * Retire a team.
     *
     * @param id identifies the team
     * @throws NoMatchFoundException if a team with that ID was not found
     */
    fun retire(id: Long) {
        try {
            val team = findById(id)
            team.retire()
            logger.info("Retiring team ${team.id}")
            teamRepository.save(team)
        } catch (exception: NoMatchFoundException) {
            logger.warn("Cannot retire a team as none exists with the ID '$id'")
            throw exception
        }
    }

}
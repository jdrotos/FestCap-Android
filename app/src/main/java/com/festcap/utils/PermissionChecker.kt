package com.festcap.utils

import com.google.firebase.auth.FirebaseUser
import com.festcap.data.Fest
import com.festcap.data.Venue

/**
 * Check common permissions
 */
object PermissionChecker {
    fun canAdminFest(user: FirebaseUser, fest: Fest) =
            fest.adminIds.containsKey(user.uid)

    fun canViewFest(user: FirebaseUser, fest: Fest) =
            fest.memberIds.containsKey(user.uid)

    fun canEditHeadCount(user: FirebaseUser, fest: Fest, venue: Venue) =
            canAdminFest(user, fest) || venue.doorKeeperIds.containsKey(user.uid)
}
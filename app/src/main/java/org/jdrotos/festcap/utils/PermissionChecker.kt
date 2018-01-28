package org.jdrotos.festcap.utils

import com.firebase.ui.auth.User
import com.google.firebase.auth.FirebaseUser
import org.jdrotos.festcap.data.Fest
import org.jdrotos.festcap.data.Venue

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
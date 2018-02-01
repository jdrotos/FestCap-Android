package com.festcap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.festcap.data.DataKeys
import com.festcap.data.User
import timber.log.Timber


/**
 * This is the launcher activity. It routes us to the login flow or the data screen.
 */
class LaunchActivity : Activity() {

    private val usersRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.USERS)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLoginAndRoute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult")
        checkLoginAndRoute()
    }

    private fun checkLoginAndRoute() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Timber.d("Not logged in, starting firebase login")
            // Choose authentication providers
            val providers = listOf(
                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN)
        } else {
            val user = User(id = currentUser.uid, email = currentUser.email ?: "", name = currentUser.displayName ?: "")
            usersRef.child(currentUser.uid).setValue(user)
            usersRef.child(currentUser.uid).push()

            Timber.d("Logged in...")
            startActivity(FestsActivity.createIntent(this))
            //startActivity(VenueActivity.createIntent(this, "fake_fest_id"))
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 445566
    }
}
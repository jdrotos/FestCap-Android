package org.jdrotos.festcap


import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.database.*
import org.jdrotos.festcap.data.DataKeys
import org.jdrotos.festcap.databinding.ActivityVenueBinding
import timber.log.Timber

/**
 * Created by jdrotos on 11/18/17.
 */
class VenueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVenueBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var venuesViewModel: VenuesViewModel

    private val venuesRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.VENUES)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        venuesViewModel = ViewModelProviders.of(this).get(VenuesViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_venue)

        setSupportActionBar(binding.toolbar)
        drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open_drawer_desc, R.string.closed_drawer_desc)
        drawerToggle.syncState()

        binding.selectAVenue.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.startDrawerContainer)
        }

        processIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        // We need to re-process here in case the venue or fest has been removed
        processArgs(venuesViewModel.festId.value, venuesViewModel.venueId.value)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        val festId = intent.getStringExtra(ARG_FEST_ID)
        val venueId: String? = intent.getStringExtra(ARG_VENUE_ID)
        processArgs(festId, venueId)
    }

    private fun processArgs(festId: String?, venueId: String?) {
        Timber.d("JOE: festId: $festId")
        if (festId == null) {
            Timber.e("Closing Activity. No festId provided.")
            finish()
            return
        } else if (festId != venuesViewModel.festId.value) {
            fragmentManager.beginTransaction().let {
                it.replace(R.id.start_drawer_container, FestDrawerFragment.newInstance(festId), FTAG_DRAWER)
                it.commit()
            }
        }
        venuesViewModel.festId.value = festId

        if (venueId == null) {
            removeVenueFrag()
        } else {
            // We make sure that we have a valid venue
            venuesRef.child(venueId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Timber.d("onCancelled was called unexpectedly")
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    if (snapshot?.exists() == true) {
                        showVenueFrag(venueId)
                    } else {
                        removeVenueFrag()
                    }
                }

            })
        }
        venuesViewModel.venueId.value = venueId
    }

    private fun removeVenueFrag() {
        binding.selectAVenue.visibility = View.VISIBLE
        fragmentManager.findFragmentByTag(FTAG_VENUE)?.let { venueFrag ->
            fragmentManager.beginTransaction().let {
                it.remove(venueFrag)
                it.commit()
            }
        }
        binding.drawerLayout.openDrawer(binding.startDrawerContainer)
    }

    private fun showVenueFrag(venueId: String) {
        binding.selectAVenue.visibility = View.GONE
        binding.drawerLayout.closeDrawers()
        fragmentManager.beginTransaction().let {
            it.replace(R.id.main_fragment_container, VenueFragment.newInstance(venueId), FTAG_VENUE)
            it.commit()
        }
    }

    companion object {
        private const val ARG_FEST_ID = "ARG_FEST_ID"
        private const val ARG_VENUE_ID = "ARG_VENUE_ID"

        private const val FTAG_DRAWER = "FTAG_DRAWER"
        private const val FTAG_VENUE = "FTAG_VENUE"

        fun createIntent(context: Context, festId: String, venueId: String? = null) =
                Intent(context, VenueActivity::class.java).apply {
                    putExtra(ARG_FEST_ID, festId)
                    putExtra(ARG_VENUE_ID, venueId)
                }
    }
}
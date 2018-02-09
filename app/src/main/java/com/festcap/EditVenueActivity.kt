package com.festcap

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.google.firebase.database.*
import com.festcap.data.DataKeys
import com.festcap.data.HeadCount
import com.festcap.data.User
import com.festcap.data.Venue
import com.festcap.databinding.ActivityEditVenueBinding
import com.festcap.utils.NachoUtils
import timber.log.Timber


/**
 * This is the activity for editing a venue
 */
class EditVenueActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditVenueBinding

    private var users = emptyList<User>()
    private var hasProcessedUsers = false

    private val argVenue: Venue by lazy {
        intent.getParcelableExtra<Venue>(ARG_VENUE)
    }

    private var hasHeadCount = false

    private val venuesRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.VENUES).child(argVenue.id)
        }
    }

    private val headCountRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.HEADCOUNTS).child(argVenue.id)
        }
    }

    private val usersRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.USERS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_venue)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.venueNameEdittext.setText(argVenue.name)
        binding.venueCapcityEdittext.setText(argVenue.capacity.toString())
        NachoUtils.setupStandardChipsForEmail(binding.venueDoorkeepersEdittext)

        binding.createBtn.setOnClickListener {
            createVenue()?.let {
                venuesRef.setValue(it)
                venuesRef.push()

                if (!hasHeadCount) {
                    headCountRef.setValue(HeadCount(it.id, 0))
                    headCountRef.push()
                }

                startActivity(VenueActivity.createIntent(this, it.festId, it.id))
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        headCountRef.addListenerForSingleValueEvent(headCountDataListener)
        usersRef.addValueEventListener(usersDataListener)
    }

    override fun onStop() {
        super.onStop()
        headCountRef.removeEventListener(headCountDataListener)
        usersRef.removeEventListener(usersDataListener)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.edit_venue_menu, menu)
        menu?.findItem(R.id.delete_venue)?.icon?.let {
            it.mutate()
            it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_venue -> {
                deleteVenue()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteVenue() {
        venuesRef.removeValue()
        venuesRef.push()

        headCountRef.removeValue()
        headCountRef.push()
        finish()
    }

    private fun createVenue(): Venue? {
        val venueName = binding.venueNameEdittext.text.trim()
        if (venueName.isNullOrBlank()) {
            binding.venueNameInputLayout.error = resources.getString(R.string.venue_name_required)
            return null
        }

        val venueCapacity = binding.venueCapcityEdittext.text
        if (venueCapacity.isNullOrBlank()) {
            binding.venueCapacityInputLayout.error = resources.getString(R.string.venue_capacity_required)
            return null
        }

        val venueCapacityInt: Int? = try {
            venueCapacity.toString().toInt()
        } catch (ex: Exception) {
            null
        }

        if (venueCapacityInt == null || venueCapacityInt <= 0) {
            binding.venueCapacityInputLayout.error = resources.getString(R.string.venue_capacity_must_be_number)
            return null
        }

        val doorKeeperEmailAddrs = binding.venueDoorkeepersEdittext.chipValues.distinct()
        val emailToUserIds = users.associateBy({ it.email }, { it.id })
        val unknownDoorKeeperEmailAddrs = doorKeeperEmailAddrs.filter { !emailToUserIds.containsKey(it) }
        if (unknownDoorKeeperEmailAddrs.isNotEmpty()) {
            binding.venueDoorkeepersInputLayout.error = getString(R.string.unknown_emails) + unknownDoorKeeperEmailAddrs.joinToString()
            return null
        } else {
            binding.venueDoorkeepersInputLayout.error = null
        }
        
        val doorKeepers = doorKeeperEmailAddrs
                .filter { !emailToUserIds[it].isNullOrEmpty() }
                .map { User(id = emailToUserIds[it]!!, email = it) }
                .associate { it.id to it }

        return argVenue.copy(name = venueName.toString(), capacity = venueCapacityInt, doorKeeperIds = doorKeepers)
    }

    private val headCountDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            snapshot?.getValue(HeadCount::class.java)?.let {
                hasHeadCount = true
            }
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    private val usersDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            users = snapshot?.children?.mapNotNull { it.getValue(User::class.java) } ?: emptyList()
            if (!hasProcessedUsers) {
                val festAdminUsers = argVenue.doorKeeperIds.keys.mapNotNull { doorKeeperId -> users.firstOrNull { it.id == doorKeeperId } }
                binding.venueDoorkeepersEdittext.setText(festAdminUsers.mapNotNull { it.email })
                hasProcessedUsers = true
            }
            binding.venueDoorkeepersEdittext.setAdapter(NachoUtils.genUserEmailAdapter(this@EditVenueActivity, users))
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    companion object {
        private const val ARG_VENUE = "ARG_VENUE"

        fun generateNewIntent(context: Context, venue: Venue) = Intent(context, EditVenueActivity::class.java).apply {
            putExtra(ARG_VENUE, venue)
        }
    }

}
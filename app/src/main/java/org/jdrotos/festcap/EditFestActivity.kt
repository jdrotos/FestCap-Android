package org.jdrotos.festcap

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import org.jdrotos.festcap.data.DataKeys
import org.jdrotos.festcap.data.Fest
import org.jdrotos.festcap.databinding.ActivityEditFestBinding
import com.google.firebase.database.*
import org.jdrotos.festcap.data.User
import org.jdrotos.festcap.utils.NachoUtils
import timber.log.Timber


/**
 * This is the activity for editing a venue
 */
class EditFestActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditFestBinding

    private var users = emptyList<User>()
    private var hasProcessedUsers = false

    private val argFest: Fest by lazy {
        intent.getParcelableExtra<Fest>(ARG_FEST)
    }

    private val festsRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.FESTS).child(argFest.id)
        }
    }

    private val usersRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.USERS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_fest)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.festNameEdittext.setText(argFest.name)

        NachoUtils.setupStandardChipsForEmail(binding.festAdminsEdittext)
        NachoUtils.setupStandardChipsForEmail(binding.festMembersEdittext)

        binding.createBtn.setOnClickListener {
            createFest()?.let {
                festsRef.setValue(it)
                festsRef.push()
                startActivity(VenueActivity.createIntent(this, it.id, null))
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        usersRef.addValueEventListener(usersDataListener)
    }

    override fun onStop() {
        super.onStop()
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
                deleteFest()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteFest() {
        festsRef.removeValue()
        festsRef.push()
        finish()
    }

    private fun createFest(): Fest? {
        var errors = 0

        val festName = binding.festNameEdittext.text.trim()
        if (festName.isNullOrBlank()) {
            binding.festNameInputLayout.error = resources.getString(R.string.fest_name_required)
            errors++
        } else {
            binding.festNameInputLayout.error = null
        }

        val adminEmailAddrs = binding.festAdminsEdittext.chipValues.distinct()
        if (adminEmailAddrs.isEmpty()) {
            binding.festAdminsInputLayout.error = getString(R.string.no_admins_entered)
            errors++
        } else {
            binding.festAdminsInputLayout.error = null
        }
        val emailToUserIds = users.associateBy({ it.email }, { it.id })
        val unknownAdminEmailAddrs = adminEmailAddrs.filter { !emailToUserIds.containsKey(it) }
        if (unknownAdminEmailAddrs.isNotEmpty()) {
            binding.festAdminsInputLayout.error = getString(R.string.unknown_emails) + unknownAdminEmailAddrs.joinToString()
            errors++
        } else {
            binding.festAdminsInputLayout.error = null
        }

        val memberEmailAddrs = binding.festMembersEdittext.chipValues.distinct()
        val unknownMemberEmailAddrs = memberEmailAddrs.filter { !emailToUserIds.containsKey(it) }
        if (unknownMemberEmailAddrs.isNotEmpty()) {
            binding.festMembersInputLayout.error = getString(R.string.unknown_emails) + unknownMemberEmailAddrs.joinToString()
            errors++
        } else {
            binding.festMembersInputLayout.error = null
        }


        if (errors > 0) {
            return null
        }

        val adminIds = adminEmailAddrs.mapNotNull { emailToUserIds[it] }.associate { it to true }
        val memberIds = memberEmailAddrs.mapNotNull { emailToUserIds[it] }.associate { it to true }
        return argFest.copy(name = festName.toString(), adminIds = adminIds, memberIds = memberIds)
    }


    private val usersDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            users = snapshot?.children?.mapNotNull { it.getValue(User::class.java) } ?: emptyList()
            if (!hasProcessedUsers) {
                val festAdminUsers = argFest.adminIds.keys.mapNotNull { adminId -> users.firstOrNull { it.id == adminId } }
                val festMemberUsers = argFest.memberIds.keys.mapNotNull { memberId -> users.firstOrNull { it.id == memberId } }
                binding.festAdminsEdittext.setText(festAdminUsers.mapNotNull { it.email })
                binding.festMembersEdittext.setText(festMemberUsers.mapNotNull { it.email })
                hasProcessedUsers = true
            }
            binding.festAdminsEdittext.setAdapter(NachoUtils.genUserEmailAdapter(this@EditFestActivity, users))
            binding.festMembersEdittext.setAdapter(NachoUtils.genUserEmailAdapter(this@EditFestActivity, users))
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    companion object {
        private const val ARG_FEST = "ARG_FEST"

        fun generateNewIntent(context: Context, fest: Fest) = Intent(context, EditFestActivity::class.java).apply {
            putExtra(ARG_FEST, fest)
        }
    }

}
package org.jdrotos.festcap

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jdrotos.festcap.data.DataKeys
import org.jdrotos.festcap.data.Fest
import org.jdrotos.festcap.data.Venue
import org.jdrotos.festcap.databinding.ActivityEditFestBinding
import org.jdrotos.festcap.databinding.ActivityEditVenueBinding


/**
 * This is the activity for editing a venue
 */
class EditFestActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditFestBinding

    private val argFest: Fest by lazy {
        intent.getParcelableExtra<Fest>(ARG_FEST)
    }

    private val festsRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.FESTS).child(argFest.id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_fest)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.festNameEdittext.setText(argFest.name)

        // TODO: Setup fest admins

        binding.createBtn.setOnClickListener {
            createVenue()?.let {
                festsRef.setValue(it)
                festsRef.push()
                startActivity(VenueActivity.createIntent(this, it.id, null))
                finish()
            }
        }
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

    private fun createVenue(): Fest? {
        val venueName = binding.festNameEdittext.text.trim()
        if (venueName.isNullOrBlank()) {
            binding.festNameInputLayout.error = resources.getString(R.string.fest_name_required)
            return null
        }

        // TODO: validate admins

        return argFest.copy(name = venueName.toString())
    }


    companion object {
        private const val ARG_FEST = "ARG_FEST"

        fun generateNewIntent(context: Context, fest: Fest) = Intent(context, EditFestActivity::class.java).apply {
            putExtra(ARG_FEST, fest)
        }
    }

}
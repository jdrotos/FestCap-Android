package org.jdrotos.festcap

import android.app.DialogFragment
import android.app.Fragment
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jdrotos.festcap.data.DataKeys
import org.jdrotos.festcap.data.Fest
import org.jdrotos.festcap.data.HeadCount
import org.jdrotos.festcap.data.Venue
import org.jdrotos.festcap.databinding.FragmentVenueBinding
import org.jdrotos.festcap.utils.PermissionChecker
import timber.log.Timber
import java.util.*

/**
 * Created by jdrotos on 11/19/17.
 */
class VenueFragment : Fragment() {

    private lateinit var binding: FragmentVenueBinding

    private lateinit var venue: Venue
    private lateinit var fest: Fest

    private val venueInitialized
        get() = this::venue.isInitialized
    private var headCount = 0L

    private val festRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(DataKeys.FESTS + "/$festId")
    }

    private val venueRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(DataKeys.VENUES + "/$venueId")
    }

    private val headCountRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(DataKeys.HEADCOUNTS + "/$venueId")
    }

    private val festId by lazy {
        arguments.getString(ARG_FEST_ID)
    }

    private val venueId by lazy {
        arguments.getString(ARG_VENUE_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_venue, container, false)

        binding.plusBtn.setOnClickListener {
            incrementHeadcount(1)
        }

        binding.minusBtn.setOnClickListener {
            incrementHeadcount(-1)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.venue_fragment_menu, menu)
        menu.findItem(R.id.edit_venue)?.icon?.let {
            it.mutate()
            it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val canEditFest = this::fest.isInitialized && FirebaseAuth.getInstance().currentUser?.let {
            PermissionChecker.canAdminFest(it, fest)
        } ?: false
        menu?.findItem(R.id.edit_venue)?.isEnabled = canEditFest
        menu?.findItem(R.id.edit_venue)?.isVisible = canEditFest
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.edit_venue) {
            startActivity(EditVenueActivity.generateNewIntent(activity, venue))
            return true
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        festRef.addValueEventListener(festsDataListener)
        venueRef.addValueEventListener(venueDataListener)
        headCountRef.addValueEventListener(headCountDataListener)
    }

    override fun onStop() {
        super.onStop()
        festRef.removeEventListener(festsDataListener)
        venueRef.removeEventListener(venueDataListener)
        headCountRef.removeEventListener(headCountDataListener)
    }

    private fun incrementHeadcount(increment: Int) {
        val headCount = Math.max(0, this.headCount + increment)
        headCountRef.setValue(HeadCount(venueId, headCount))
        headCountRef.push()
    }

    private fun bindVenue(venue: Venue) {
        this.venue = venue

        activity.title = venue.name
        binding.headcountTv.text = "$headCount/"
        binding.capacityTv.text = venue.capacity.toString()
    }

    private fun bindFest(fest: Fest) {
        this.fest = fest
        (activity as? AppCompatActivity)?.let {
            it.supportInvalidateOptionsMenu()
        }
    }

    private val venueDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            snapshot?.getValue(Venue::class.java)?.let {
                bindVenue(it)
            }
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    private val festsDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            snapshot?.getValue(Fest::class.java)?.let {
                bindFest(it)
            }
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    private val headCountDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            snapshot?.getValue(HeadCount::class.java)?.let {
                headCount = it.headCount
                if (venueInitialized) {
                    bindVenue(venue)
                }
            }
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    companion object {
        private const val FTAG_EDIT_VENUE = "FTAG_EDIT_VENUE"

        private const val ARG_FEST_ID = "ARG_FEST_ID"
        private const val ARG_VENUE_ID = "ARG_VENUE_ID"
        fun newInstance(festId: String, venueId: String) = VenueFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEST_ID, festId)
                putString(ARG_VENUE_ID, venueId)
            }
        }

    }
}
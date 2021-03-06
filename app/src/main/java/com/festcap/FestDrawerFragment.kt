package com.festcap

import android.app.Fragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.festcap.data.DataKeys
import com.festcap.data.Fest
import com.festcap.data.HeadCount
import com.festcap.data.Venue
import com.festcap.databinding.FragmentFestDrawerBinding
import timber.log.Timber

/**
 *
 */
class FestDrawerFragment : Fragment() {

    private lateinit var binding: FragmentFestDrawerBinding
    private lateinit var adapter: VenueAdapter
    private lateinit var venuesViewModel: VenuesViewModel

    private val headCountsRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(DataKeys.HEADCOUNTS)
    }

    private val venuesRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(DataKeys.VENUES)
    }

    private val festRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference(DataKeys.FESTS)
    }

    private val festId by lazy {
        arguments.getString(ARG_FEST_ID)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        venuesViewModel = ViewModelProviders.of(context as AppCompatActivity).get(VenuesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_fest_drawer, container, false)

        binding.festNameTv.setOnClickListener {
            startActivity(FestsActivity.createIntent(activity))
            activity.finish()
        }

        binding.recycler.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.VERTICAL, false)
        adapter = VenueAdapter( {
            startActivity(EditVenueActivity.generateNewIntent(activity, Venue(festId = festId)))

        }).also {
            binding.recycler.adapter = it
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        venuesRef.addValueEventListener(venueDataListener)
        festRef.addValueEventListener(festDataListener)
        headCountsRef.addValueEventListener(headCountDataListener)
        venuesViewModel.venueId.observe(this.activity as AppCompatActivity, Observer {
            adapter.selectedVenueId = it
        })
    }

    override fun onStop() {
        super.onStop()
        venuesRef.removeEventListener(venueDataListener)
        festRef.removeEventListener(festDataListener)
        headCountsRef.removeEventListener(headCountDataListener)
    }

    private val venueDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            adapter.venues = snapshot?.children?.mapNotNull { it.getValue(Venue::class.java) }?.filter { it.festId == festId }?.sortedBy { it.name } ?: emptyList<Venue>()
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    private val festDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            snapshot?.children?.mapNotNull { it.getValue(Fest::class.java) }?.find { it.id == festId }?.let {fest ->
                binding.festNameTv.text = fest.name

                val allowCreateVenue = FirebaseAuth.getInstance().currentUser?.uid?.let {
                    fest.adminIds.containsKey(it)
                } ?: false

                adapter.allowCreate = allowCreateVenue
            }
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    private val headCountDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            adapter.headCounts = snapshot?.children?.mapNotNull { it.getValue(HeadCount::class.java) }?.associateBy({ it.id }) ?: emptyMap()
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    companion object {
        private const val ARG_FEST_ID = "ARG_FEST_ID"

        fun newInstance(festId: String) = FestDrawerFragment().apply {
            arguments = Bundle().apply { putString(ARG_FEST_ID, festId) }
        }
    }
}
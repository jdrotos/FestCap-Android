package com.festcap


import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.festcap.data.DataKeys
import com.festcap.data.Fest
import com.festcap.databinding.ActivityFestsBinding
import timber.log.Timber

/**
 * Created by jdrotos on 11/18/17.
 */
class FestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFestsBinding
    private lateinit var adapter: FestsAdapter

    private val festsRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().let {
            it.getReference(DataKeys.FESTS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fests)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        adapter = FestsAdapter(true, {
            FirebaseAuth.getInstance().currentUser?.let { u ->
                val fest = Fest(creatorId = u.uid, memberIds = mapOf(u.uid to mapOf(u.uid to true)), adminIds = mapOf(u.uid to true))
                startActivity(EditFestActivity.generateNewIntent(this, fest, true))
            }
        })
        binding.recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycler.itemAnimator = DefaultItemAnimator()
        binding.recycler.adapter = adapter
    }


    override fun onStart() {
        super.onStart()
        festsRef.addValueEventListener(festsDataListener)
    }

    override fun onStop() {
        super.onStop()
        festsRef.removeEventListener(festsDataListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.fest_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LaunchActivity::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val festsDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot?) {
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                adapter.fests = snapshot?.children
                        ?.mapNotNull { it.getValue(Fest::class.java) }
                        ?.filter { it.adminIds.containsKey(userId) || it.memberIds.containsKey(userId) }
                        ?: emptyList<Fest>()
            }
        }

        override fun onCancelled(e: DatabaseError?) {
            Timber.w("Database connection error!")
        }
    }

    companion object {

        fun createIntent(context: Context) =
                Intent(context, FestsActivity::class.java)
    }
}
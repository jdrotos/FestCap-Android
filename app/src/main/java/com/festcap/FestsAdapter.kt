package com.festcap

import android.databinding.DataBindingUtil
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.festcap.data.Fest
import com.festcap.databinding.RowFestBinding
import com.festcap.utils.PermissionChecker
import kotlin.properties.Delegates

/**
 * Created by jdrotos on 11/18/17.
 */
class FestsAdapter(private val allowCreate: Boolean, private val addNewFestClick: () -> Unit)
    : RecyclerView.Adapter<FestsAdapter.FestAdapterVH>() {

    var fests by Delegates.observable(emptyList<Fest>(), { prop, oldval, newval ->
        notifyDataSetChanged()
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestAdapterVH = when (viewType) {
        ROW_TYPE_FEST -> FestAdapterVH.FestRow(parent)
        ROW_TYPE_ADD_FEST -> FestAdapterVH.AddNewFestRow(parent)
        else -> throw  RuntimeException("Invalid viewtype:$viewType")
    }

    override fun onBindViewHolder(holder: FestAdapterVH, position: Int) = when (holder) {
        is FestAdapterVH.FestRow -> holder.bind(fests[position])
        is FestAdapterVH.AddNewFestRow -> holder.itemView.setOnClickListener { addNewFestClick.invoke() }
    }

    override fun getItemViewType(position: Int): Int {
        if (allowCreate && position == (itemCount - 1)) {
            return ROW_TYPE_ADD_FEST
        }
        return ROW_TYPE_FEST
    }

    override fun getItemCount(): Int = fests.size + if (allowCreate) 1 else 0

    sealed class FestAdapterVH(parent: ViewGroup, @LayoutRes layoutRes: Int)
        : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {
        class FestRow(parent: ViewGroup) : FestAdapterVH(parent, R.layout.row_fest) {

            private val binding: RowFestBinding

            init {
                binding = DataBindingUtil.bind(itemView)
            }

            fun bind(fest: Fest) {
                binding.festName.text = fest.name
                itemView.setOnClickListener {
                    itemView.context.startActivity(VenueActivity.createIntent(itemView.context, fest.id))
                }

                val canEditFest = FirebaseAuth.getInstance().currentUser?.let {
                    PermissionChecker.canAdminFest(it, fest)
                } ?: false
                binding.editFest.visibility = if (canEditFest) View.VISIBLE else View.GONE
                binding.editFest.setOnClickListener {
                    itemView.context.startActivity(EditFestActivity.generateNewIntent(itemView.context, fest, false))
                }
            }
        }

        class AddNewFestRow(parent: ViewGroup) : FestAdapterVH(parent, R.layout.row_add_fest)
    }

    companion object {
        private const val ROW_TYPE_FEST = 0
        private const val ROW_TYPE_ADD_FEST = 1
    }
}

package org.jdrotos.festcap

import android.arch.lifecycle.Transformations.map
import android.databinding.DataBindingUtil
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.jdrotos.festcap.data.HeadCount
import org.jdrotos.festcap.data.Venue
import org.jdrotos.festcap.databinding.RowVenueBinding
import kotlin.properties.Delegates

/**
 * Created by jdrotos on 11/18/17.
 */
class VenueAdapter(private val addNewVenueClick: () -> Unit)
    : RecyclerView.Adapter<VenueAdapter.VenueAdapterVH>() {

    var allowCreate: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var venues by Delegates.observable(emptyList<Venue>(), { prop, oldval, newval ->
        notifyDataSetChanged()
    })

    var headCounts by Delegates.observable(emptyMap<String, HeadCount>(), { prop, oldval, newval ->
        notifyDataSetChanged()
    })

    var selectedVenueId: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VenueAdapterVH = when (viewType) {
        ROW_TYPE_VENUE -> VenueAdapterVH.VenueRow(parent)
        ROW_TYPE_ADD_VENUE -> VenueAdapterVH.AddNewVenueRow(parent)
        else -> throw  RuntimeException("Invalid viewtype:$viewType")
    }

    override fun onBindViewHolder(holder: VenueAdapterVH, position: Int) = when (holder) {
        is VenueAdapterVH.VenueRow -> {
            val venue = venues[position]
            val headCount = headCounts[venue.id]?.headCount ?: 0L
            holder.bind(venue, headCount, venue.id == selectedVenueId)
        }
        is VenueAdapterVH.AddNewVenueRow -> holder.itemView.setOnClickListener { addNewVenueClick.invoke() }
    }

    override fun getItemViewType(position: Int): Int {
        if (allowCreate && position == (itemCount - 1)) {
            return ROW_TYPE_ADD_VENUE
        }
        return ROW_TYPE_VENUE
    }

    override fun getItemCount(): Int = venues.size + if (allowCreate) 1 else 0

    sealed class VenueAdapterVH(parent: ViewGroup, @LayoutRes layoutRes: Int)
        : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {
        class VenueRow(parent: ViewGroup) : VenueAdapterVH(parent, R.layout.row_venue) {

            private val binding: RowVenueBinding

            init {
                binding = DataBindingUtil.bind(itemView)
            }

            fun bind(venue: Venue, headCount: Long, selected: Boolean) {
                val colorRes = if (selected) R.color.colorPrimary else R.color.dark_text
                val color = ContextCompat.getColor(itemView.context, colorRes)
                binding.venueName.setTextColor(color)
                binding.venueHeadcount.setTextColor(color)
                binding.venueIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

                binding.venueName.text = venue.name
                binding.venueHeadcount.text = "$headCount / ${venue.capacity}"
                itemView.setOnClickListener {
                    itemView.context.startActivity(VenueActivity.createIntent(itemView.context, venue.festId, venue.id))
                }
            }
        }

        class AddNewVenueRow(parent: ViewGroup) : VenueAdapterVH(parent, R.layout.row_add_venue)
    }

    companion object {
        private const val ROW_TYPE_VENUE = 0
        private const val ROW_TYPE_ADD_VENUE = 1
    }
}

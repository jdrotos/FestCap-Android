package org.jdrotos.festcap.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by jdrotos on 11/18/17.
 */
@Parcelize
data class Fest(override val id: String = UUID.randomUUID().toString(),
                val name: String = "",
                val creatorId: String = "",
                val adminIds: Map<String, Boolean> = emptyMap(),
                val memberIds: Map<String, Boolean> = emptyMap(),
                val venueIds: List<String> = emptyList())
    : Identifiable, Parcelable
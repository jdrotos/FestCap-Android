package com.festcap.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by jdrotos on 11/18/17.
 */
@Parcelize
data class Venue(override val id: String = UUID.randomUUID().toString(),
                 val festId: String = "",
                 val name: String = "",
                 val capacity: Int = 0,
                 val doorKeeperIds: Map<String, Map<String, Boolean>> = emptyMap())
    : Identifiable, Parcelable
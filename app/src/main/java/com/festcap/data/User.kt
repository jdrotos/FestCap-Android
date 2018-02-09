package com.festcap.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by jdrotos on 11/19/17.
 */
@Parcelize
data class User(override val id: String = UUID.randomUUID().toString(),
                val name: String? = null,
                val email: String = "",
                val fests: List<String> = emptyList())
    : Identifiable, Parcelable
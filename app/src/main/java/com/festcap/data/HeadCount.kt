package com.festcap.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by jdrotos on 11/18/17.
 */
@Parcelize
data class HeadCount(override val id: String = "", val headCount: Long = 0)
    : Identifiable, Parcelable
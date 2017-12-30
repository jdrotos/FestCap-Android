package org.jdrotos.festcap

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.MutableLiveData

class VenuesViewModel : ViewModel() {
    val festId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val venueId: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
}
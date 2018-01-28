package org.jdrotos.festcap.utils

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.validator.ChipifyingNachoValidator
import org.jdrotos.festcap.data.User

/**
 * Created by jdrotos on 1/28/18.
 */
object NachoUtils {
    fun setupStandardChipsForEmail(tv: NachoTextView) {
        tv.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tv.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tv.addChipTerminator('\t', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tv.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tv.addChipTerminator(';', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        tv.enableEditChipOnTouch(false, true)
        tv.setNachoValidator(ChipifyingNachoValidator())
    }

    fun genUserEmailAdapter(context: Context, users: List<User>): ArrayAdapter<String> {
        val suggestions = users.mapNotNull { it.email }.toTypedArray()
        return ArrayAdapter(context, R.layout.simple_dropdown_item_1line, suggestions)
    }
}
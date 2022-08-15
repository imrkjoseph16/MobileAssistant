package com.imrkjoseph.echomobileassistant.activity

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.imrkjoseph.echomobileassistant.app.base.BaseViewModel

class FacilityViewModel : BaseViewModel() {

    fun checkPermissions(
        context: Context?,
        vararg permissions: Array<out String>
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {

            permissions.forEach {
                if (ContextCompat.checkSelfPermission(context, it.toString()) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun permissionStateChecker(grantResults: IntArray): Boolean {
        var isPermitted = false

        grantResults.forEach {
            isPermitted = it == PackageManager.PERMISSION_GRANTED
        }
        return isPermitted
    }
}
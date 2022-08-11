package com.imrkjoseph.fibermobileassistant.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.imrkjoseph.fibermobileassistant.app.Actions
import com.imrkjoseph.fibermobileassistant.app.base.BaseActivity
import com.imrkjoseph.fibermobileassistant.app.common.ServiceState
import com.imrkjoseph.fibermobileassistant.app.common.Utils.Companion.PERMISSIONS_RECORD_AUDIO
import com.imrkjoseph.fibermobileassistant.app.common.getServiceState
import com.imrkjoseph.fibermobileassistant.databinding.ActivityFacilityBinding
import com.imrkjoseph.fibermobileassistant.service.FiberService

class FacilityActivity : BaseActivity<ActivityFacilityBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityFacilityBinding
        get() = ActivityFacilityBinding::inflate

    override fun onViewsBound() {
        super.onViewsBound()
        checkPermission()
        setupService(Actions.START)
    }

    private fun checkPermission() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_RECORD_AUDIO)
            return
        }
    }

    private fun setupService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, FiberService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED
            ) {
                setupService(Actions.START)
            } else {
                finish()
            }
        }
    }
}
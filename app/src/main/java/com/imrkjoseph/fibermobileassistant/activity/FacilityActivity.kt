package com.imrkjoseph.fibermobileassistant.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.PERMISSIONS_RECORD_AUDIO
import com.imrkjoseph.fibermobileassistant.app.Default.Companion.PERMISSION_DRAW_OVER_OVERLAY
import com.imrkjoseph.fibermobileassistant.app.base.BaseActivity
import com.imrkjoseph.fibermobileassistant.app.common.helper.Utils.Companion.getServiceState
import com.imrkjoseph.fibermobileassistant.app.common.navigation.Actions
import com.imrkjoseph.fibermobileassistant.databinding.ActivityFacilityBinding
import com.imrkjoseph.fibermobileassistant.service.FiberService
import com.imrkjoseph.fibermobileassistant.service.ServiceEnum

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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, PERMISSION_DRAW_OVER_OVERLAY)
            return
        }
    }

    private fun setupService(action: Actions) {
        if (getServiceState(this) == ServiceEnum.STOPPED && action == Actions.STOP) return
        Intent(this, FiberService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_RECORD_AUDIO ||
            requestCode == PERMISSION_DRAW_OVER_OVERLAY
        ) {
            if (grantResults.isNotEmpty() && grantResults[0]
                == PackageManager.PERMISSION_GRANTED
                && Settings.canDrawOverlays(this)
            ) {
                setupService(Actions.START)
            } else {
                finish()
            }
        }
    }
}
package com.imrkjoseph.echomobileassistant.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.PERMISSIONS_ECHO
import com.imrkjoseph.echomobileassistant.app.base.BaseActivity
import com.imrkjoseph.echomobileassistant.app.common.Default.Companion.getEchoPermissions
import com.imrkjoseph.echomobileassistant.app.common.helper.Utils.Companion.getServiceState
import com.imrkjoseph.echomobileassistant.app.common.navigation.Actions
import com.imrkjoseph.echomobileassistant.databinding.ActivityFacilityBinding
import com.imrkjoseph.echomobileassistant.service.EchoService
import com.imrkjoseph.echomobileassistant.service.ServiceEnum

class FacilityActivity : BaseActivity<ActivityFacilityBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityFacilityBinding
        get() = ActivityFacilityBinding::inflate

    private val viewModel by viewModels<FacilityViewModel>()

    override fun onViewsBound() {
        super.onViewsBound()
        checkPermission()
    }

    private fun checkPermission() {
        if (!viewModel.checkPermissions(this, getEchoPermissions())) {
            requestPermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && canOverlayLayout().not()) {
            openOverlayPermission()
        } else {
            setupService(action = Actions.START)
        }
    }

    private fun requestPermission() = ActivityCompat.requestPermissions(
        this, getEchoPermissions(),
        PERMISSIONS_ECHO)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openOverlayPermission(){
        // If the draw over permission is not available open the settings screen
        // to grant the permission.
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startForResultPermission.launch(intent)
    }

    private fun setupService(action: Actions) {
        if (getServiceState(this) == ServiceEnum.STOPPED && action == Actions.STOP) return
        Intent(this, EchoService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(it)
            else startService(it)
            finish()
        }
    }

    private val startForResultPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        setupService(action = Actions.START).takeIf { 
            result.resultCode == Activity.RESULT_OK && canOverlayLayout()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val isPermissionGranted = viewModel.permissionStateChecker(grantResults)

        if (requestCode == PERMISSIONS_ECHO) {
            when {
                grantResults.isNotEmpty() 
                && isPermissionGranted 
                && canOverlayLayout() -> setupService(action = Actions.START)
                isPermissionGranted.not() -> requestPermission()
                canOverlayLayout().not() -> openOverlayPermission()
                else -> finish()
            }
        }
    }
    
    private fun canOverlayLayout() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else false 
}
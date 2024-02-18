package com.alonsoapp.storagepermissionshandlercompose.StoragePermissionsHandler

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

class PermissionRequesterStorage(
    private val componentActivity: ComponentActivity,
    private val context: Context
) {
    private fun isAndroid11OrAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    private var grantedPermission: @Composable () -> Unit = {}
    private val storagePermissionsArray = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun hasPermissions(onGrantedPermission: @Composable () -> Unit): Boolean {
        // Check if the API level is >= 30.
        return if (isAndroid11OrAbove()) {
            grantedPermission = onGrantedPermission
            Environment.isExternalStorageManager()
        }
        // If the API level is < 30, check the permissions in the traditional way.
        else {
            grantedPermission = onGrantedPermission
            checkArrayStoragePermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun requestPermission() {
        if (isAndroid11OrAbove()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", context.applicationContext.packageName))
                resultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                resultLauncher.launch(intent)
            }
        } else {
            storagePermissionLauncher.launch(storagePermissionsArray)
        }
    }

    private fun checkArrayStoragePermissions(): Boolean {
        for (permission in storagePermissionsArray) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /*
    * Register ForActivity Result
    * */

    /**
     * Permission result receiver after user has completed the specified action.
     * Called only for API level >= 30
     * */
    @RequiresApi(Build.VERSION_CODES.R)
    private val resultLauncher =
        componentActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android is 11 (R) or above
                if (Environment.isExternalStorageManager()) {
                    componentActivity.setContent { grantedPermission() }
                } else {
                    Toast.makeText(context, "Accept permissions to continue", Toast.LENGTH_LONG)
                        .show()
                    requestPermission()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private val storagePermissionLauncher = componentActivity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissions = permissions.all { it.value }
        if (allPermissions) {
            componentActivity.setContent {
                grantedPermission
            }
        } else {
            Toast.makeText(context, "Accept permissions to continue", Toast.LENGTH_LONG).show()
            requestPermission()
        }
    }
}
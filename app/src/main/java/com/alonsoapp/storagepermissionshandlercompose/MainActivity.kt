package com.alonsoapp.storagepermissionshandlercompose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alonsoapp.storagepermissionshandlercompose.Screen.HomeScreen
import com.alonsoapp.storagepermissionshandlercompose.ui.theme.PermissioStorageJCTheme
import com.alonsoapp.storagepermissionshandlercompose.StoragePermissionsHandler.PermissionRequesterStorage

class MainActivity : ComponentActivity() {
    private val permissionRequesterStorage by lazy {
        PermissionRequesterStorage(
            componentActivity = this,
            context = this
        )
    }
    private val goToHome: @Composable () -> Unit = { HomeScreen() }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        val hasPermissions = permissionRequesterStorage.hasPermissions {
            goToHome()
        }
        setContentComposable {
            if (hasPermissions) goToHome() else {
                permissionRequesterStorage.requestPermission()
            }
        }
    }


    private fun setContentComposable(content: @Composable () -> Unit) {
        setContent {
            PermissioStorageJCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    content()
                }
            }
        }
    }
}


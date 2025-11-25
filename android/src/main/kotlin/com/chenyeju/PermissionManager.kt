package com.chenyeju

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker

/**
 * Manages camera permissions
 */
class PermissionManager {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1230

        private fun getRequiredPermissions(): Array<String> {
            // From Android 10 (API 29) and above, WRITE_EXTERNAL_STORAGE is deprecated/ignored.
            // We need CAMERA for opening/previewing the UVC camera and RECORD_AUDIO for video recording with audio.
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
        }
        
        /**
         * Check if camera and storage permissions are granted
         */
        fun hasRequiredPermissions(context: Context): Boolean {
            val permissions = getRequiredPermissions()
            return permissions.all { perm -> isPermissionGranted(context, perm) }
        }
        
        /**
         * Request camera and storage permissions
         * @return true if permissions already granted, false if request was made
         */
        fun requestPermissionsIfNeeded(activity: Activity?): Boolean {
            if (activity == null) {
                return false
            }
            
            if (hasRequiredPermissions(activity.applicationContext)) {
                return true
            }
            
            val permissionsToRequest = getRequiredPermissions()
                .filter { perm -> !isPermissionGranted(activity.applicationContext, perm) }
                .toTypedArray()

            if (permissionsToRequest.isEmpty()) {
                return true
            }

            ActivityCompat.requestPermissions(activity, permissionsToRequest, PERMISSION_REQUEST_CODE)
            return false
        }
        
        /**
         * Check if permission result is successful
         */
        fun isPermissionGranted(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
            if (requestCode != PERMISSION_REQUEST_CODE) {
                return false
            }
            
            return grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
        
        /**
         * Get permission request code
         */
        fun getPermissionRequestCode() = PERMISSION_REQUEST_CODE
    }
} 
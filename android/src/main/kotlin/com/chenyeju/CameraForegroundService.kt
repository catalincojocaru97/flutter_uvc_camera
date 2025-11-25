package com.chenyeju

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Foreground service to keep camera running when screen is off or app is in background.
 * This prevents Android from killing the app or throttling camera FPS.
 */
class CameraForegroundService : Service() {
    
    companion object {
        private const val TAG = "CameraForegroundService"
        private const val CHANNEL_ID = "uvc_camera_channel"
        private const val CHANNEL_NAME = "UVC Camera"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.chenyeju.action.START_CAMERA_SERVICE"
        const val ACTION_STOP = "com.chenyeju.action.STOP_CAMERA_SERVICE"
        const val ACTION_UPDATE_RECORDING = "com.chenyeju.action.UPDATE_RECORDING"
        
        const val EXTRA_IS_RECORDING = "is_recording"
        
        private var isRunning = false
        
        /**
         * Check if the service is currently running
         */
        fun isServiceRunning(): Boolean = isRunning
        
        /**
         * Start the foreground service
         */
        fun start(context: Context) {
            Log.i(TAG, "start() called, isRunning=$isRunning")
            if (isRunning) {
                Log.i(TAG, "Service already running, skipping start")
                return
            }
            
            try {
                val intent = Intent(context, CameraForegroundService::class.java).apply {
                    action = ACTION_START
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.i(TAG, "Service start intent sent")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service", e)
            }
        }
        
        /**
         * Stop the foreground service
         */
        fun stop(context: Context) {
            Log.i(TAG, "stop() called, isRunning=$isRunning")
            if (!isRunning) {
                Log.i(TAG, "Service not running, skipping stop")
                return
            }
            
            try {
                val intent = Intent(context, CameraForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
                Log.i(TAG, "Service stop intent sent")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop foreground service", e)
            }
        }
        
        /**
         * Update the notification to show recording status
         */
        fun updateRecordingStatus(context: Context, isRecording: Boolean) {
            Log.i(TAG, "updateRecordingStatus() called, isRecording=$isRecording, isRunning=$isRunning")
            if (!isRunning) return
            
            try {
                val intent = Intent(context, CameraForegroundService::class.java).apply {
                    action = ACTION_UPDATE_RECORDING
                    putExtra(EXTRA_IS_RECORDING, isRecording)
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update recording status", e)
            }
        }
    }
    
    private var notificationManager: NotificationManager? = null
    private var isRecording = false
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate()")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand() action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                isRunning = true
                isRecording = false
                Log.i(TAG, "Starting foreground with notification")
                startForeground(NOTIFICATION_ID, buildNotification())
            }
            ACTION_STOP -> {
                Log.i(TAG, "Stopping foreground service")
                isRunning = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE_RECORDING -> {
                isRecording = intent.getBooleanExtra(EXTRA_IS_RECORDING, false)
                Log.i(TAG, "Updating notification, isRecording=$isRecording")
                notificationManager?.notify(NOTIFICATION_ID, buildNotification())
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "UVC Camera service notification"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): Notification {
        // Create pending intent to open the app when notification is tapped
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }
        
        val title = if (isRecording) "Recording in progress" else "Camera active"
        val text = if (isRecording) "Tap to return to app" else "UVC camera is running"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_camera_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .apply {
                if (pendingIntent != null) {
                    setContentIntent(pendingIntent)
                }
            }
            .build()
    }
}


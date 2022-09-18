package com.example.gassensorapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.properties.Delegates

class NewService : Service() {

    lateinit var mainHandler: Handler

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getRequest(mute: Boolean, pendingIntent: PendingIntent) {
        var builder = NotificationCompat.Builder(this, "0")
            .setSmallIcon(R.drawable.alert)
            .setContentTitle("Danger! Leak Detected!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Our sensors have detected a gas leakage. Hurry, take the required action."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if (!mute) {
            val stringRequest = StringRequest(
                Request.Method.GET,
                "https://api.thingspeak.com/channels/1146999/feeds.json?api_key=YAM3FMGQMI4RU83I&results=2",
                Response.Listener { responseString->
                    val jsonArray = JSONObject(responseString).getJSONArray("feeds")
                    val feeds = jsonArray.getJSONObject(0)
                    val field = feeds.getString("field1")


                    var reading = field.toDouble()


                    if (reading > 300) {
                        with(NotificationManagerCompat.from(this)) {
                            createNotificationChannel()
                            notify(0, builder.build())
                        }
                    }

                },
                Response.ErrorListener { volleyError->


                }
            )

            Volley.newRequestQueue(this).add(stringRequest)
        }
    }

    // execution of service will start
    // on calling this method
    override fun onStartCommand(intent: Intent, flag: Int, startId: Int): Int {
        super.onStartCommand(intent, flag, startId);

        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this@NewService)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        val updateTextTask = object : Runnable {
            override fun run() {
                var mute = prefs.getBoolean("mute", false)
                getRequest(mute, pendingIntent)
                mainHandler.postDelayed(this, 1000)

            }
        }

        mainHandler = Handler(Looper.getMainLooper())

            mainHandler.post(updateTextTask)

        return START_STICKY
    }

    // execution of the service will
    // stop on calling this method
    override fun onDestroy() {
        // stopping the process
            val broadcastIntent = Intent()
            broadcastIntent.action = "restartservice"
            broadcastIntent.setClass(this, Restarter::class.java)
            this.sendBroadcast(broadcastIntent)

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
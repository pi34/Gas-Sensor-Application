package com.example.gassensorapplication

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.System.out
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var mainHandler: Handler

//    lateinit var timePicker1: TimePicker
//    lateinit var timePicker2: TimePicker
//    lateinit var time: TextView
//    lateinit var time2: TextView
//    lateinit var calendar: Calendar
//    private var format = ""

    private lateinit var series: LineGraphSeries<DataPoint>

    private val updateTextTask = object : Runnable {
        override fun run() {
            getRequest()
            mainHandler.postDelayed(this, 1000)

        }
    }

    var lastX: Double = 0.0;

    fun getRequest() {

        val stringRequest = StringRequest(
            Request.Method.GET,
            "https://api.thingspeak.com/channels/1146999/feeds.json?api_key=YAM3FMGQMI4RU83I&results=2",
            Response.Listener {responseString->
                val jsonArray = JSONObject(responseString).getJSONArray("feeds")
                val feeds = jsonArray.getJSONObject(0)
                val field = feeds.getString("field1")

                textView7.text = field

                var reading = field.toDouble()
                var reading_type = "Not Determined"

                if (reading > 300) {
                    reading_type = "Detected"

                } else {
                    reading_type = "Not Detected"
                    textView3.setTextColor(getResources().getColor(R.color.colorSafe))


                }

                textView3.text = reading_type

                series.appendData(DataPoint(lastX++, reading), true, 500)
            },
            Response.ErrorListener {volleyError->

                textView8.text = volleyError.message
            }
        )

        Volley.newRequestQueue(this).add(stringRequest)


    }

//    fun setStartTime(view: View?) {
//        val hour = timePicker1.currentHour
//        val min = timePicker1.currentMinute
//        var textbox = showTime(hour, min)
//        time.text = textbox
//    }

//    fun setEndTime(view: View?) {
//        val hour = timePicker2.currentHour
//        val min = timePicker2.currentMinute
//        var textbox2 = showTime(hour, min)
//        time2.text = textbox2
//    }

//    fun showTime(hour: Int, min: Int): java.lang.StringBuilder {
//        var hour = hour
//        if (hour == 0) {
//            hour += 12
//            format = "AM"
//        } else if (hour == 12) {
//            format = "PM"
//        } else if (hour > 12) {
//            hour -= 12
//            format = "PM"
//        } else {
//            format = "AM"
//        }
//        var textbox = StringBuilder().append(hour).append(" : ").append(min)
//            .append(" ").append(format)

//        return textbox
//    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this, NewService::class.java))

        val another_intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, another_intent, 0)

        mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(updateTextTask)

        series = LineGraphSeries<DataPoint>()
        graph.addSeries(series)

        var viewport = graph.getViewport()
        viewport.setYAxisBoundsManual(true);
        viewport.setScrollable(true);
        viewport.setMaxX(20.0)
        viewport.setMaxY(500.0)

        button1.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            // start your next activity
            startActivity(intent)

        }


        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val username = prefs.getString("device_id", "Default")
        val mute = prefs.getBoolean("mute", false)
        val mute2 = prefs.getBoolean("device_off", false)
        val emergency = prefs.getString("emergency", "Default")

        textView18.text = username

        var status = "Unmute"
        var status2 = "Connected"

        if (mute == true) {
            status = "Mute"
            stopService(Intent(this, NewService::class.java))
        }

        if (mute2 == true) {
            status2 = "Not Connected"
        }

        textView16.text = status

        textView12.text = status2

        var number = "tel:"+emergency

        button3.setOnClickListener {

            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse(number)
            startActivity(callIntent)
        }

        button2.setOnClickListener {

            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:101")
            startActivity(callIntent)
        }


//        timePicker1 = findViewById(R.id.timePicker1) as TimePicker
//        timePicker2 = findViewById(R.id.timePicker2) as TimePicker
//        time = findViewById(R.id.textView1) as TextView
//        time2 = findViewById(R.id.textView26) as TextView
//        calendar = Calendar.getInstance()

//        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
//        val min: Int = calendar.get(Calendar.MINUTE)
//       showTime(hour, min)

    }}

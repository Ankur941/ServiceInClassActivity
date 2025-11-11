package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.TextView
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false
    private lateinit var timerTextView : TextView
    private var isRunning = false

    val timerHandler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message){
            timerTextView.text = msg.what.toString()
        }
    }


     private val serviceConnection = object: ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(timerHandler)
            isConnected = true
        }

         override fun onServiceDisconnected(p0: ComponentName?) {
                 isConnected = false
         }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.textView)
        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        val startButton = findViewById<Button>(R.id.startButton)



        findViewById<Button>(R.id.startButton).setOnClickListener {
            //startService(Intent(this, TimerService::class.java))
            //bindService(Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE)
            if(isConnected) {
                if(isRunning){
                    timerBinder.pause()
                    startButton.text = "Start"

                }else {
                    timerBinder.start(100)
                    startButton.text = "Pause"
                }

                isRunning = !isRunning

            }

        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
                isRunning = false
                startButton.text = "Start"
                timerTextView.text = "0"
            }

           //explicitly stop



        }
    }

    override fun onDestroy() {
        if(isConnected){
            unbindService(serviceConnection)
            isConnected = false
        }
        super.onDestroy()
    }
}
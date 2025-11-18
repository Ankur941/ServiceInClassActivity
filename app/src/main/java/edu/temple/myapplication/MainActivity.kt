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
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false
    private lateinit var timerTextView : TextView
    private var isRunning = false
    private var startPauseMenuItem: MenuItem? = null

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
            handleStartPause()

        }
        
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            handleStop()

           //explicitly stop



        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        startPauseMenuItem = menu?.findItem(R.id.action_start_pause)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_start_pause -> {
                handleStartPause()
                true
            }
            R.id.action_stop -> {
                handleStop()
                true

            }
            else -> super.onOptionsItemSelected(item)


        }
    }

    private fun handleStop(){
        if (isConnected) {
            timerBinder.stop()
            isRunning = false
            updateMenuState()
            timerTextView.text = "0"
        }
    }

    private fun handleStartPause()
    {
        if(isConnected){
            isRunning = if(isRunning){
                timerBinder.pause()
                false

            }else {
                timerBinder.start(100)
                true
            }
            updateMenuState()
        }
    }

    private fun updateMenuState(){
        val startButton = findViewById<Button>(R.id.startButton)
        if(isRunning){
            startButton.text = getString(R.string.pause)
            startPauseMenuItem?.title = getString(R.string.pause)
            startPauseMenuItem?.setIcon(android.R.drawable.ic_media_pause)

        } else {
            startButton.text = getString(R.string.start)
            startPauseMenuItem?.title = getString(R.string.start)
            startPauseMenuItem?.setIcon(android.R.drawable.ic_media_play)

        }
    }



    override fun onDestroy() {
        if(isRunning){
            unbindService(serviceConnection)
            isConnected = false
        }
        super.onDestroy()
    }
}
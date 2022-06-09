package com.example.cctv_app

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast

class FullscreenActivity : AppCompatActivity() {
    private var frameList: List<CctvLayout> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)

        val t = findViewById<LinearLayout>(R.id.test)


        frameList = (0..15).map { i ->
            val frame = CctvLayout(this)
            frame.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            frame
        }
        val cnt = intent.getIntExtra("cnt", 0)
        val save = intent.getIntegerArrayListExtra("saveList")

        if(save != null) {
            if (save.any { it == cnt }) {
                frameList[cnt].setup(1)
                frameList[cnt].setLabel("${cnt}")
                frameList[cnt].warning(true)
            }
        }
        t.addView(frameList[cnt])

        frameList[cnt].setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("save", save)
            startActivity(intent)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}
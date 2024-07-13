package com.example.qunltichnh.login

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.qunltichnh.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val btnLVH = findViewById<Button>(R.id.btnLVH)

        btnLVH.setOnClickListener{
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/phoenix.khai.528"))
            startActivity(i)
        }
    }
}
package com.example.qunltichnh.adapter

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.qunltichnh.R
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso

class ZoomImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom_image)    //activity hiển thị hình ảnh được phóng to

        val imageViewZoom = findViewById<PhotoView>(R.id.imageViewZoom)

        val imageUri = intent.getStringExtra("image_uri")

        if (imageUri != null) {
            Picasso.get().load(Uri.parse(imageUri)).into(imageViewZoom)
        }

    }
}
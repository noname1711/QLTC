package com.example.qunltichnh.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunltichnh.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class RvAnhChi(
    private val ds: MutableList<String>,
    private val context: Context,
    private val listener: OnImageLongClickListener
) : RecyclerView.Adapter<RvAnhChi.ChiViewHolder>() {

    inner class ChiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgListChi: ImageView = itemView.findViewById(R.id.imgListChi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_anh_chi, parent, false)
        return ChiViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChiViewHolder, position: Int) {
        val imageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").reference.child(ds[position])
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(holder.imgListChi)
            holder.imgListChi.setOnClickListener {
                val intent = Intent(context, ZoomImageActivity::class.java).apply {
                    putExtra("image_uri", uri.toString())
                }
                context.startActivity(intent)
            }
            holder.imgListChi.setOnLongClickListener {
                listener.onImageLongClick(ds[position], position)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return ds.size
    }
}
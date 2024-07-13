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

class RvAnhThu(
    private val ds: MutableList<String>,
    private val context: Context,
    private val listener: OnImageLongClickListener
) : RecyclerView.Adapter<RvAnhThu.ThuViewHolder>() {

    inner class ThuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgListThu: ImageView = itemView.findViewById(R.id.imgListThu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_anh_thu, parent, false)
        return ThuViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThuViewHolder, position: Int) {
        val imageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").reference.child(ds[position])
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(holder.imgListThu)
            holder.imgListThu.setOnClickListener {
                val intent = Intent(context, ZoomImageActivity::class.java).apply {
                    putExtra("image_uri", uri.toString())
                }
                context.startActivity(intent)
            }
            holder.imgListThu.setOnLongClickListener {
                listener.onImageLongClick(ds[position], position)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return ds.size
    }
}
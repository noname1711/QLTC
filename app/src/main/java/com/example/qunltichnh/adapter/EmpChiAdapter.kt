package com.example.qunltichnh.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunltichnh.R

//điều hướng cho danh sách data thu
class EmpChiAdapter(private val dsChi : ArrayList<employeeChi>): RecyclerView.Adapter<EmpChiAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener
    interface onItemClickListener {
        fun onItemClick (position: Int)
    }
    fun setOnItemClickListener (clickListener : onItemClickListener){
        mListener = clickListener
    }
    class ViewHolder(itemView : View,clickListener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        init{
            itemView.setOnClickListener{
                clickListener.onItemClick(adapterPosition)
            }
        }
    }
    //view holder để vẽ Ui cho item trên recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_fetching_nam_item_chi,parent,false)
        return ViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply{
            val tvEmpNameChi = findViewById<TextView>(R.id.tvEmpNameChi)
            tvEmpNameChi.text = dsChi[position].khoanChi   //lấy khoanChi từ employeeChi
        }
    }

    override fun getItemCount(): Int {
        return dsChi.size  //kích cỡ danh sách chi
    }
}
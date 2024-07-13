package com.example.qunltichnh.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qunltichnh.R

class EmpChiCompanyAdapter(private val dsChiCompany : ArrayList<employeeChi>): RecyclerView.Adapter<EmpChiCompanyAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener
    interface onItemClickListener {
        fun onItemClick (position: Int)
    }
    fun setOnItemClickListener (clickListener : onItemClickListener){
        mListener = clickListener
    }
    class ViewHolder(itemView : View, clickListener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        init{
            itemView.setOnClickListener{
                clickListener.onItemClick(adapterPosition)
            }
        }
    }
    //view holder để vẽ Ui cho item trên recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_fetching_company_item_chi,parent,false)
        return ViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.apply{
            val tvEmpNameChiNu = findViewById<TextView>(R.id.tvEmpNameChiCompany)
            tvEmpNameChiNu.text = dsChiCompany[position].khoanChi   //lấy khoanChi từ employeeChi
        }
    }

    override fun getItemCount(): Int {
        return dsChiCompany.size  //kích cỡ danh sách chi
    }
}
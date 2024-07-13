package com.example.qunltichnh.custom

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.qunltichnh.R
import com.example.qunltichnh.outdata.OutDataGv


class CustomGridView( val activity: Activity, val list: List<OutDataGv>):
    ArrayAdapter<OutDataGv>(activity, R.layout.layout_gridview_item){
    override fun getCount(): Int {
        return list.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val contexs = activity.layoutInflater
        val rowView = contexs.inflate(R.layout.layout_gridview_item,parent,false)
        val images = rowView.findViewById<ImageView>(R.id.imgThu)
        val txtThu = rowView.findViewById<TextView>(R.id.txtThu)
        images.setImageResource(list[position].images)
        txtThu.text = list[position].ten
        return rowView
    }
}
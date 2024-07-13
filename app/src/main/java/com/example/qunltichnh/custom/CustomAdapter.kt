package com.example.qunltichnh.custom

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.qunltichnh.R
import com.example.qunltichnh.outdata.OutData

//adpater cho list view ở màn chọn chế độ

class CustomAdapter(val activity: Activity, val list:List<OutData>): ArrayAdapter<OutData>(activity,
    R.layout.list_item
) {
    override fun getCount(): Int {
        return list.size  //vẽ lên view all dòng
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val contexs = activity.layoutInflater
        val rowView = contexs.inflate(R.layout.list_item,parent,false)
        val images = rowView.findViewById<ImageView>(R.id.images)
        val title = rowView.findViewById<TextView>(R.id.title)
        val desc = rowView.findViewById<TextView>(R.id.desc)
        title.text= list[position].title
        desc.text= list[position].desc
        images.setImageResource(list[position].image)
        return rowView   //ve thanh 1 dong
    }
}
package com.example.qunltichnh.adapter

//định nghĩa callback khi người dùng giữ vào 1 ảnh
// interface 1 lớp
interface OnImageLongClickListener {
    fun onImageLongClick(imagePath: String, position: Int)
}
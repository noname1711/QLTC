package com.example.qunltichnh.anh

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.OnImageLongClickListener
import com.example.qunltichnh.adapter.RvAnhChi
import com.example.qunltichnh.adapter.RvAnhThu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AnhNamActivity : AppCompatActivity(), OnImageLongClickListener {

    private lateinit var rvAnhNamThu: RecyclerView
    private lateinit var adapterThu: RvAnhThu
    private val listAnhThu = mutableListOf<String>()

    private lateinit var rvAnhNamChi: RecyclerView
    private lateinit var adapterChi: RvAnhChi
    private val listAnhChi = mutableListOf<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var txtAnhNamThu: TextView
    private lateinit var txtAnhNamChi: TextView
    private lateinit var txtLoadAnh: TextView

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anh_nam)

        //nút thoát
        val imageButtonCloseAnh = findViewById<ImageButton>(R.id.imageButtonCloseAnh)
        imageButtonCloseAnh.setOnClickListener{
            finish()
        }

        // ktao views
        rvAnhNamThu = findViewById(R.id.rvAnhNamThu)
        rvAnhNamThu.layoutManager = LinearLayoutManager(this)
        adapterThu = RvAnhThu(listAnhThu, this, this)
        rvAnhNamThu.adapter = adapterThu

        rvAnhNamChi = findViewById(R.id.rvAnhNamChi)
        rvAnhNamChi.layoutManager = LinearLayoutManager(this)
        adapterChi = RvAnhChi(listAnhChi, this, this)
        rvAnhNamChi.adapter = adapterChi

        txtAnhNamThu = findViewById(R.id.txtAnhNamThu)
        txtAnhNamChi = findViewById(R.id.txtAnhNamChi)
        txtLoadAnh = findViewById(R.id.txtLoadAnh)

        // Set loading visibility
        txtLoadAnh.visibility = View.VISIBLE
        rvAnhNamThu.visibility = View.GONE
        rvAnhNamChi.visibility = View.GONE
        txtAnhNamThu.visibility = View.GONE
        txtAnhNamChi.visibility = View.GONE

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").reference.child("images")

        loadAnhThu()
        loadAnhChi()

        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@AnhNamActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }
    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun loadAnhChi() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = (user.email ?: "").substringBefore("@")
            val imagesRef = storageRef.child("$userId/Nam/Chi/image/")
            // xóa ảnh load trc khi hiện ảnh mới
            listAnhChi.clear()
            adapterChi.notifyDataSetChanged()
            updateViewVisibility()
            imagesRef.listAll().addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    listAnhChi.add(item.path)
                }
                adapterChi.notifyDataSetChanged()    //update lại data
                updateViewVisibility()   //cài đặt hiển thị
            }.addOnFailureListener {
                Toast.makeText(this@AnhNamActivity,"Không thể hiển thị hình ảnh",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadAnhThu() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = (user.email ?: "").substringBefore("@")
            val imagesRef = storageRef.child("$userId/Nam/Thu/image/")
            // xóa ảnh load trc khi hiện ảnh mới
            listAnhThu.clear()
            adapterThu.notifyDataSetChanged()
            updateViewVisibility()
            imagesRef.listAll().addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    listAnhThu.add(item.path)
                }
                adapterThu.notifyDataSetChanged()    //update lại data
                updateViewVisibility()   //cài đặt hiển thị
            }.addOnFailureListener {
                Toast.makeText(this@AnhNamActivity,"Không thể hiển thị hình ảnh",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateViewVisibility() {
        if (listAnhThu.isEmpty() && listAnhChi.isEmpty()) {
            txtLoadAnh.text = "Không có hình ảnh được tải lên"
            txtLoadAnh.visibility = View.VISIBLE
            rvAnhNamThu.visibility = View.GONE
            rvAnhNamChi.visibility = View.GONE
            txtAnhNamThu.visibility = View.GONE
            txtAnhNamChi.visibility = View.GONE
        } else {
            txtLoadAnh.visibility = View.GONE
            if (listAnhThu.isNotEmpty()) {
                rvAnhNamThu.visibility = View.VISIBLE
                txtAnhNamThu.visibility = View.VISIBLE
            } else {
                rvAnhNamThu.visibility = View.GONE
                txtAnhNamThu.visibility = View.GONE
            }
            if (listAnhChi.isNotEmpty()) {
                rvAnhNamChi.visibility = View.VISIBLE
                txtAnhNamChi.visibility = View.VISIBLE
            } else {
                rvAnhNamChi.visibility = View.GONE
                txtAnhNamChi.visibility = View.GONE
            }
        }
    }

    override fun onImageLongClick(imagePath: String, position: Int) {    //nhấn lâu thì sẽ hiện activity xóa ảnh
        AlertDialog.Builder(this@AnhNamActivity)
            .setTitle("Xóa ảnh")
            .setMessage("Bạn có chắc chắn muốn xóa ảnh này không?")
            .setPositiveButton("Có") { dialog, _ ->
                // Xóa ảnh từ Firebase Storage
                val imageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").reference.child(imagePath)
                imageRef.delete().addOnSuccessListener {
                    // Xóa ảnh khỏi danh sách và thông báo
                    if (listAnhThu.contains(imagePath)) {
                        val index = listAnhThu.indexOf(imagePath)
                        listAnhThu.removeAt(index)
                        adapterThu.notifyItemRemoved(index)
                        adapterThu.notifyItemRangeChanged(index, listAnhThu.size)
                    } else if (listAnhChi.contains(imagePath)) {
                        val index = listAnhChi.indexOf(imagePath)
                        listAnhChi.removeAt(index)
                        adapterChi.notifyItemRemoved(index)
                        adapterChi.notifyItemRangeChanged(index, listAnhChi.size)
                    }
                    Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Không thể xóa ảnh", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Không") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}

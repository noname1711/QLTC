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

class AnhCompanyActivity : AppCompatActivity(), OnImageLongClickListener {

    private lateinit var rvAnhCompanyThu: RecyclerView
    private lateinit var adapterThu: RvAnhThu
    private val listAnhThu = mutableListOf<String>()

    private lateinit var rvAnhCompanyChi: RecyclerView
    private lateinit var adapterChi: RvAnhChi
    private val listAnhChi = mutableListOf<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private lateinit var txtAnhCompanyThu: TextView
    private lateinit var txtAnhCompanyChi: TextView
    private lateinit var txtLoadAnhCompany: TextView

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anh_company)

        //nút thoát
        val imageButtonCloseAnh = findViewById<ImageButton>(R.id.imageButtonCloseAnhCompany)
        imageButtonCloseAnh.setOnClickListener{
            finish()
        }

        // ktao views
        rvAnhCompanyThu = findViewById(R.id.rvAnhCompanyThu)
        rvAnhCompanyThu.layoutManager = LinearLayoutManager(this)
        adapterThu = RvAnhThu(listAnhThu, this, this)
        rvAnhCompanyThu.adapter = adapterThu

        rvAnhCompanyChi = findViewById(R.id.rvAnhCompanyChi)
        rvAnhCompanyChi.layoutManager = LinearLayoutManager(this)
        adapterChi = RvAnhChi(listAnhChi, this, this)
        rvAnhCompanyChi.adapter = adapterChi

        txtAnhCompanyThu = findViewById(R.id.txtAnhCompanyThu)
        txtAnhCompanyChi = findViewById(R.id.txtAnhCompanyChi)
        txtLoadAnhCompany = findViewById(R.id.txtLoadAnhCompany)

        // Set loading visibility
        txtLoadAnhCompany.visibility = View.VISIBLE
        rvAnhCompanyThu.visibility = View.GONE
        rvAnhCompanyChi.visibility = View.GONE
        txtAnhCompanyThu.visibility = View.GONE
        txtAnhCompanyChi.visibility = View.GONE

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").reference.child("images")

        loadAnhThu()
        loadAnhChi()

        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@AnhCompanyActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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
            val imagesRef = storageRef.child("$userId/Company/Chi/image/")
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
                Toast.makeText(this@AnhCompanyActivity,"Không thể hiển thị hình ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadAnhThu() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = (user.email ?: "").substringBefore("@")
            val imagesRef = storageRef.child("$userId/Company/Thu/image/")
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
                Toast.makeText(this@AnhCompanyActivity,"Không thể hiển thị hình ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateViewVisibility() {
        if (listAnhThu.isEmpty() && listAnhChi.isEmpty()) {
            txtLoadAnhCompany.text = "Không có hình ảnh được tải lên"
            txtLoadAnhCompany.visibility = View.VISIBLE
            rvAnhCompanyThu.visibility = View.GONE
            rvAnhCompanyChi.visibility = View.GONE
            txtAnhCompanyThu.visibility = View.GONE
            txtAnhCompanyChi.visibility = View.GONE
        } else {
            txtLoadAnhCompany.visibility = View.GONE
            if (listAnhThu.isNotEmpty()) {
                rvAnhCompanyThu.visibility = View.VISIBLE
                txtAnhCompanyThu.visibility = View.VISIBLE
            } else {
                rvAnhCompanyThu.visibility = View.GONE
                txtAnhCompanyThu.visibility = View.GONE
            }
            if (listAnhChi.isNotEmpty()) {
                rvAnhCompanyChi.visibility = View.VISIBLE
                txtAnhCompanyChi.visibility = View.VISIBLE
            } else {
                rvAnhCompanyChi.visibility = View.GONE
                txtAnhCompanyChi.visibility = View.GONE
            }
        }
    }

    override fun onImageLongClick(imagePath: String, position: Int) {    //nhấn lâu thì sẽ hiện activity xóa ảnh
        AlertDialog.Builder(this@AnhCompanyActivity)
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
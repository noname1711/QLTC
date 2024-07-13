package com.example.qunltichnh.update

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.employeeThu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class UpdateNamThuActivity : AppCompatActivity() {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_nam_thu)

        setValueToView()  //hàm set giá trị để hiển thị
        val btnDeleteThu = findViewById<Button>(R.id.btnDeleteThu)
        btnDeleteThu.setOnClickListener{
            deleteData(intent.getStringExtra("idThu").toString())   //hàm xóa data theo idThu
        }
        val btnUpdateThu = findViewById<Button>(R.id.btnUpdateThu)
        btnUpdateThu.setOnClickListener{
            openUpdateDialogThu(
                intent.getStringExtra("idThu").toString(),
                intent.getStringExtra("khoanThu").toString()
            )
        }
        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@UpdateNamThuActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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

    private fun openUpdateDialogThu(idThu: String, khoanThu: String) {
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater  //đổi layout xml thành view
        val mDialogView = inflater.inflate(R.layout.update_dialog_thu,null)
        mDialog.setView(mDialogView)   //truyền layout là update dialog thu và để dạng mặc định
        //update thông tin vào dialog
        val etEmpDateThu= mDialogView.findViewById<EditText>(R.id.etEmpDateThu)
        val etEmpTimeThu = mDialogView.findViewById<EditText>(R.id.etEmpTimeThu)
        val etEmpKhoanThu = mDialogView.findViewById<EditText>(R.id.etEmpKhoanThu)
        val etEmpLuongThu = mDialogView.findViewById<EditText>(R.id.etEmpLuongThu)
        val btnUpdateDataThu = mDialogView.findViewById<Button>(R.id.btnUpdateDataThu)

        etEmpDateThu.setText(intent.getStringExtra("dateThu").toString())
        etEmpTimeThu.setText(intent.getStringExtra("timeThu").toString())
        etEmpKhoanThu.setText(intent.getStringExtra("khoanThu").toString())
        etEmpLuongThu.setText(intent.getStringExtra("luongThu").toString())
        //tiêu đề dialog
        mDialog.setTitle("Cập nhật thông tin của $khoanThu")
        //khởi tạo và hiển thị dialog
        val alertDialog = mDialog.create()
        alertDialog.show()
        // click và btnUpdateDataThu
        btnUpdateDataThu.setOnClickListener {
            updateDataThu(idThu,etEmpDateThu.text.toString(),etEmpTimeThu.text.toString(),etEmpKhoanThu.text.toString(),etEmpLuongThu.text.toString())
            //chỉnh sửa xong thì update lại data lên màn hình
            val tvDateThu = findViewById<TextView>(R.id.tvDateThu)
            val tvTimeThu = findViewById<TextView>(R.id.tvTimeThu)
            val tvKhoanThu = findViewById<TextView>(R.id.tvKhoanThu)
            val tvLuongThu = findViewById<TextView>(R.id.tvLuongThu)
            tvDateThu.text = etEmpDateThu.text.toString()
            tvTimeThu.text = etEmpTimeThu.text.toString()
            tvKhoanThu.text = etEmpKhoanThu.text.toString()
            tvLuongThu.text = etEmpLuongThu.text.toString()
            alertDialog.dismiss()  //update xong thì đóng dialog lại
        }
    }

    private fun updateDataThu(idThu: String, dateThu: String, timeThu: String, khoanThu: String, luongThu: String) {
        val dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = (currentUser.email ?: "").substringBefore("@")
        val employeeDataThu = dbRef.child(userId).child("Nam").child("Thu")
        val empInfoThu = employeeThu(idThu,dateThu,timeThu,khoanThu,luongThu)
        ////chỉnh lại data thành data người dùng đã thêm
        employeeDataThu.child(idThu).setValue(empInfoThu).addOnSuccessListener{
            Toast.makeText(this@UpdateNamThuActivity,"Đã chỉnh sửa thông tin",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { err ->
            Toast.makeText(this@UpdateNamThuActivity,"Xóa lỗi ${err.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setValueToView() {
        val tvDateThu = findViewById<TextView>(R.id.tvDateThu)
        val tvTimeThu = findViewById<TextView>(R.id.tvTimeThu)
        val tvKhoanThu = findViewById<TextView>(R.id.tvKhoanThu)
        val tvLuongThu = findViewById<TextView>(R.id.tvLuongThu)

        tvDateThu.text = intent.getStringExtra("dateThu")
        tvTimeThu.text = intent.getStringExtra("timeThu")
        tvKhoanThu.text = intent.getStringExtra("khoanThu")
        tvLuongThu.text = intent.getStringExtra("luongThu")
    }

    private fun deleteData(idThu : String) {
        val dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = (currentUser.email ?: "").substringBefore("@")
        val employeeDataThu = dbRef.child(userId).child("Nam").child("Thu")
        employeeDataThu.child(idThu).removeValue().addOnSuccessListener {
            Toast.makeText(this@UpdateNamThuActivity,"Đã xóa khoản thu này",Toast.LENGTH_SHORT).show()
            finish()  //xóa thành công thì back về màn fetching
        }.addOnFailureListener { err ->
            Toast.makeText(this@UpdateNamThuActivity,"Xóa lỗi ${err.message}",Toast.LENGTH_SHORT).show()
        }
    }
}
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
import com.example.qunltichnh.adapter.employeeChi
import com.example.qunltichnh.adapter.employeeThu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UpdateNamChiActivity : AppCompatActivity() {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_nam_chi)

        setValueToView()  //hàm set giá trị để hiển thị
        val btnDeleteChi = findViewById<Button>(R.id.btnDeleteChi)
        btnDeleteChi.setOnClickListener{
            deleteData(intent.getStringExtra("idChi").toString())   //hàm xóa data theo idChi
        }
        val btnUpdateChi = findViewById<Button>(R.id.btnUpdateChi)
        btnUpdateChi.setOnClickListener{
            openUpdateDialogChi(
                intent.getStringExtra("idChi").toString(),
                intent.getStringExtra("khoanChi").toString()
            )
        }
        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@UpdateNamChiActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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


    private fun openUpdateDialogChi(idChi: String, khoanChi: String) {
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater  //đổi layout xml thành view
        val mDialogView = inflater.inflate(R.layout.update_dialog_chi,null)
        mDialog.setView(mDialogView)   //truyền layout là update dialog thu và để dạng mặc định
        //update thông tin vào dialog
        val etEmpDateChi= mDialogView.findViewById<EditText>(R.id.etEmpDateChi)
        val etEmpTimeChi = mDialogView.findViewById<EditText>(R.id.etEmpTimeChi)
        val etEmpKhoanChi = mDialogView.findViewById<EditText>(R.id.etEmpKhoanChi)
        val etEmpLuongChi = mDialogView.findViewById<EditText>(R.id.etEmpLuongChi)
        val btnUpdateDataChi = mDialogView.findViewById<Button>(R.id.btnUpdateDataChi)

        etEmpDateChi.setText(intent.getStringExtra("dateChi").toString())
        etEmpTimeChi.setText(intent.getStringExtra("timeChi").toString())
        etEmpKhoanChi.setText(intent.getStringExtra("khoanChi").toString())
        etEmpLuongChi.setText(intent.getStringExtra("luongChi").toString())
        //tiêu đề dialog
        mDialog.setTitle("Cập nhật thông tin của $khoanChi")
        //khởi tạo và hiển thị dialog
        val alertDialog = mDialog.create()
        alertDialog.show()
        // click và btnUpdateDataThu
        btnUpdateDataChi.setOnClickListener {
            updateDataChi(idChi,etEmpDateChi.text.toString(),etEmpTimeChi.text.toString(),etEmpKhoanChi.text.toString(),etEmpLuongChi.text.toString())
            /*Toast.makeText(this@UpdateNamThuActivity,"Đã chỉnh sửa thông tin",Toast.LENGTH_SHORT).show()*/
            //chỉnh sửa xong thì update lại data lên màn hình
            val tvDateChi = findViewById<TextView>(R.id.tvDateChi)
            val tvTimeChi = findViewById<TextView>(R.id.tvTimeChi)
            val tvKhoanChi = findViewById<TextView>(R.id.tvKhoanChi)
            val tvLuongChi = findViewById<TextView>(R.id.tvLuongChi)
            tvDateChi.text = etEmpDateChi.text.toString()
            tvTimeChi.text = etEmpTimeChi.text.toString()
            tvKhoanChi.text = etEmpKhoanChi.text.toString()
            tvLuongChi.text = etEmpLuongChi.text.toString()
            alertDialog.dismiss()  //update xong thì đóng dialog lại
        }
    }

    private fun updateDataChi(idChi: String, dateChi: String, timeChi: String, khoanChi: String, luongChi: String) {
        val dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = (currentUser.email ?: "").substringBefore("@")
        val employeeDataChi = dbRef.child(userId).child("Nam").child("Chi")
        val empInfoChi = employeeChi(idChi,dateChi,timeChi,khoanChi,luongChi)
        ////chỉnh lại data thành data người dùng đã thêm
        employeeDataChi.child(idChi).setValue(empInfoChi).addOnSuccessListener{
            Toast.makeText(this@UpdateNamChiActivity,"Đã chỉnh sửa thông tin",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { err ->
            Toast.makeText(this@UpdateNamChiActivity,"Xóa lỗi ${err.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteData(idChi: String) {
        val dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = (currentUser.email ?: "").substringBefore("@")
        val employeeDataChi = dbRef.child(userId).child("Nam").child("Chi")
        employeeDataChi.child(idChi).removeValue().addOnSuccessListener {
            Toast.makeText(this@UpdateNamChiActivity,"Đã xóa khoản chi này", Toast.LENGTH_SHORT).show()
            finish()  //xóa thành công thì back về màn fetching
        }.addOnFailureListener { err ->
            Toast.makeText(this@UpdateNamChiActivity,"Xóa lỗi ${err.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setValueToView() {
        val tvDateChi = findViewById<TextView>(R.id.tvDateChi)
        val tvTimeChi = findViewById<TextView>(R.id.tvTimeChi)
        val tvKhoanChi = findViewById<TextView>(R.id.tvKhoanChi)
        val tvLuongChi = findViewById<TextView>(R.id.tvLuongChi)

        tvDateChi.text = intent.getStringExtra("dateChi")
        tvTimeChi.text = intent.getStringExtra("timeChi")
        tvKhoanChi.text = intent.getStringExtra("khoanChi")
        tvLuongChi.text = intent.getStringExtra("luongChi")
    }
}
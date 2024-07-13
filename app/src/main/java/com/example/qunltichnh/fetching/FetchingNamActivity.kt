package com.example.qunltichnh.fetching

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.EmpChiAdapter
import com.example.qunltichnh.adapter.EmpThuAdapter
import com.example.qunltichnh.adapter.employeeChi
import com.example.qunltichnh.adapter.employeeThu
import com.example.qunltichnh.update.UpdateNamChiActivity
import com.example.qunltichnh.update.UpdateNamThuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class FetchingNamActivity : AppCompatActivity() {

    private lateinit var dsThu: ArrayList<employeeThu>
    private lateinit var dsChi: ArrayList<employeeChi>
    private lateinit var dbRef : DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var employeeDataRefThu: DatabaseReference
    private lateinit var employeeDataRefChi: DatabaseReference

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetching_nam)
        //nút thoát
        val imageButtonClose = findViewById<ImageButton>(R.id.imageButtonClose)
        imageButtonClose.setOnClickListener{
            finish()
        }
        // Khởi tạo RecyclerView
        val rvEmpThu = findViewById<RecyclerView>(R.id.rvEmpThu)
        val rvEmpChi = findViewById<RecyclerView>(R.id.rvEmpChi)
        //điều hướng 2 rv theo mặc định(chiều dọc)
        rvEmpThu.layoutManager = LinearLayoutManager(this)
        rvEmpChi.layoutManager = LinearLayoutManager(this)
        //tối ưu hiệu năng khi trượt rv
        rvEmpThu.setHasFixedSize(true)
        rvEmpChi.setHasFixedSize(true)

        dsThu = ArrayList()
        dsChi = ArrayList()

        // Lấy thông tin từ Firebase
        fetchData()

        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@FetchingNamActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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


    private fun fetchData() {
        //text view hiện tên rv
        val textViewKhoanThu = findViewById<TextView>(R.id.textViewKhoanThu)
        val textViewKhoanChi = findViewById<TextView>(R.id.textViewKhoanChi)
        // Hiển thị chữ loading khi data chưa hiển thị
        val txtLoadingData = findViewById<TextView>(R.id.txtLoadingData)
        txtLoadingData.visibility = View.VISIBLE
        val rvEmpThu = findViewById<RecyclerView>(R.id.rvEmpThu)
        val rvEmpChi = findViewById<RecyclerView>(R.id.rvEmpChi)
        // Khởi tạo Firebase Database reference
        dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
        // Lấy user hiện tại từ Firebase Auth
        currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = (currentUser.email ?: "").substringBefore("@")
        // Tham chiếu tới dữ liệu Thu và Chi của user
        employeeDataRefThu = dbRef.child(userId).child("Nam").child("Thu")
        employeeDataRefChi = dbRef.child(userId).child("Nam").child("Chi")

        // Lắng nghe sự thay đổi dữ liệu Thu
        employeeDataRefThu.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dsThu.clear()
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        ////lấy dữ liệu lần lượt các mục đầu tiên của mục con trong thu và truy xuất data( trong đó có cả khoanthu)
                        val empDataThu = empSnap.getValue(employeeThu::class.java)
                        dsThu.add(empDataThu!!)
                    }
                    val mAdapterThu = EmpThuAdapter(dsThu)
                    rvEmpThu.adapter = mAdapterThu
                    //nghe sự kiện click lên rv và truyền data
                    mAdapterThu.setOnItemClickListener(object : EmpThuAdapter.onItemClickListener{
                        override fun onItemClick(position: Int){
                            val i = Intent(this@FetchingNamActivity, UpdateNamThuActivity::class.java)
                            i.putExtra("idThu",dsThu[position].idThu)
                            i.putExtra("dateThu",dsThu[position].dateThu)
                            i.putExtra("timeThu",dsThu[position].timeThu)
                            i.putExtra("luongThu",dsThu[position].luongThu)
                            i.putExtra("khoanThu",dsThu[position].khoanThu)
                            startActivity(i)
                        }
                    })
                    if (dsThu.isEmpty()) {
                        textViewKhoanThu.visibility = View.GONE
                        rvEmpThu.visibility = View.GONE
                    } else {
                        textViewKhoanThu.visibility = View.VISIBLE
                        rvEmpThu.visibility = View.VISIBLE
                    }
                } else {
                    textViewKhoanThu.visibility = View.GONE
                    rvEmpThu.visibility = View.GONE
                }
                updateLoadingMessage()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FetchingNamActivity, "Không thể truy vấn được dữ liệu khoản thu", Toast.LENGTH_SHORT).show()
            }
        })

        // Lắng nghe sự thay đổi dữ liệu Chi
        employeeDataRefChi.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dsChi.clear()
                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        ////lấy dữ liệu lần lượt các mục đầu tiên của mục con trong thu và truy xuất data( trong đó có cả khoanchi)
                        val empDataChi = empSnap/*.children.first()*/.getValue(employeeChi::class.java)
                        dsChi.add(empDataChi!!)
                    }
                    val mAdapterChi = EmpChiAdapter(dsChi)
                    rvEmpChi.adapter = mAdapterChi
                    //nghe sự kiện click lên rv và truyền data
                    mAdapterChi.setOnItemClickListener(object : EmpChiAdapter.onItemClickListener{
                        override fun onItemClick(position: Int){
                            val i = Intent(this@FetchingNamActivity, UpdateNamChiActivity::class.java)
                            i.putExtra("idChi",dsChi[position].idChi)
                            i.putExtra("dateChi",dsChi[position].dateChi)
                            i.putExtra("timeChi",dsChi[position].timeChi)
                            i.putExtra("luongChi",dsChi[position].luongChi)
                            i.putExtra("khoanChi",dsChi[position].khoanChi)
                            startActivity(i)
                        }
                    })
                    if (dsChi.isEmpty()) {
                        textViewKhoanChi.visibility = View.GONE
                        rvEmpChi.visibility = View.GONE
                    } else {
                        textViewKhoanChi.visibility = View.VISIBLE
                        rvEmpChi.visibility = View.VISIBLE
                    }
                } else {
                    textViewKhoanChi.visibility = View.GONE
                    rvEmpChi.visibility = View.GONE
                }
                updateLoadingMessage()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FetchingNamActivity, "Không thể truy vấn được dữ liệu khoản chi", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // hàm trong trường hợp cả 2 rv đều ko có data
    private fun updateLoadingMessage() {
        val txtLoadingData = findViewById<TextView>(R.id.txtLoadingData)
        if (dsThu.isEmpty() && dsChi.isEmpty()) {
            txtLoadingData.text = "Không có dữ liệu nào được nhập"
            txtLoadingData.visibility = View.VISIBLE
        } else {
            txtLoadingData.visibility = View.GONE
        }
    }
}

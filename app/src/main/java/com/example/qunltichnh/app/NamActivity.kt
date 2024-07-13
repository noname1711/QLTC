package com.example.qunltichnh.app


import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.custom.CustomGridView
import com.example.qunltichnh.thu.InsertionActivity
import com.example.qunltichnh.outdata.OutDataGv
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.employeeChi
import com.example.qunltichnh.adapter.employeeThu
import com.example.qunltichnh.anh.AnhNamActivity
import com.example.qunltichnh.chi.InsertionChiActivity
import com.example.qunltichnh.dothi.DoThiNamActivity
import com.example.qunltichnh.fetching.FetchingNamActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class NamActivity : AppCompatActivity() {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nam)
        //về lại màn app
        val imgBtnBackApp = findViewById<ImageButton>(R.id.imgBtnBackApp)
        imgBtnBackApp.setOnClickListener{
            val i = Intent(this@NamActivity, AppActivity::class.java)
            startActivity(i)
        }
        // hiển thị màn cho data người dùng đã nhập
        val btnDataNam = findViewById<ImageButton>(R.id.btnDataNam)
        btnDataNam.setOnClickListener{
            val i = Intent(this@NamActivity, FetchingNamActivity::class.java)
            startActivity(i)
        }
        //hiển thị hình ảnh bill thu chi
        val btnAnhNam = findViewById<ImageButton>(R.id.btnAnhNam)
        btnAnhNam.setOnClickListener{
            val i = Intent (this@NamActivity, AnhNamActivity::class.java)
            startActivity(i)
        }
        //hiển thị dữ liệu dưới dạng đồ thị
        val btnDoThiNam = findViewById<ImageButton>(R.id.btnDoThiNam)
        btnDoThiNam.setOnClickListener{
            val i = Intent(this@NamActivity, DoThiNamActivity::class.java)
            startActivity(i)
        }
        //về lại đầu trang
        val scrollViewNam = findViewById<ScrollView>(R.id.scrollViewNam)
        val btnFirstNam = findViewById<FloatingActionButton>(R.id.btnFirstNam)
        btnFirstNam.setOnClickListener {
            scrollViewNam.smoothScrollTo(0, 0)
        }
        //grid view cho thu
        val list = mutableListOf<OutDataGv>()
        list.add(OutDataGv(R.drawable.nam_di_lam, "  Làm việc"))
        list.add(OutDataGv(R.drawable.nam_lam_them, " Làm thêm"))
        list.add(OutDataGv(R.drawable.lai_suat, "   Lãi suất"))
        list.add(OutDataGv(R.drawable.nam_giai_thuong, "Thưởng thêm"))
        list.add(OutDataGv(R.drawable.khac, "     Khác"))
        val customThu = CustomGridView(this, list)
        val gvThu = findViewById<GridView>(R.id.gvThu)
        gvThu.adapter = customThu
        gvThu.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, InsertionActivity::class.java)
            startActivity(intent)
        }


        //grid view cho chi
        val list1 = mutableListOf<OutDataGv>()
        list1.add(OutDataGv(R.drawable.giao_duc, "  Giáo dục"))
        list1.add(OutDataGv(R.drawable.nam_an, "   Ăn uống"))
        list1.add(OutDataGv(R.drawable.nam_the_thao, "   Thể thao"))
        list1.add(OutDataGv(R.drawable.suc_khoe, "  Sức khỏe"))
        list1.add(OutDataGv(R.drawable.di_lai, "     Đi lại"))
        list1.add(OutDataGv(R.drawable.noi_o, "     Chỗ ở"))
        list1.add(OutDataGv(R.drawable.dau_tu, "    Đầu tư"))
        list1.add(OutDataGv(R.drawable.bank, " Ngân hàng"))
        list1.add(OutDataGv(R.drawable.chung_khoan, "Chứng khoán"))
        list1.add(OutDataGv(R.drawable.hen_ho, "    Hẹn hò"))
        list1.add(OutDataGv(R.drawable.healing, "    Healing"))
        list1.add(OutDataGv(R.drawable.tu_thien, "   Từ thiện"))
        list1.add(OutDataGv(R.drawable.vay, "   Vay nợ"))
        list1.add(OutDataGv(R.drawable.khac, "      Khác"))
        val customChi = CustomGridView(this, list1)
        val gvChi = findViewById<GridView>(R.id.gvChi)
        gvChi.adapter = customChi
        gvChi.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, InsertionChiActivity::class.java)
            startActivity(intent)
        }

        val btnCheckTk = findViewById<Button>(R.id.btnCheckTk)
        btnCheckTk.setOnClickListener {

            //xử lí hiện tiết kiệm
            val databaseRef =
                FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users")

            val currentUser = FirebaseAuth.getInstance().currentUser

            currentUser?.let { user ->
                val userId = (user.email ?: "").substringBefore("@")

                val employeeDataRefThu = databaseRef.child(userId).child("Nam").child("Thu")
                val employeeDataRefChi = databaseRef.child(userId).child("Nam").child("Chi")

                // Khai báo biến tổng LuongThu và LuongChi
                var totalLuongThu = 0
                var totalLuongChi = 0

                // Biến đếm để kiểm tra cả hai listener đã hoàn thành
                var listenerCount = 0

                // Truy vấn dữ liệu empLuongThu
                employeeDataRefThu.addListenerForSingleValueEvent(object : ValueEventListener {  //lắng nghe sk 1 lần duy nhất
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        totalLuongThu = 0    //reset tổng thu trc khi chạy vòng lăp
                        for (snapshot in dataSnapshot.children) {    //chạy từng data 1
                            //lấy dữ liệu lần lượt các mục đầu tiên của mục con trong thu và truy xuất data( trong đó có cả luongthu)
                            val employee = snapshot.getValue(employeeThu::class.java)
                            val LuongThu = employee?.luongThu?.toIntOrNull() ?: 0   //chuyển sang int, ko thành công thì = 0
                            totalLuongThu += LuongThu
                        }
                        // Tăng biến đếm listener và kiểm tra cả hai listener đã hoàn thành
                        listenerCount++
                        if (listenerCount == 2) {
                            calculateAndShowDialog(totalLuongThu, totalLuongChi)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Xử lý khi có lỗi xảy ra ở nhập lượng thu
                        Toast.makeText(
                            this@NamActivity,
                            "Lỗi lượng thu: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

                // Truy vấn dữ liệu empLuongChi
                employeeDataRefChi.addListenerForSingleValueEvent(object :ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        totalLuongChi = 0     //reset tổng chi trc khi chạy vòng lặp
                        for (snapshot in dataSnapshot.children) {     //chạy từng data 1
                            //lấy dữ liệu lần lượt các mục đầu tiên của mục con trong thu và truy xuất data( trong đó có cả luongchi)
                            val employee = snapshot/*.children.first()*/.getValue(employeeChi::class.java)
                            val empLuongChi = employee?.luongChi?.toIntOrNull() ?: 0   //chuyển sang int, ko thành công thì = 0
                            totalLuongChi += empLuongChi
                        }
                        // Tăng biến đếm listener và kiểm tra cả hai listener đã hoàn thành
                        listenerCount++
                        if (listenerCount == 2) {
                            calculateAndShowDialog(totalLuongThu, totalLuongChi)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Xử lý khi có lỗi xảy ra ở nhập lượng chi
                        Toast.makeText(
                            this@NamActivity,
                            "Lỗi lượng chi: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }

        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@NamActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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

    private fun calculateAndShowDialog(totalLuongThu: Int, totalLuongChi: Int) {
        val hieu = totalLuongThu - totalLuongChi
        val txtTietKiem = findViewById<TextView>(R.id.txtTietKiem)
        txtTietKiem.text = "$hieu" // Set giá trị hiệu vào TextView txtTietKiem
        //hiển thị các cảnh báo chi tiêu
        if (hieu <= 0) {
            showDialogLo() // Hiển thị dialog lỗ khi hiệu <= 0
        } else {
            showDialogLai() // Hiển thị dialog lãi khi hiệu > 0
        }
    }

    private fun showDialogLai() {
        val builder = AlertDialog.Builder(this@NamActivity,R.style.Themecustom)
        val view = layoutInflater.inflate(R.layout.dialog_lai, null) //chuyển đổi layout đã tke thành view
        builder.setView(view)
        val dialog = builder.create() // Tạo dialog từ AlertDialog.Builder
        dialog.show() // Hiển thị dialog
        val imgOutDialogLai = view.findViewById<ImageButton>(R.id.imgOutDialogLai)  //view để hiểu là nút hiện trên layout khác
        imgOutDialogLai.setOnClickListener{
            dialog.dismiss()
        }
    }

    private fun showDialogLo() {
        val builder = AlertDialog.Builder(this@NamActivity,R.style.Themecustom)
        val view = layoutInflater.inflate(R.layout.dialog_lo, null) //chuyển đổi layout đã tke thành view
        builder.setView(view)
        val dialog = builder.create() // Tạo dialog từ AlertDialog.Builder
        dialog.show() // Hiển thị dialog
        val imgOutDialogLo = view.findViewById<ImageButton>(R.id.imgOutDialogLo)
        imgOutDialogLo.setOnClickListener {
            dialog.dismiss()
        }
    }
}



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
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.employeeChi
import com.example.qunltichnh.adapter.employeeThu
import com.example.qunltichnh.anh.AnhCompanyActivity
import com.example.qunltichnh.chi.InsertionChiCompanyActivity
import com.example.qunltichnh.custom.CustomGridView
import com.example.qunltichnh.dothi.DoThiCompanyActivity
import com.example.qunltichnh.fetching.FetchingCompanyActivity
import com.example.qunltichnh.outdata.OutDataGv
import com.example.qunltichnh.thu.InsertionThuCompanyActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CompanyActivity : AppCompatActivity() {
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company)

        //về lại màn app
        val imgBtnBackAppCompany = findViewById<ImageButton>(R.id.imgBtnBackAppCompany)
        imgBtnBackAppCompany.setOnClickListener{
            val i = Intent(this@CompanyActivity, AppActivity::class.java)
            startActivity(i)
        }
        // hiển thị màn cho data người dùng đã nhập
        val btnDataCompany = findViewById<ImageButton>(R.id.btnDataCompany)
        btnDataCompany.setOnClickListener{
            val i = Intent(this@CompanyActivity, FetchingCompanyActivity::class.java)
            startActivity(i)
        }
        //hiển thị hình ảnh bill thu chi
        val btnAnhCompany = findViewById<ImageButton>(R.id.btnAnhCompany)
        btnAnhCompany.setOnClickListener{
            val i = Intent (this@CompanyActivity, AnhCompanyActivity::class.java)
            startActivity(i)
        }
        //hiển thị dữ liệu dưới dạng đồ thị
        val btnDoThiCompany = findViewById<ImageButton>(R.id.btnDoThiCompany)
        btnDoThiCompany.setOnClickListener{
            val i = Intent(this@CompanyActivity, DoThiCompanyActivity::class.java)
            startActivity(i)
        }
        //về lại đầu trang
        val scrollViewCompany = findViewById<ScrollView>(R.id.scrollViewCompany)
        val btnFirstCompany = findViewById<FloatingActionButton>(R.id.btnFirstCompany)
        btnFirstCompany.setOnClickListener {
            scrollViewCompany.smoothScrollTo(0, 0)
        }
        //grid view cho thu
        val list = mutableListOf<OutDataGv>()
        list.add(OutDataGv(R.drawable.ban_hang, "  Bán hàng"))
        list.add(OutDataGv(R.drawable.dich_vu, "   Dịch vụ"))
        list.add(OutDataGv(R.drawable.ban_quyen, "  Bản quyền"))
        list.add(OutDataGv(R.drawable.lai_suat, "  Lãi suất"))
        list.add(OutDataGv(R.drawable.hop_tac, "  Hợp tác"))
        list.add(OutDataGv(R.drawable.tai_chinh, "  Tài chính"))
        list.add(OutDataGv(R.drawable.tro_cap, "  Trợ cấp"))
        list.add(OutDataGv(R.drawable.du_an, "   Dự án"))
        list.add(OutDataGv(R.drawable.khac, "     Khác"))
        val customThu = CustomGridView(this, list)
        val gvThu = findViewById<GridView>(R.id.gvThuCompany)
        gvThu.adapter = customThu
        gvThu.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, InsertionThuCompanyActivity::class.java)
            startActivity(intent)
        }


        //grid view cho chi
        val list1 = mutableListOf<OutDataGv>()
        list1.add(OutDataGv(R.drawable.san_xuat, "   Sản xuất"))
        list1.add(OutDataGv(R.drawable.mua_hang, "  Mua hàng"))
        list1.add(OutDataGv(R.drawable.nhan_su, "    Nhân sự"))
        list1.add(OutDataGv(R.drawable.hanh_chinh, " Hành chính"))
        list1.add(OutDataGv(R.drawable.co_so, "    Cơ sở"))
        list1.add(OutDataGv(R.drawable.van_hanh, "   Vận hành"))
        list1.add(OutDataGv(R.drawable.quang_cao, " Quảng cáo"))
        list1.add(OutDataGv(R.drawable.bank, "  Khoản vay"))
        list1.add(OutDataGv(R.drawable.phap_ly, "  Pháp lý"))
        list1.add(OutDataGv(R.drawable.phat_trien, " Phát triển"))
        list1.add(OutDataGv(R.drawable.dao_tao, "   Đào tạo"))
        list1.add(OutDataGv(R.drawable.dau_tu_to, "   Đầu tư"))
        list1.add(OutDataGv(R.drawable.tu_thien, "   Từ thiện"))
        list1.add(OutDataGv(R.drawable.khac, "      Khác"))
        val customChi = CustomGridView(this, list1)
        val gvChi = findViewById<GridView>(R.id.gvChiCompany)
        gvChi.adapter = customChi
        gvChi.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, InsertionChiCompanyActivity::class.java)
            startActivity(intent)
        }

        val btnCheckTkCompany = findViewById<Button>(R.id.btnCheckTkCompany)
        btnCheckTkCompany.setOnClickListener {

            //xử lí hiện tiết kiệm
            val databaseRef =
                FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users")

            val currentUser = FirebaseAuth.getInstance().currentUser

            currentUser?.let { user ->
                val userId = (user.email ?: "").substringBefore("@")

                val employeeDataRefThu = databaseRef.child(userId).child("Company").child("Thu")
                val employeeDataRefChi = databaseRef.child(userId).child("Company").child("Chi")

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
                            this@CompanyActivity,
                            "Lỗi lượng thu: ${databaseError.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

                // Truy vấn dữ liệu empLuongChi
                employeeDataRefChi.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        totalLuongChi = 0     //reset tổng chi trc khi chạy vòng lặp
                        for (snapshot in dataSnapshot.children) {     //chạy từng data 1
                            //lấy dữ liệu lần lượt các mục đầu tiên của mục con trong thu và truy xuất data( trong đó có cả luongchi)
                            val employee = snapshot.getValue(employeeChi::class.java)
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
                            this@CompanyActivity,
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
            Toast.makeText(this@CompanyActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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
        val txtTietKiem = findViewById<TextView>(R.id.txtTietKiemCompany)
        txtTietKiem.text = "$hieu" // Set giá trị hiệu vào TextView txtTietKiem
        //hiển thị các cảnh báo chi tiêu
        if (hieu <= 0) {
            showDialogLo() // Hiển thị dialog lỗ khi hiệu <= 0
        } else {
            showDialogLai() // Hiển thị dialog lãi khi hiệu > 0
        }
    }

    private fun showDialogLai() {
        val builder = AlertDialog.Builder(this@CompanyActivity,R.style.Themecustom)
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
        val builder = AlertDialog.Builder(this@CompanyActivity,R.style.Themecustom)
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
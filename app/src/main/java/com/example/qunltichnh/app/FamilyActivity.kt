package com.example.qunltichnh.app

import android.content.Context
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
import com.example.qunltichnh.anh.AnhFamilyActivity
import com.example.qunltichnh.chat.PickChatFamilyActivity
import com.example.qunltichnh.chi.InsertionChiFamilyActivity
import com.example.qunltichnh.custom.CustomGridView
import com.example.qunltichnh.dothi.DoThiFamilyActivity
import com.example.qunltichnh.fetching.FetchingFamilyActivity
import com.example.qunltichnh.outdata.OutDataGv
import com.example.qunltichnh.thu.InsertionThuFamilyActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FamilyActivity : AppCompatActivity() {

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)


        //về lại màn app
        val imgBtnBackAppFamily = findViewById<ImageButton>(R.id.imgBtnBackAppFamily)
        imgBtnBackAppFamily.setOnClickListener{
            val i = Intent(this@FamilyActivity, AppActivity::class.java)
            startActivity(i)
        }
        // hiển thị màn cho data người dùng đã nhập
        val btnDataFamily = findViewById<ImageButton>(R.id.btnDataFamily)
        btnDataFamily.setOnClickListener{
            val i = Intent(this@FamilyActivity, FetchingFamilyActivity::class.java)
            startActivity(i)
        }
        //hiển thị hình ảnh bill thu chi
        val btnAnhFamily = findViewById<ImageButton>(R.id.btnAnhFamily)
        btnAnhFamily.setOnClickListener{
            val i = Intent (this@FamilyActivity, AnhFamilyActivity::class.java)
            startActivity(i)
        }
        //hiển thị dữ liệu dưới dạng đồ thị
        val btnDoThiFamily = findViewById<ImageButton>(R.id.btnDoThiFamily)
        btnDoThiFamily.setOnClickListener{
            val i = Intent(this@FamilyActivity, DoThiFamilyActivity::class.java)
            startActivity(i)
        }
        //về lại đầu trang
        val scrollViewFamily = findViewById<ScrollView>(R.id.scrollViewFamily)
        val btnFirstFamily = findViewById<FloatingActionButton>(R.id.btnFirstFamily)
        btnFirstFamily.setOnClickListener {
            scrollViewFamily.smoothScrollTo(0, 0)
        }
        //chế độ nhắn tin gia đình
        val btnChatFamily = findViewById<FloatingActionButton>(R.id.btnChatFamily)
        btnChatFamily.setOnClickListener{
            val i = Intent(this@FamilyActivity, PickChatFamilyActivity::class.java)
            startActivity(i)
        }

        //grid view cho thu
        val list = mutableListOf<OutDataGv>()
        list.add(OutDataGv(R.drawable.nam_lam_them, "  Lương bố"))
        list.add(OutDataGv(R.drawable.nu_lam_them, "  Lương mẹ"))
        list.add(OutDataGv(R.drawable.lai_suat, "   Lãi suất"))
        list.add(OutDataGv(R.drawable.nu_giai_thuong, "Thưởng thêm"))
        list.add(OutDataGv(R.drawable.khac, "     Khác"))
        val customThu = CustomGridView(this, list)
        val gvThu = findViewById<GridView>(R.id.gvThuFamily)
        gvThu.adapter = customThu
        gvThu.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, InsertionThuFamilyActivity::class.java)
            startActivity(intent)
        }


        //grid view cho chi
        val list1 = mutableListOf<OutDataGv>()
        list1.add(OutDataGv(R.drawable.giao_duc, "  Giáo dục"))
        list1.add(OutDataGv(R.drawable.family_an, "   Ăn uống"))
        list1.add(OutDataGv(R.drawable.suc_khoe, "  Sức khỏe"))
        list1.add(OutDataGv(R.drawable.di_lai, "     Đi lại"))
        list1.add(OutDataGv(R.drawable.noi_o, "     Chỗ ở"))
        list1.add(OutDataGv(R.drawable.dau_tu, "    Đầu tư"))
        list1.add(OutDataGv(R.drawable.bank, " Ngân hàng"))
        list1.add(OutDataGv(R.drawable.chung_khoan, "Chứng khoán"))
        list1.add(OutDataGv(R.drawable.ho_hang, "   Họ hàng"))
        list1.add(OutDataGv(R.drawable.le_hoi, "   Ngày lễ"))
        list1.add(OutDataGv(R.drawable.hen_ho, "    Hẹn hò"))
        list1.add(OutDataGv(R.drawable.family_du_lich, "    Du lịch"))
        list1.add(OutDataGv(R.drawable.tu_thien, "   Từ thiện"))
        list1.add(OutDataGv(R.drawable.vay, "    Vay nợ"))
        list1.add(OutDataGv(R.drawable.khac, "      Khác"))
        val customChi = CustomGridView(this, list1)
        val gvChi = findViewById<GridView>(R.id.gvChiFamily)
        gvChi.adapter = customChi
        gvChi.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, InsertionChiFamilyActivity::class.java)
            startActivity(intent)
        }

        val btnCheckTkFamily = findViewById<Button>(R.id.btnCheckTkFamily)
        btnCheckTkFamily.setOnClickListener {

            //xử lí hiện tiết kiệm
            val databaseRef =
                FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users")

            val currentUser = FirebaseAuth.getInstance().currentUser

            currentUser?.let { user ->
                val userId = (user.email ?: "").substringBefore("@")

                val employeeDataRefThu = databaseRef.child(userId).child("Family").child("Thu")
                val employeeDataRefChi = databaseRef.child(userId).child("Family").child("Chi")

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
                            this@FamilyActivity,
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
                            this@FamilyActivity,
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
            Toast.makeText(this@FamilyActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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
        val txtTietKiem = findViewById<TextView>(R.id.txtTietKiemFamily)
        txtTietKiem.text = "$hieu" // Set giá trị hiệu vào TextView txtTietKiem
        //hiển thị các cảnh báo chi tiêu
        if (hieu <= 0) {
            showDialogLo() // Hiển thị dialog lỗ khi hiệu <= 0
        } else {
            showDialogLai() // Hiển thị dialog lãi khi hiệu > 0
        }
    }

    private fun showDialogLai() {
        val builder = AlertDialog.Builder(this@FamilyActivity,R.style.Themecustom)
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
        val builder = AlertDialog.Builder(this@FamilyActivity,R.style.Themecustom)
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
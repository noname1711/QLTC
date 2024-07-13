package com.example.qunltichnh.dothi

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.employeeChi
import com.example.qunltichnh.adapter.employeeThu
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DoThiNamActivity : AppCompatActivity() {

    private lateinit var combinedChart: CombinedChart
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var edtChonNam: EditText
    private lateinit var btnTruyVan: Button

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_do_thi_nam)

        combinedChart = findViewById(R.id.combinedChart)
        edtChonNam = findViewById(R.id.edtChonNam)
        btnTruyVan = findViewById(R.id.btnTruyVan)
        dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
        auth = FirebaseAuth.getInstance()

        btnTruyVan.setOnClickListener {
            val year = edtChonNam.text.toString().trim()
            if (year.isNotEmpty()) {
                fetchChartData(year)
            } else {
                Toast.makeText(this@DoThiNamActivity, "Vui lòng chọn năm", Toast.LENGTH_SHORT).show()
            }
        }

        networkChangeReceiver = NetworkChangeReceiver()
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@DoThiNamActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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

    private fun fetchChartData(year: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.email?.substringBefore("@")
            val thuRef = dbRef.child(userId!!).child("Nam").child("Thu")
            val chiRef = dbRef.child(userId).child("Nam").child("Chi")
            val thuList = mutableListOf<employeeThu>()
            thuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (thuSnapshot in snapshot.children) {
                        val thu = thuSnapshot.getValue(employeeThu::class.java)
                        thu?.let {
                            if (it.dateThu?.endsWith("/$year") == true) {
                                thuList.add(it)
                            }
                        }
                    }
                    fetchChiData(chiRef, thuList, year)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun fetchChiData(chiRef: DatabaseReference, thuList: List<employeeThu>, year: String) {
        val chiList = mutableListOf<employeeChi>()
        chiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chiSnapshot in snapshot.children) {
                    val chi = chiSnapshot.getValue(employeeChi::class.java)
                    chi?.let {
                        if (it.dateChi?.endsWith("/$year") == true) {
                            chiList.add(it)
                        }
                    }
                }
                setupChartData(thuList, chiList, year)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    data class ThangDoanhThu(val thu: Float, val chi: Float)
    private fun setupChartData(thuList: List<employeeThu>, chiList: List<employeeChi>, yearSelect: String) {
        val barEntries = ArrayList<BarEntry>()
        val lineEntries = ArrayList<Entry>()
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val thangDoanhThuList = MutableList(12) { ThangDoanhThu(0f, 0f) }
        for (thu in thuList) {
            val dateParts = thu.dateThu?.split("/")
            val month = dateParts?.getOrNull(1)?.toIntOrNull() ?: continue
            val thuAmount = thu.luongThu?.toFloatOrNull() ?: 0f
            thangDoanhThuList[month - 1] = thangDoanhThuList[month - 1].copy(thu = thangDoanhThuList[month - 1].thu + thuAmount)
            // đảm bảo tháng 1 sẽ ứng với mảng ptu 0
        }
        for (chi in chiList) {
            val dateParts = chi.dateChi?.split("/")
            val month = dateParts?.getOrNull(1)?.toIntOrNull() ?: continue
            val chiAmount = chi.luongChi?.toFloatOrNull() ?: 0f
            thangDoanhThuList[month - 1] = thangDoanhThuList[month - 1].copy(chi = thangDoanhThuList[month - 1].chi + chiAmount)
            //đảm bảo tháng 1 sẽ ứng với mảng ptu 0
        }
        for ((index, thangDoanhThu) in thangDoanhThuList.withIndex()) {
            barEntries.add(BarEntry(index.toFloat(), thangDoanhThu.thu))
            lineEntries.add(Entry(index.toFloat(), thangDoanhThu.chi))
        }
        val barDataSet = BarDataSet(barEntries, "Thu nhập").apply {
            color = resources.getColor(R.color.green)
        }
        val lineDataSet = LineDataSet(lineEntries, "Chi tiêu").apply {
            color = resources.getColor(R.color.red)
            setCircleColor(resources.getColor(R.color.yellow)) //màu hình tròn
            circleRadius = 2.5f   //kích thước hình tròn
        }
        val barData = BarData(barDataSet as IBarDataSet)
        val lineData = LineData(lineDataSet as ILineDataSet)

        barData.notifyDataChanged()
        lineData.notifyDataChanged()

        val combinedData = CombinedData().apply {
            setData(barData)
            setData(lineData)
        }
        combinedData.notifyDataChanged()
        combinedChart.apply {
            data = combinedData
            xAxis.valueFormatter = IndexAxisValueFormatter(months)  //định dạng trục x theo mảng months
            xAxis.granularity = 1f  //khoảng cách ptu trong trục x là 1 đơn vị
            xAxis.setLabelCount(months.size, true)   //số nhãn trên trục x là số lượng các tháng
            xAxis.position = XAxis.XAxisPosition.BOTTOM    //trục x bên dưới biểu đồ
            axisRight.isEnabled = false   //tắt hiển thị trục y bên phải biểu đồ
            legend.isEnabled = true  //hiển thị chú thích
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP   //chú thích hiển thị bên trên
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT  //chú thích ở bên phải
            legend.orientation = Legend.LegendOrientation.HORIZONTAL  //chú thích nằm ngang
            legend.setDrawInside(false)  //chú thích không nằm vào trong biểu đồ
            description.isEnabled = false  //tắt mô tả đồ thị
            setPinchZoom(true)   //cho phép phóng to thu nhỏ đồ thị
            isDoubleTapToZoomEnabled = true   //phóng to khi nhấn 2 lần
            invalidate()   //vẽ lại đồ thị khi data thay đổi
        }
    }
}

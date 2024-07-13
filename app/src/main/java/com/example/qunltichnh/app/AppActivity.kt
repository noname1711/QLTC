package com.example.qunltichnh.app

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.qunltichnh.MainActivity
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.custom.CustomAdapter
import com.example.qunltichnh.outdata.OutData

class AppActivity : AppCompatActivity() {

    lateinit var customAdapter: CustomAdapter

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        val txtLogOut = findViewById<TextView>(R.id.txtLogOut)
        txtLogOut.setOnClickListener {
            // Xóa trạng thái đăng nhập khỏi SharedPreferences
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("isChecked")
            editor.apply()

            // Chuyển về MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Đóng activity hiện tại để ngăn người dùng quay lại màn hình app
        }



        val list = mutableListOf<OutData>()
        //getString mới chuyển ngôn ngữ trong out data đc
        list.add(OutData(R.drawable.nam, getString(R.string.nam), getString(R.string.cho_nam)))
        list.add(OutData(R.drawable.nu, getString(R.string.nu), getString(R.string.cho_nu)))
        list.add(OutData(R.drawable.giadinh, getString(R.string.gia_dinh), getString(R.string.cho_family)))
        list.add(OutData(R.drawable.congty, getString(R.string.cong_ty), getString(R.string.cho_company)))

        customAdapter = CustomAdapter(this, list)
        val lvCheDo = findViewById<ListView>(R.id.lvCheDo)
        lvCheDo.adapter = customAdapter

        lvCheDo.setOnItemClickListener { parent, view, position, id ->

            when (position) {
                0 -> {
                    val intent = Intent(this, NamActivity::class.java)
                    startActivity(intent)
                }

                1 -> {
                    val intent = Intent(this, NuActivity::class.java)
                    startActivity(intent)
                }

                2 -> {
                    val intent = Intent(this, FamilyActivity::class.java)
                    startActivity(intent)
                }

                3 -> {
                    val intent = Intent(this, CompanyActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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
}

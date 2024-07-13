package com.example.qunltichnh.login

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.qunltichnh.MainActivity
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    public override fun onStart() {
        super.onStart()
        // ktra xem nếu tk khác null thì chạy hàm reload
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Tải lại thành công
                val updatedUser = auth.currentUser
                updateUI(updatedUser)
            } else {
                // Xảy ra lỗi khi tải lại
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val txtToMain = findViewById<TextView>(R.id.txtToMain)
        txtToMain.setOnClickListener {
            val i = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(i)
        }

        val edtGmail = findViewById<EditText>(R.id.edtGmail)
        val edtPassWord = findViewById<TextInputEditText>(R.id.edtPassWord)
        val btnXacThuc = findViewById<Button>(R.id.btnXacThuc)
        val btnSent = findViewById<Button>(R.id.btnSent)
        btnSent.setOnClickListener{
            val strGmail = edtGmail.text.toString().trim()
            if (strGmail.isEmpty()) {
                Toast.makeText(this@LoginActivity, R.string.email_none, Toast.LENGTH_SHORT).show()
                return@setOnClickListener  //thoát khỏi lệnh đang đc thực hiện khi bị rỗng
            }
            guiMaXacThuc(strGmail)
        }
        btnXacThuc.setOnClickListener {
            val strGmail = edtGmail.text.toString().trim()
            val strPassWord = edtPassWord.text.toString().trim()
            if (strGmail.isEmpty() || strPassWord.isEmpty()) {
                Toast.makeText(this@LoginActivity, R.string.dien_full, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            kiemTraXacThucEmail(strGmail, strPassWord)
        }
        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@LoginActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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

    private fun guiMaXacThuc(email: String) {
        auth.createUserWithEmailAndPassword(email, "matkhau_tamthoi")  // pass tạm thời để firebase gửi link xác thực đến gmail
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()   //gửi email
                        ?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(baseContext, R.string.vao_xacthuc, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(baseContext, R.string.guimafail, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(baseContext, R.string.ko_thay, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun kiemTraXacThucEmail(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, "matkhau_tamthoi")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                dangKyTaiKhoan(email, pass)
                            } else {
                                Toast.makeText(baseContext, R.string.xt_tb, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(baseContext, R.string.chua_xt, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(baseContext, R.string.dk_tb, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun dangKyTaiKhoan(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, R.string.tao_tk_tc, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, R.string.tao_tk_tb, Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            Log.d("LoginActivity", "User Email: ${currentUser.email}")
        } else {
            Log.d("LoginActivity", "User không tồn tại")
        }
    }
}
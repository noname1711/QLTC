package com.example.qunltichnh

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.qunltichnh.app.AppActivity
import com.example.qunltichnh.databinding.ActivityMainBinding
import com.example.qunltichnh.login.IntroActivity
import com.example.qunltichnh.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    public override fun onStart() {
        super.onStart()
        // Kiểm tra xem người dùng đã đăng nhập chưa (ko null) và cập nhật giao diện người dùng tương ứng.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
        // Kiểm tra trạng thái của checkbox từ SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isChecked = sharedPreferences.getBoolean("isChecked", false)

        if (isChecked) {
            // Nếu checkbox được chọn, chuyển sang màn hình app luôn mà không cần đăng nhập lại
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val i = Intent(this, AppActivity::class.java)
                startActivity(i)
                finish() // Đóng MainActivity để ngăn người dùng quay lại màn hình đăng nhập
            }
        }

    }

    private fun reload() {
        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Tải lại thành công
                val updatedUser = auth.currentUser
                updateUI(updatedUser)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnMeo.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=XYec-SBZE0Y"))
            startActivity(i)
        }

        binding.btnIntro.setOnClickListener {
            val i = Intent(this, IntroActivity::class.java)
            startActivity(i)
        }

        binding.btnDangKi.setOnClickListener {
            val i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }
        auth = Firebase.auth
        onStart()


        binding.btnDangNhap.setOnClickListener {
            val strGmail = binding.edtUser.getText().toString().trim()
            val strPassWord = binding.edtPass.getText().toString().trim()
            Login(strGmail,strPassWord)
        }

        //quên mật khẩu
        binding.txtQuenPass.setOnClickListener {
            showForgotPasswordDialog()
        }

        networkChangeReceiver = NetworkChangeReceiver()
        // Kiểm tra trạng thái mạng ban đầu
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this@MainActivity, R.string.loss_internet, Toast.LENGTH_SHORT).show()
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


    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)  // Khởi tạo AlertDialog Builder
        builder.setTitle(R.string.reset_pass)  // Đặt tiêu đề cho dialog
        // Khởi tạo EditText để người dùng nhập email
        val input = EditText(this)
        input.hint = "Email"
        // Tạo một LinearLayout để chứa EditText
        val container = LinearLayout(this)
        // Đặt LayoutParams cho LinearLayout
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // Đặt margin cho EditText để căn chỉnh khoảng cách từ mép dialog
        params.setMargins(57, 0, 50, 0)  // Thay đổi giá trị margin để phù hợp
        input.layoutParams = params  // Áp dụng LayoutParams cho EditText
        container.addView(input)  // Thêm EditText vào LinearLayout
        builder.setView(container)  // Thiết lập LinearLayout làm view cho dialog
        // Thiết lập nút "Gửi" và hành động khi nhấn nút
        builder.setPositiveButton(R.string.send) { dialog, which ->
            val email = input.text.toString().trim()  // Lấy giá trị email người dùng nhập
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)  // Gửi email đặt lại mật khẩu
            } else {
                Toast.makeText(this, R.string.email_none, Toast.LENGTH_SHORT).show()  // Hiển thị thông báo lỗi nếu email trống
            }
        }
        // Thiết lập nút "Hủy" và hành động khi nhấn nút
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        builder.show()  // Hiển thị dialog
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)    //gửi email reset
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.check_email_to_reset_password, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.failed_email_to_reset_pass, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun Login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            // Nếu email hoặc mật khẩu trống, hiển thị thông báo
            Toast.makeText(
                baseContext,
                R.string.blank,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // thành công thì update UI vs thông tin mới
                    Toast.makeText(baseContext, R.string.login_success, Toast.LENGTH_SHORT).show()
                    val currentUser = auth.currentUser
                    updateUI(currentUser)
                    // Kiểm tra xem người dùng hiện tại có tồn tại trong Firebase không
                    if (currentUser != null) {
                        val i = Intent(this, AppActivity::class.java)
                        startActivity(i)
                    } else {
                        // Người dùng không tồn tại trong Firebase, không chuyển sang giao diện của app
                        Toast.makeText(
                            baseContext,
                            R.string.invalid_info,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // đăng nhập thất bại thì thông báo
                    Toast.makeText(
                        baseContext,
                        R.string.invalid_info,
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
        // Lưu trạng thái của checkbox vào SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isChecked", binding.chkLuuDangNhap.isChecked)
        editor.apply()

    }


    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            // Hiển thị thông tin người dùng trong logcat
            Log.d("LoginActivity", "User Email: ${currentUser.email}")
        } else {
            Log.d("LoginActivity", "${R.string.user_notexist}")
        }
    }
}
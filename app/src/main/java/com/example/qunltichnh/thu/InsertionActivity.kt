package com.example.qunltichnh.thu

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.employeeThu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class InsertionActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null

    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    // Đăng ký các kết quả từ ActivityResultContracts(xử lí kết quả từ activity và quyền trong android)
    private val captureImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            uri?.let { uploadImage(it) }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            this.uri = it
            uploadImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertion)

        // Khởi tạo Firebase Database và Storage
        dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
        storageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").getReference("images")
        auth = FirebaseAuth.getInstance()

        // Nút thoát
        val imgBtnBack = findViewById<ImageButton>(R.id.imgBtnBack)
        imgBtnBack.setOnClickListener {
            finish()
        }

        // Cài đặt thời gian mặc định và nút chọn thời gian
        val btnLich = findViewById<ImageButton>(R.id.BtnLich)
        val btnDongHo = findViewById<ImageButton>(R.id.BtnDongHo)
        val txtTime = findViewById<TextView>(R.id.txtTime)
        val txtDate = findViewById<TextView>(R.id.txtDate)

        val today = Calendar.getInstance()
        val startYear = today.get(Calendar.YEAR)
        val startMonth = today.get(Calendar.MONTH)
        val startDay = today.get(Calendar.DAY_OF_MONTH)
        val startHour = today.get(Calendar.HOUR_OF_DAY)
        val startMinute = today.get(Calendar.MINUTE)

        // Hiển thị TimePickerDialog để chọn thời gian
        btnDongHo.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                txtTime.text = "$hourOfDay ${getString(R.string.gio)} $minute ${getString(R.string.phut)}"
            }, startHour, startMinute, true).show()
        }

        // Hiển thị DatePickerDialog để chọn ngày
        btnLich.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                txtDate.text = "$dayOfMonth/${month + 1}/$year"
            }, startYear, startMonth, startDay).show()
        }

        // Nút chọn ảnh
        val btnAnhThu = findViewById<Button>(R.id.btnAnhThu)
        btnAnhThu.setOnClickListener {
            showImagePickerDialog()
        }

        // Nút lưu thông tin
        val btnSaveThu = findViewById<Button>(R.id.btnSaveThu)
        btnSaveThu.setOnClickListener {
            if (uri != null) {
                uploadImage(uri!!)
            }
            saveEmployeeData()
        }

        // Kiểm tra kết nối mạng
        networkChangeReceiver = NetworkChangeReceiver()
        val initialNetworkStatus = networkChangeReceiver.isNetworkAvailable(this)
        networkChangeReceiver.isConnected = initialNetworkStatus
        if (!initialNetworkStatus) {
            Toast.makeText(this, R.string.loss_internet, Toast.LENGTH_SHORT).show()
        }
    }

    // Đăng ký và hủy đăng ký bộ thu trạng thái mạng
    override fun onResume() {
        super.onResume()
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    // Hiển thị dialog chọn ảnh
    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>(getString(R.string.chup_anh), getString(R.string.chon_anh))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.chon_image))
        builder.setItems(options) { _, item ->
            when (options[item]) {
                getString(R.string.chup_anh)-> {
                    val photoUri = createImageUri()
                    captureImage.launch(photoUri)
                    uri = photoUri
                }
                getString(R.string.chon_anh) -> {
                    pickImage.launch("image/*")
                }
            }
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    // Tạo URI cho ảnh chụp
    private fun createImageUri(): Uri? {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "new_image.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // Tải ảnh lên Firebase Storage
    private fun uploadImage(uri: Uri) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = (user.email ?: "").substringBefore("@")
            val imageRef = storageRef.child(userId).child("Nam").child("Thu").child("image/${uri.lastPathSegment}")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.tai_anh_tc, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "${getString(R.string.tai_anh_tb)} ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Lưu dữ liệu
    private fun saveEmployeeData() {
        val currentUser = auth.currentUser
        val edtKhoanThu = findViewById<EditText>(R.id.edtKhoanThu)
        val edtLuongThu = findViewById<EditText>(R.id.edtLuongThu)

        currentUser?.let { user ->
            val userId = (user.email ?: "").substringBefore("@")
            val empDate = findViewById<TextView>(R.id.txtDate).text.toString()
            val empTime = findViewById<TextView>(R.id.txtTime).text.toString()
            val empKhoanThu = edtKhoanThu.text.toString()
            val empLuongThu = edtLuongThu.text.toString()
            val idThu = dbRef.push().key!!

            val employeeDataRef = dbRef.child(userId)
                .child("Nam")
                .child("Thu")
                .child(idThu)

            val employeeThu = employeeThu(idThu, empDate, empTime, empKhoanThu, empLuongThu)

            if (empKhoanThu.isEmpty()) {
                edtKhoanThu.error = "Vui lòng nhập chi tiết khoản thu"
                return
            }
            if (empLuongThu.isEmpty()) {
                edtLuongThu.error = "Vui lòng nhập chi tiết khoản thu"
                return
            }

            employeeDataRef.setValue(employeeThu)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đã thêm dữ liệu thành công", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { err ->
                    Toast.makeText(this, "Lỗi: ${err.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}


package com.example.qunltichnh.thu

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.qunltichnh.NetworkChangeReceiver
import com.example.qunltichnh.R
import com.example.qunltichnh.adapter.employeeThu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

class InsertionThuFamilyActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_insertion_thu_family)

        // Reference là tham chiếu
        dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
        storageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").getReference("images")
        //phải có đường link nếu sử dụng server asia
        auth = FirebaseAuth.getInstance()
        //nút thoát
        val imgBtnBackFamily= findViewById<ImageButton>(R.id.imgBtnBackFamily)
        imgBtnBackFamily.setOnClickListener{
            finish()
        }
        //pick thời gian mặc định là thực hoặc tùy chọn
        val btnLich = findViewById<ImageButton>(R.id.BtnLichFamily)
        val btnDongHo = findViewById<ImageButton>(R.id.BtnDongHoFamily)
        val txtTime = findViewById<TextView>(R.id.txtTimeFamily)
        val txtDate = findViewById<TextView>(R.id.txtDateFamily)
        //thời gian thực
        val today = Calendar.getInstance()
        val startYear = today.get(Calendar.YEAR)
        val startMonth = today.get(Calendar.MONTH)
        val startDay = today.get(Calendar.DAY_OF_MONTH)
        val startHour = today.get(Calendar.HOUR_OF_DAY)
        val startMinute = today.get(Calendar.MINUTE)
        //chọn time set cho txt
        btnDongHo.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                txtTime.text = "$hourOfDay ${getString(R.string.gio)} $minute ${getString(R.string.phut)}"
            }, startHour, startMinute, true).show()
        }
        btnLich.setOnClickListener {
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    txtDate.setText("$dayOfMonth/${month + 1}/$year")     //tháng trong kotlin tính từ 0 dd/mm/yyyy
                }, startYear, startMonth, startDay).show()
        }
        // Nút chọn ảnh
        val btnAnhThu = findViewById<Button>(R.id.btnAnhThuFamily)
        btnAnhThu.setOnClickListener {
            showImagePickerDialog()
        }

        // Nút lưu thông tin
        val btnSaveThu = findViewById<Button>(R.id.btnSaveThuFamily)
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
            val imageRef = storageRef.child(userId).child("Family").child("Thu").child("image/${uri.lastPathSegment}")  //ảnh đi kèm với định dạng của ảnh
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.tai_anh_tc, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "${getString(R.string.tai_anh_tb)} ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveEmployeeData() {
        val currentUser = auth.currentUser
        val edtKhoanThu = findViewById<EditText>(R.id.edtKhoanThuFamily)
        val edtLuongThu = findViewById<EditText>(R.id.edtLuongThuFamily)

        currentUser?.let { user ->
            val userId = (user.email?: "").substringBefore("@")   //firebase ko nhận dấu @ và . nên chỉ lấy phần trước dấu @ của gmail
            val empDate = findViewById<TextView>(R.id.txtDateFamily).text.toString()
            val empTime = findViewById<TextView>(R.id.txtTimeFamily).text.toString()
            val empKhoanThu = findViewById<EditText>(R.id.edtKhoanThuFamily).text.toString()
            val empLuongThu = findViewById<EditText>(R.id.edtLuongThuFamily).text.toString()
            val idThu = dbRef.push().key!!   //khóa chính

            val employeeDataRef = dbRef.child(userId)  //gmail người dùng
                .child("Family")
                .child("Thu")
                .child(idThu)

            val employeeThu = employeeThu(idThu,empDate,empTime,empKhoanThu,empLuongThu)

            // ko để trống dữ liệu
            if (empKhoanThu.isEmpty()){
                edtKhoanThu.error = "Vui lòng nhập chi tiết khoản thu"
                return
            }
            if (empLuongThu.isEmpty()){
                edtLuongThu.error = "Vui lòng nhập chi tiết khoản thu"
                return
            }

            // truyền dữ liệu lên realtime
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
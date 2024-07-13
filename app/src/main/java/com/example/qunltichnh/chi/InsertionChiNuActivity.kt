package com.example.qunltichnh.chi

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
import com.example.qunltichnh.adapter.employeeChi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar

class InsertionChiNuActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_insertion_chi_nu)

        /*FirebaseDatabase.getInstance().setPersistenceEnabled(true)   //mở lưu trữ offline*/
        dbRef = FirebaseDatabase.getInstance("https://quanlichitieu-2f5e2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
        storageRef = FirebaseStorage.getInstance("gs://quanlichitieu-2f5e2.appspot.com").getReference("images")
        //phải có đường link nếu sử dụng server asia
        auth = FirebaseAuth.getInstance()
        //nút thoát
        val imgBtnBackChi= findViewById<ImageButton>(R.id.imgBtnBackChiNu)
        imgBtnBackChi.setOnClickListener{
            finish()
        }

        //pick thời gian mặc định là thực hoặc tùy chọn
        val btnLich = findViewById<ImageButton>(R.id.BtnLichChiNu)
        val btnDongHo = findViewById<ImageButton>(R.id.BtnDongHoChiNu)
        val txtTimeChi = findViewById<TextView>(R.id.txtTimeChiNu)
        val txtDateChi = findViewById<TextView>(R.id.txtDateChiNu)
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
                txtTimeChi.setText("$hourOfDay giờ $minute phút")
            }, startHour, startMinute, true).show()
        }
        btnLich.setOnClickListener {
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    txtDateChi.setText("$dayOfMonth/${month + 1}/$year")     //tháng trong kotlin tính từ 0
                }, startYear, startMonth, startDay).show()
        }
        // Nút chọn ảnh
        val btnAnhChi = findViewById<Button>(R.id.btnAnhChiNu)
        btnAnhChi.setOnClickListener {
            showImagePickerDialog()
        }

        // Nút lưu thông tin
        val btnSaveChi = findViewById<Button>(R.id.btnSaveChiNu)
        btnSaveChi.setOnClickListener {
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
        val options = arrayOf<CharSequence>("Chụp ảnh", "Chọn từ thư viện")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn ảnh")
        builder.setItems(options) { _, item ->
            when (options[item]) {
                "Chụp ảnh" -> {
                    val photoUri = createImageUri()
                    captureImage.launch(photoUri)
                    uri = photoUri
                }
                "Chọn từ thư viện" -> {
                    pickImage.launch("image/*")
                }
            }
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
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
            val imageRef = storageRef.child(userId).child("Nu").child("Chi").child("image/${uri.lastPathSegment}")   //ảnh đi kèm với định dạng của ảnh
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Tải ảnh lên thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveEmployeeData() {
        val currentUser = auth.currentUser
        val edtKhoanChi = findViewById<EditText>(R.id.edtKhoanChiNu)
        val edtLuongChi = findViewById<EditText>(R.id.edtLuongChiNu)
        currentUser?.let { user ->
            val userId = (user.email?: "").substringBefore("@")   //firebase ko nhận dấu @ và . nên chỉ lấy phần trước dấu @ của gmail
            val empDateChi = findViewById<TextView>(R.id.txtDateChiNu).text.toString()
            val empTimeChi = findViewById<TextView>(R.id.txtTimeChiNu).text.toString()
            val empKhoanChi = findViewById<EditText>(R.id.edtKhoanChiNu).text.toString()
            val empLuongChi = findViewById<EditText>(R.id.edtLuongChiNu).text.toString()
            val idChi = dbRef.push().key!!   //khóa chính

            val employeeDataRef = dbRef.child(userId)  //gmail người dung
                .child("Nu")
                .child("Chi")
                .child(idChi)

            /*val chi = intent.getStringExtra("POSITION").toString()   //nhận lấy vị trí và gắn kèm cả tên từ NamActivity*/
            val employeeChi = employeeChi(idChi,empDateChi,empTimeChi,empKhoanChi,empLuongChi)

            // không để trống thông tin
            if (empDateChi.isEmpty()){
                Toast.makeText(this,"Vui lòng chọn ngày/tháng/năm", Toast.LENGTH_SHORT).show()
                return
            }
            if (empTimeChi.isEmpty()){
                Toast.makeText(this,"Vui lòng chọn giờ/phút", Toast.LENGTH_SHORT).show()
                return
            }
            if (empKhoanChi.isEmpty()){
                edtKhoanChi.error = "Vui lòng nhập chi tiết khoản chi"
                return
            }
            if (empLuongChi.isEmpty()){
                edtLuongChi.error = "Vui lòng nhập chi tiết khoản chi"
                return
            }

            // truyền data lên realtime
            employeeDataRef.setValue(employeeChi)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đã thêm dữ liệu thành công", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { err ->
                    Toast.makeText(this, "Lỗi: ${err.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
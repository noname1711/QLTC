package com.example.qunltichnh.chat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunltichnh.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PickChatFamilyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var createChatButton: Button
    private lateinit var joinChatButton: Button
    private lateinit var returnToChatButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_chat_family)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        createChatButton = findViewById(R.id.button)
        joinChatButton = findViewById(R.id.button2)
        returnToChatButton = findViewById(R.id.returnToChatButton)

        createChatButton.setOnClickListener {
            // Chuyển sang Activity tạo phòng chat
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }

        joinChatButton.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.isEmailVerified) {
                // Kiểm tra xem người dùng đã có phòng chat hay chưa
                val userId = currentUser.uid
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val roomId = documentSnapshot.getString("roomId")
                            if (roomId != null && roomId.isNotEmpty()) {
                                // Nếu có phòng chat, chuyển người dùng vào phòng chat đó
                                val intent = Intent(this, ChatRoomActivity::class.java)
                                startActivity(intent)
                            } else {
                                // Nếu không có phòng chat, hiển thị thông báo
                                Toast.makeText(this, "Bạn chưa có phòng chat. Vui lòng tạo phòng chat trước khi tham gia", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Nếu không có dữ liệu người dùng, hiển thị thông báo
                            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Nếu email chưa xác thực, hiển thị thông báo
                Toast.makeText(this, "Vui lòng xác thực email của bạn để vào phòng chat", Toast.LENGTH_SHORT).show()
            }
        }
        returnToChatButton.setOnClickListener {
            // Chuyển người dùng đến phòng chat đã có sẵn
            val intent = Intent(this, ChatRoomActivity::class.java)
            startActivity(intent)
        }
    }
}

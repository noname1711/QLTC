package com.example.qunltichnh.chat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qunltichnh.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class CreateRoomActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var inviteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        inviteButton = findViewById(R.id.inviteButton)

        inviteButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendInvite(email)
            } else {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendInvite(email: String) {
        // Tạo người dùng mới với email này
        auth.createUserWithEmailAndPassword(email, "defaultPassword123")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    sendVerificationEmail(user)
                    addUserToChatRoom(user)
                } else {
                    Toast.makeText(this, "Mời thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Mail xác thực đến ${user.email}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Xác thực thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserToChatRoom(user: FirebaseUser?) {
        val roomId = "your_chat_room_id"
        val userMap = hashMapOf(
            "uid" to user?.uid,
            "email" to user?.email
        )

        db.collection("chatRooms").document(roomId).collection("members").document(user?.uid!!)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Vào phòng chat", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Vào phòng chat thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

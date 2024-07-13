package com.example.qunltichnh.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qunltichnh.R
import com.example.qunltichnh.chat.adapter.MessageAdapter
import com.example.qunltichnh.chat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var roomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)

        roomId = "your_chat_room_id"  // Lấy roomId từ intent hoặc cách khác

        setupRecyclerView()
        loadMessages()

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = messageAdapter
    }

    private fun loadMessages() {
        db.collection("chatRooms").document(roomId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to load messages: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.toObjects(Message::class.java)
                    messageAdapter.submitList(messages)
                }
            }
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString()
        if (messageText.isEmpty()) {
            return
        }

        val currentUser = auth.currentUser ?: return
        val message = Message(
            senderId = currentUser.uid,
            senderName = currentUser.email ?: "Anonymous",
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chatRooms").document(roomId).collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageEditText.text.clear()
                messagesRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

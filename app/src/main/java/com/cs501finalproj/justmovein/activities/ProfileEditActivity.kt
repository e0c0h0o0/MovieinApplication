package com.cs501finalproj.justmovein.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cs501finalproj.justmovein.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileEditActivity : BaseActivity() {

    val database = Firebase.database
    val user = Firebase.auth.currentUser
    lateinit var userId: String
    lateinit var origName: String
    lateinit var origEmail: String

    lateinit var nameEdit: EditText
    lateinit var emailEdit: EditText
    lateinit var saveButton: Button

    // var userRef

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_edit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameEdit = findViewById(R.id.u_name_edit)
        emailEdit = findViewById(R.id.u_email_edit)
        saveButton = findViewById(R.id.u_save_button)


        userId = intent.getStringExtra("USER_ID")!!

        val databaseRef = database.getReference("user/$userId")
        databaseRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    origName = snapshot.child("name").getValue(String::class.java)!!
                    origEmail = snapshot.child("email").getValue(String::class.java)!!
                    this@ProfileEditActivity.runOnUiThread {
                        nameEdit.setText(origName)
                        emailEdit.setText(origEmail)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) { }

        })

        saveButton.setOnClickListener {
            val newName = nameEdit.text.toString()
            val newEmail = emailEdit.text.toString()

            if(newName != origName) {
                databaseRef.child("name").setValue(newName)
            }
            if(newEmail != origEmail) {
                databaseRef.child("email").setValue(newName)
            }

            Toast.makeText(this@ProfileEditActivity, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}
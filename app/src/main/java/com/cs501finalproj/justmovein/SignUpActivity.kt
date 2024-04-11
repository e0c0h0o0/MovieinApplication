package com.cs501finalproj.justmovein

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var txtLogIn: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDfRef:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()
        edtName = findViewById(R.id.edt_name)
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edtPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnSignUp. setOnClickListener{
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            signUp(name,email,password)
        }
        txtLogIn = findViewById(R.id.txtLogIn)
        txtLogIn.setOnClickListener{
            val intent = Intent(this,LogInActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun signUp(name:String,email:String,password:String){
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // code for  jumping to home
                    mAuth.currentUser?.let { addUserToDatabase(name,email, it.uid) }
                    val   intent = Intent(this@SignUpActivity, MainActivity::class.java)
//                    finish()
                    startActivity(intent)

                } else {
                    Log.e("SignUpError", "Sign-up failed", task.exception)
                    Toast.makeText(baseContext, "Sign-up failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }
    @SuppressLint("SuspiciousIndentation")
    private  fun addUserToDatabase(name:String, email: String, uid:String){
    mDfRef = FirebaseDatabase.getInstance().getReference()
        mDfRef.child("user").child(uid).setValue(User(name, email, uid))
    }
}
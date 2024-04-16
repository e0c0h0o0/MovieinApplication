package com.cs501finalproj.justmovein

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.firebase.auth.FirebaseAuth

class    LogInActivity : AppCompatActivity() {
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogIn: Button
    private lateinit var txtSignUp: TextView
    private lateinit var mAuth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()

        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogIn= findViewById(R.id.btnLogin)
        txtSignUp = findViewById(R.id.btnSignUp)
        txtSignUp.setOnClickListener{
             val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }
        btnLogIn.setOnClickListener{
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            login(email,password)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun login(email:String,password:String){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                   //code for logging in user
                    val  intent = Intent(this@LogInActivity, MainActivity::class.java)
//                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LogInActivity,"User is not exist",Toast.LENGTH_LONG).show()
                }
            }

    }
}
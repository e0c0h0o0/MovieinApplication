package com.cs501finalproj.justmovein

import android.content.Intent
import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.cs501finalproj.justmovein.databinding.ActivityMainBinding
import com.cs501finalproj.justmovein.util.UiUtil

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class  MainActivity : AppCompatActivity() {
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mDbRef:DatabaseReference
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.bottomNavBar.setOnItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.bottom_menu_housing->{
                    UiUtil.showToast(this,"Home")
                }
                R.id.bottom_menu_trade->{
                    UiUtil.showToast(this,"trade  ")
                }
                R.id.bottom_menu_profile->{
                    //Goto ProfileActivity
                    val intent = Intent(this,ProfileActivity::class.java)
                    intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid )
                    startActivity(intent)
                }
            }
            false
        }
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }



}
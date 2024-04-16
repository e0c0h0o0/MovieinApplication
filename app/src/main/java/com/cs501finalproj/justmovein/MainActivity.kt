package com.cs501finalproj.justmovein

import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_trading -> {
                    val fragment = TradeMainFragment()
                    switchFragment(fragment)
                }
                R.id.navigation_profile -> {
                    FirebaseAuth.getInstance().currentUser?.let {
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                    } ?: run {
                        Log.d("MainActivity", "Redirecting to login - no user logged in.")
                        val loginIntent = Intent(this, LogInActivity::class.java)
                        startActivity(loginIntent)
                    }
                    false
                }
                else -> false
            }
            true
        }
        bottomNav.selectedItemId = R.id.navigation_trading
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
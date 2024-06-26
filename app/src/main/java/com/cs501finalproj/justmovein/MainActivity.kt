package com.cs501finalproj.justmovein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.cs501finalproj.justmovein.activities.BaseActivity
import com.cs501finalproj.justmovein.util.UiUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : BaseActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UiUtil.setupStyleWhenAppRun(this)


        enableEdgeToEdge()
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()

        // Check if the user is already logged in
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            // User is logged in, set the content view to the main layout
            setContentView(R.layout.activity_main)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        try {
            bottomNav.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_housing -> {
                        val fragment = HousingMainFragment()
                        switchFragment(fragment)
                    }
                    R.id.navigation_trading -> {
                        val fragment = TradeMainFragment()
                        switchFragment(fragment)
                    }
                    R.id.navigation_profile -> {
                        val intent = Intent(this,ProfileActivity::class.java)
                        intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid )
                        startActivity(intent)
                        true
                    }
                }
                true
            }
        }
        catch (_: Exception) {

        }
        bottomNav.selectedItemId = R.id.navigation_trading
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
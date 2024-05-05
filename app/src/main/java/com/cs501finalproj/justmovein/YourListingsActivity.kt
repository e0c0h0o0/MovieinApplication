package com.cs501finalproj.justmovein

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs501finalproj.justmovein.activities.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class YourListingsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_listings)

        recyclerView = findViewById(R.id.rvYourListings)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemAdapter = ItemAdapter(mutableListOf(), "listing_items")
        recyclerView.adapter = itemAdapter

        loadItems()
    }

    private fun loadItems() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.w("YourListingsActivity", "Current user ID is null")
            return
        }

        FirebaseDatabase.getInstance().getReference("Items")
            .orderByChild("sellerId").equalTo(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val items = mutableListOf<Item>()
                    dataSnapshot.children.forEach { snapshot ->
                        snapshot.getValue(Item::class.java)?.let { item ->
                            items.add(item)
                        }
                    }
                    if (items.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        findViewById<TextView>(R.id.empty_view).visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        findViewById<TextView>(R.id.empty_view).visibility = View.GONE
                        itemAdapter.updateItemList(items)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("YourListingsActivity", "loadItems:onCancelled", databaseError.toException())
                }
            })
    }
}

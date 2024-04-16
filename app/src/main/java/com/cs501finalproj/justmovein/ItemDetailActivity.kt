package com.cs501finalproj.justmovein

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var item: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_detail)

        val itemId = intent.getStringExtra("ITEM_ID")
        val titleView: TextView = findViewById(R.id.item_detail_title)
        val priceView: TextView = findViewById(R.id.item_detail_price)
        val imageView: ImageView = findViewById(R.id.item_detail_image)
        val descriptionView: TextView = findViewById(R.id.item_detail_description)
        val conditionView: TextView = findViewById(R.id.item_detail_condition)
        val categoryView: TextView = findViewById(R.id.item_detail_category)
        val fabLike: FloatingActionButton = findViewById(R.id.fab_like)
        val exit : ImageView = findViewById(R.id.cross)

        if (itemId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Items").child(itemId)
            databaseReference.get().addOnSuccessListener { dataSnapshot ->
                item = dataSnapshot.getValue(Item::class.java) ?: return@addOnSuccessListener
                titleView.text = item.title
                priceView.text = String.format("$%.2f", item.price)
                descriptionView.text = "Description: ${item.description}"
                conditionView.text = "Condition: ${item.condition}"
                categoryView.text = "Category: ${item.category}"
                Glide.with(this).load(item.imageUrl).into(imageView)

                updateLikeButton(fabLike)  // Update like button based on current status
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load item details.", Toast.LENGTH_SHORT).show()
            }
        }

        exit.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        fabLike.setOnClickListener {
            toggleLikeStatus(fabLike)
        }
    }

    private fun updateLikeButton(fabLike: FloatingActionButton) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val isLiked = item.likedBy?.containsKey(userId) ?: false
        fabLike.setImageResource(if (isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24)
    }

    private fun toggleLikeStatus(fabLike: FloatingActionButton) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val isLiked = item.likedBy?.containsKey(userId) ?: false
        if (isLiked) {
            item.likedBy?.remove(userId)
        } else {
            if (item.likedBy == null) item.likedBy = mutableMapOf()
            item.likedBy!![userId] = true
        }

        updateItemInDatabase()
        updateLikeButton(fabLike)
    }

    private fun updateItemInDatabase() {
        val itemId = item.id ?: return
        FirebaseDatabase.getInstance().getReference("Items").child(itemId).setValue(item)
    }
}

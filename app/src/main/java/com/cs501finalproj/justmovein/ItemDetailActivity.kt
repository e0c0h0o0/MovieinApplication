package com.cs501finalproj.justmovein

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.viewpager2.widget.ViewPager2
import com.cs501finalproj.justmovein.activities.BaseActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ItemDetailActivity :BaseActivity() {

    private lateinit var item: Item
    private lateinit var databaseReference: DatabaseReference
    val Int.dp: Int get() = (this * resources.displayMetrics.density + 0.5f).toInt()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        val itemId = intent.getStringExtra("ITEM_ID")
        if (itemId == null) {
            Toast.makeText(this, "Item not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Items").child(itemId)
        val titleView: TextView = findViewById(R.id.item_detail_title)
        val priceView: TextView = findViewById(R.id.item_detail_price)
        val descriptionView: TextView = findViewById(R.id.item_detail_description)
        val conditionView: TextView = findViewById(R.id.item_detail_condition)
        val categoryView: TextView = findViewById(R.id.item_detail_category)
        val fabLike: FloatingActionButton = findViewById(R.id.fab_like)
        val moreOptionsButton: ImageButton = findViewById(R.id.more_options_button)
        val exitButton: ImageView = findViewById(R.id.cross)
        val shareBtn: Button = findViewById(R.id.btnShare)

        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            item = dataSnapshot.getValue(Item::class.java) ?: return@addOnSuccessListener
            updateUI(item, titleView, priceView, descriptionView, conditionView, categoryView, fabLike)
            configureOptionsMenu(moreOptionsButton, item)

            // Initialize contact button after fetching item and seller details
            setupContactButton(item.sellerId, item.title)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load item details.", Toast.LENGTH_SHORT).show()
        }

        setupListeners(fabLike, moreOptionsButton, exitButton, shareBtn)
    }

    private fun setupContactButton(sellerId: String?, itemTitle: String?) {
        val contactBtn: Button = findViewById(R.id.btnContact)
        contactBtn.setOnClickListener {
            if (sellerId != null) {
                Log.d("ItemDetailActivity", "Fetching seller details for ID: $sellerId")
                FirebaseDatabase.getInstance().getReference("user").child(sellerId)
                    .get().addOnSuccessListener { dataSnapshot ->
                        val seller = dataSnapshot.getValue(User::class.java)
                        if (seller != null) {
                            Log.d("ItemDetailActivity", "Seller found: ${seller.name}")
                            val intent = Intent(this@ItemDetailActivity, ContactSellerActivity::class.java).apply {
                                putExtra("SELLER_NAME", seller.name)
                                putExtra("SELLER_ZIPCODE", seller.zipcode)
                                putExtra("ITEM_NAME", itemTitle)
                                putExtra("SELLER_ICON", seller.profilePic)
                                putExtra("SELLER_EMAIL", seller.email)
                            }
                            startActivity(intent)
                        } else {
                            Log.d("ItemDetailActivity", "No data for seller found in database.")
                            Toast.makeText(this@ItemDetailActivity, "Seller details not found.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Log.d("ItemDetailActivity", "Failed to fetch seller details: ${it.message}")
                        Toast.makeText(this@ItemDetailActivity, "Failed to load seller details.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@ItemDetailActivity, "Seller ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupListeners(fabLike: FloatingActionButton, moreOptionsButton: ImageButton, exitButton: ImageView, shareBtn : Button) {
        fabLike.setOnClickListener {
            toggleLikeStatus(fabLike)
        }

        moreOptionsButton.setOnClickListener { view ->
            showPopupMenu(view, item)
        }

        exitButton.setOnClickListener{
            onBackPressed()
        }

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"

            // Add item details to the shared text
            val sharedText = "Check out on JustMoveIn!\n\nItem: ${item.title}\nPrice: ${item.price}\nDescription: ${item.description}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, sharedText)

            // Add item image to the shared intent
            val imageUri = Uri.parse(item.imageUrls.toString())
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    private fun updateUI(item: Item, titleView: TextView, priceView: TextView,
                         descriptionView: TextView, conditionView: TextView, categoryView: TextView,
                         fabLike: FloatingActionButton) {
        titleView.text = item.title
        priceView.text = String.format("$%.2f", item.price ?: 0.0)
        descriptionView.text = "Description:\n${item.description}\n"
        conditionView.text = "Condition:\n${item.condition}\n"
        categoryView.text = "Category:\n${item.category}"

        // Update for multiple images
        val viewPager = findViewById<ViewPager2>(R.id.image_viewpager)
        viewPager.adapter = ImageAdapter(this, item.imageUrls ?: emptyList())

        updateLikeButton(fabLike)
    }


    private fun updateLikeButton(fabLike: FloatingActionButton) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val isLiked = item.likedBy?.containsKey(userId) ?: false
        fabLike.setImageResource(if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24)
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

    private fun configureOptionsMenu(anchorView: View, item: Item) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (item.sellerId == currentUserId) {
            anchorView.visibility = View.VISIBLE
            anchorView.setOnClickListener { view ->
                showPopupMenu(view, item)
            }
        } else {
            anchorView.visibility = View.GONE
        }
    }

    private fun toggleItemActive(item: Item, isActive: Boolean) {
        item.active = isActive
        databaseReference.setValue(item)
            .addOnSuccessListener {
                Toast.makeText(this, if (isActive) "Item activated" else "Item deactivated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update item status.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteItem(item: Item) {
        databaseReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity or navigate user away
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete item.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPopupMenu(anchorView: View, item: Item) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.item_options_menu, popup.menu)

        if (item.active == true) {
            popup.menu.findItem(R.id.action_deactivate).isVisible = true
            popup.menu.findItem(R.id.action_activate).isVisible = false
        } else {
            popup.menu.findItem(R.id.action_deactivate).isVisible = false
            popup.menu.findItem(R.id.action_activate).isVisible = true
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_deactivate -> {
                    toggleItemActive(item, false)
                    true
                }
                R.id.action_activate -> {
                    toggleItemActive(item, true)
                    true
                }
                R.id.action_delete -> {
                    deleteItem(item)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
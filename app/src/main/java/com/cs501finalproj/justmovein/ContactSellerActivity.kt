package com.cs501finalproj.justmovein

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cs501finalproj.justmovein.databinding.ActivityContactSellerBinding

class ContactSellerActivity : AppCompatActivity() {

    lateinit var binding: ActivityContactSellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sellerName = intent.getStringExtra("SELLER_NAME")
        val sellerZipcode = intent.getStringExtra("SELLER_ZIPCODE")
        val itemName = intent.getStringExtra("ITEM_NAME")
        val sellerIconUrl = intent.getStringExtra("SELLER_ICON")
        val sellerEmail = intent.getStringExtra("SELLER_EMAIL")

        binding.sellerName.text = sellerName
        binding.sellerZipcode.text = sellerZipcode
        binding.itemName.text = "Listing '$itemName'"

        // Load seller icon using Glide
        sellerIconUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_account_circle)
                .error(R.drawable.ic_account_circle) // Placeholder if loading fails
                .circleCrop()
                .into(binding.sellerIcon)
        }

        binding.cross.setOnClickListener{
            onBackPressed()
        }

        binding.buttonContact.setOnClickListener {
            // Send email to the seller
            if (sellerEmail != null) {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(sellerEmail))
                    putExtra(Intent.EXTRA_SUBJECT, "Someone is interested in your listing \"$itemName\"!")
                }
                if (emailIntent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(emailIntent, "Send Email"))
                } else {
                    Toast.makeText(this, "No email application found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Seller email address is missing.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


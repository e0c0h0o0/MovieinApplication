package com.cs501finalproj.justmovein

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs501finalproj.justmovein.activities.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ListItemActivity : BaseActivity() {

    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCondition: Spinner
    private lateinit var edtPrice: EditText
    private lateinit var btnList: Button
    private lateinit var imgCamera: ImageView
    private lateinit var itemImg : ImageView
    private lateinit var exit : ImageView
    private var imageUrl: String? = null

    companion object {
        private const val IMAGE_PICK_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_item)

        edtTitle = findViewById(R.id.edt_title)
        edtDescription = findViewById(R.id.edt_description)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCondition = findViewById(R.id.spinnerCondition)
        edtPrice = findViewById(R.id.edtPrice)
        btnList = findViewById(R.id.btnList)
        imgCamera = findViewById(R.id.camera)
        itemImg = findViewById(R.id.item_image_1)
        exit = findViewById(R.id.cross)

        imgCamera.setOnClickListener {
            pickImage()
        }

        btnList.setOnClickListener {
            listNewItem()
        }

        exit.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                // Update the ImageView to display the selected image
                itemImg.setImageURI(imageUri)
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun listNewItem() {
        val title = edtTitle.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val condition = spinnerCondition.selectedItem.toString()
        val priceText = edtPrice.text.toString().trim()

        if (validateInputs(title, description, category, condition, priceText)) {
            val price = priceText.toDoubleOrNull()
            if (price != null && imageUrl != null) {  // Ensure imageUrl is not null
                val item = Item(
                    title = title,
                    description = description,
                    category = category,
                    condition = condition,
                    price = price,
                    imageUrl = imageUrl,
                    active = true,
                    timestamp = System.currentTimeMillis(),
                    sellerId = FirebaseAuth.getInstance().currentUser?.uid // Assuming the user is logged in
                )
                pushItemToFirebase(item)
            } else {
                if (imageUrl == null) {
                    Toast.makeText(this,
                        "Please wait for the image to finish uploading", Toast.LENGTH_SHORT).show()
                }
                if (price == null) {
                    edtPrice.error = "Invalid price"
                }
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(title: String, description: String, category: String, condition: String, price: String): Boolean {
        var valid = true
        if (title.isEmpty()) {
            edtTitle.error = "Title is required"
            valid = false
        }
        if (description.isEmpty()) {
            edtDescription.error = "Description is required"
            valid = false
        }
        return valid
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("itemImages/${System.currentTimeMillis()}_image.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    imageUrl = uri.toString()  // Update the imageUrl variable
                    itemImg.setImageURI(imageUri) // Set the image to the ImageView after upload confirmation
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pushItemToFirebase(item: Item) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Items")
        val itemId = databaseRef.push().key  // Generate a unique key for the item
        item.id = itemId
        if (itemId != null) {
            databaseRef.child(itemId).setValue(item)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,
                            "Item listed successfully!", Toast.LENGTH_SHORT).show()
                        val  intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this,
                            "Failed to list item.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

package com.cs501finalproj.justmovein

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ListItemActivity : AppCompatActivity() {

    private lateinit var edtTitle: EditText
    private lateinit var edtDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCondition: Spinner
    private lateinit var edtPrice: EditText
    private lateinit var btnList: Button
    private lateinit var imgCamera: ImageView
    private lateinit var imgCameraClickArea: ImageView
    private lateinit var txtCameraLabel : TextView
    private lateinit var back : ImageView
    private val images: List<ImageView> by lazy {
        listOf(
            findViewById(R.id.item_image_1),
            findViewById(R.id.item_image_2),
            findViewById(R.id.item_image_3)
        )
    }
    private var imageUris = mutableListOf<Uri>()
    private var uploadUrls = mutableListOf<String>()

    companion object {
        private const val IMAGE_PICK_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_item)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        edtTitle = findViewById(R.id.edt_title)
        edtDescription = findViewById(R.id.edt_description)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCondition = findViewById(R.id.spinnerCondition)
        edtPrice = findViewById(R.id.edtPrice)
        btnList = findViewById(R.id.btnList)
        imgCamera = findViewById(R.id.camera)
        txtCameraLabel = findViewById(R.id.add_up_to_5)
        imgCameraClickArea = findViewById(R.id.camera_click_area)
        back = findViewById(R.id.cross)
    }

    private fun setupListeners() {
        back.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        imgCameraClickArea.setOnClickListener {
            pickImages()
        }

        btnList.setOnClickListener {
            if (imageUris.isNotEmpty()) {
                uploadImagesAndListNewItem()
            } else {
                Toast.makeText(this, "Please select images for the listing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null) {
                imageUris.clear()
                uploadUrls.clear()
                for (i in 0 until clipData.itemCount.coerceAtMost(3)) {
                    imageUris.add(clipData.getItemAt(i).uri)
                    images[i].setImageURI(clipData.getItemAt(i).uri)
                    updateUIAfterImageSelection()
                }
            } else if (data?.data != null) {
                imageUris.clear()
                uploadUrls.clear()
                imageUris.add(data.data!!)
                images[0].setImageURI(data.data)
                updateUIAfterImageSelection()
            }
        }
    }

    private fun updateUIAfterImageSelection() {
        if (imageUris.isNotEmpty()) {
            txtCameraLabel.visibility = View.INVISIBLE // Hide the text
            imgCamera.visibility = View.INVISIBLE // Hide the icon
        } else {
            txtCameraLabel.visibility = View.VISIBLE
            imgCamera.visibility = View.VISIBLE
        }
    }

    private fun uploadImagesAndListNewItem() {
        val title = edtTitle.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val condition = spinnerCondition.selectedItem.toString()
        val price = edtPrice.text.toString().trim().toDoubleOrNull()

        if (validateInputs(title, description, category, condition, price)) {
            uploadImages { imageUrls ->
                listNewItem(title, description, category, condition, price!!, imageUrls)
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadImages(onUploadComplete: (List<String>) -> Unit) {
        val uploadedUrls = mutableListOf<String>()
        imageUris.forEach { uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("itemImages/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUrl ->
                    uploadedUrls.add(downloadUrl.toString())
                    if (uploadedUrls.size == imageUris.size) {
                        onUploadComplete(uploadedUrls)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun listNewItem(title: String, description: String, category: String, condition: String, price: Double, imageUrls: List<String>) {
        val item = Item(
            title = title,
            description = description,
            category = category,
            condition = condition,
            price = price,
            imageUrls = imageUrls, // This should be a list of strings.
            active = true,
            timestamp = System.currentTimeMillis(),
            sellerId = FirebaseAuth.getInstance().currentUser?.uid
        )
        val databaseRef = FirebaseDatabase.getInstance().getReference("Items").push()
        item.id = databaseRef.key
        databaseRef.setValue(item).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Item listed successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to list item.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(title: String, description: String, category: String, condition: String, price: Double?): Boolean {
        return title.isNotEmpty() && description.isNotEmpty() && category.isNotEmpty() && condition.isNotEmpty() && price != null
    }
}

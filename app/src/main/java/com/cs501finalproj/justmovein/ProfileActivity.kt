package com.cs501finalproj.justmovein

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cs501finalproj.justmovein.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    lateinit var profileUserId : String
    lateinit var currentUserId : String
    lateinit var photoLauncher: ActivityResultLauncher<Intent>
    lateinit var profileUserModel : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val backButton = findViewById<ImageView>(R.id.backbtn)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // If you want to clear all previous activities on the stack
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Optional, if you want to close the current activity
        }
        profileUserId = intent.getStringExtra("profile_user_id")!!
        currentUserId =  FirebaseAuth.getInstance().currentUser?.uid!!

        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == RESULT_OK){
                uploadToFirestore(result.data?.data!!)
            }
        }

        if(profileUserId==currentUserId) {
            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener {
                logout()
            }
            binding.profilePic.setOnClickListener {
              checkPermissionAndPickPhoto()
            }
        }
//        }else{
//            binding.profileBtn.text = "Follow"
//            binding.profileBtn.setOnClickListener {
//                followUnfollowUser()
//            }
//        }
        getProfileDataFromFirebase()
    }

//    fun followUnfollowUser(){
//        Firebase.firestore.collection("users")
//            .document(currentUserId)
//            .get()
//            .addOnSuccessListener {
//                val currentUserModel = it.toObject(User::class.java)!!
//                updateUserData(profileUserModel)
//                updateUserData(currentUserModel)
//            }
//    } 
//

    fun uploadToFirestore(photoUri : Uri){
        binding.progressBar.visibility = View.VISIBLE
        val photoRef =    FirebaseStorage.getInstance()
            .reference
            .child("profilePic/"+ currentUserId )
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener {downloadUrl->
                    //video model store in firebase firestore
                    postToFirestore(downloadUrl.toString())
                }
            }
    }

    fun postToFirestore(url : String){
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .update("profilePic",url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
        val databaseReference = FirebaseDatabase.getInstance().getReference("user/$currentUserId")
        databaseReference.child("profilePic").setValue(url)
    }

    fun checkPermissionAndPickPhoto(){
        var readExternalPhoto : String = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES
        }else{
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if(ContextCompat.checkSelfPermission(this,readExternalPhoto)== PackageManager.PERMISSION_GRANTED){
            //we have permission
            openPhotoPicker()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalPhoto),
                100
            )
        }
    }
    private fun openPhotoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }
    fun logout(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this,LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    fun getProfileDataFromFirebase(){
        val databaseReference = FirebaseDatabase.getInstance().getReference("user/$profileUserId")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    val email = dataSnapshot.child("email").getValue(String::class.java)
                    val profilePic = dataSnapshot.child("profilePic").getValue(String::class.java)
                    if (name != null && email != null) {
                        profileUserModel = User(name, email, profileUserId,profilePic ?: "")
                        setUI()
                    }
                } else {
                    Log.d("ProfileActivity", "No user data found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ProfileActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })
//        Firebase.firestore.collection("users")
//            .document(profileUserId)
//            .get()
//            .addOnSuccessListener {
//                profileUserModel = it.toObject(User::class.java)!!
//                setUI()
//            }
    }

    fun setUI(){
        profileUserModel.apply {
            Glide.with(binding.profilePic).load(profilePic)
                .load(profilePic ?: R.drawable.icon_account_circle) // Use default if null
                .circleCrop()
                .into(binding.profilePic)
            binding.profileUsername.text = name ?: "No Name"
            binding.profileEmail.text = email ?: "No Email"
            binding.progressBar.visibility = View.INVISIBLE

        }
    }
}































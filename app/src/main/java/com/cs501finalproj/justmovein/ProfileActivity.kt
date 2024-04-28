package com.cs501finalproj.justmovein

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cs501finalproj.justmovein.activities.BaseActivity
import com.cs501finalproj.justmovein.activities.ProfileEditActivity
import com.cs501finalproj.justmovein.databinding.ActivityProfileBinding
import com.cs501finalproj.justmovein.util.UiUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.processphoenix.ProcessPhoenix


class ProfileActivity : BaseActivity() {

    lateinit var binding: ActivityProfileBinding
    lateinit var profileUserId : String
    lateinit var currentUserId : String
    lateinit var photoLauncher: ActivityResultLauncher<Intent>
    lateinit var profileUserModel : User
    var database = Firebase.database
    var auth = Firebase.auth

    lateinit var uBtnEditProfile: Button
    lateinit var uSwitchNightMode: SwitchCompat
    lateinit var uSwitchPrivateAccount: SwitchCompat
    lateinit var uItemSecurityPrivacy: RelativeLayout
    lateinit var uNotificationConfigItem: RelativeLayout
    lateinit var uLanguageSettingItem: RelativeLayout
    lateinit var uSendUsMessageItem: RelativeLayout
    lateinit var uDeleteMyAccount: RelativeLayout

    lateinit var languages: Array<CharSequence>

    val mailbox = "sundar@google.com"

    override fun onResume() {
        super.onResume()
        getProfileDataFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Switch language


        binding  = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        languages = arrayOf("Default", "English", "Japanese")

        uBtnEditProfile = findViewById(R.id.u_btn_edit_profile)
        uSwitchNightMode = findViewById(R.id.u_switch_night_mode)
        //uSwitchPrivateAccount = findViewById(R.id.u_switch_private_account)
        uItemSecurityPrivacy = findViewById(R.id.u_item_security_privacy)
        uNotificationConfigItem = findViewById<RelativeLayout>(R.id.u_notification_config)
        uLanguageSettingItem = findViewById<RelativeLayout>(R.id.u_language_setting)
        uSendUsMessageItem = findViewById<RelativeLayout>(R.id.u_send_us_message)

        uDeleteMyAccount = findViewById(R.id.u_delete_my_account)

        uDeleteMyAccount.setOnClickListener {
            auth.currentUser!!.delete()
                .addOnFailureListener{  }
                .addOnSuccessListener {
                    Toast.makeText(this@ProfileActivity, "Your account has been deleted", Toast.LENGTH_SHORT).show()
                }
        }


        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        uSendUsMessageItem.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("mailto:")) // only email apps should handle this

            intent.putExtra(Intent.EXTRA_EMAIL, mailbox)
            intent.putExtra(Intent.EXTRA_SUBJECT, "About JustMoveIn's customer feedback")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            else {
                Toast.makeText(this@ProfileActivity, "Email app not found, email has copied", Toast.LENGTH_SHORT).show()
                val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", mailbox)

                clipboard.setPrimaryClip(clip)
            }
        }

        uBtnEditProfile.setOnClickListener {
            val intent = Intent(this@ProfileActivity, ProfileEditActivity::class.java)
            intent.putExtra("USER_ID", profileUserId);
            startActivity(intent)
        }

        //uSwitchPrivateAccount.setOnCheckedChangeListener { buttonView, isChecked ->
        //    Firebase.database.getReference("user/$profileUserId").child("isPrivateAccount").setValue(isChecked)
        //}

        uItemSecurityPrivacy.setOnClickListener {
            val intent = Intent(this@ProfileActivity, SecurityAndPrivacyActivity::class.java)
            startActivity(intent)
        }

        uNotificationConfigItem.setOnClickListener {
            val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(settingsIntent)
        }

        uLanguageSettingItem.setOnClickListener {
            val dialogBuilder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            dialogBuilder.setTitle(getString(R.string.select_a_language))
            dialogBuilder.setSingleChoiceItems(languages, -1,
                DialogInterface.OnClickListener { dialog, item ->
                    val userChoice = languages.get(item)
                    editor.putString("language", userChoice.toString())
                    editor.commit()
                    handleNewLanguage()
                    dialog.dismiss() // dismiss the alertbox after chose option

                })
            val alert: android.app.AlertDialog? = dialogBuilder.create()
            alert!!.show()
        }

        //set the night mode
        //val switch = findViewById<SwitchCompat>(R.id.btnswitch)

//        val nightMode = sharedPreferences.getBoolean("night",false)
//        if (nightMode){
//            switch.isChecked = true
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
        uSwitchNightMode.isChecked = sharedPreferences.getBoolean("night", false)

        uSwitchNightMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked){
                // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                UiUtil.setupStyle(this@ProfileActivity.applicationContext, false)
                editor.putBoolean("night", false)
                editor.apply()

            }else{
                UiUtil.setupStyle(this@ProfileActivity.applicationContext, true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("night", true)
                editor.apply()

            }
        }
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
            binding.profileBtn.text = getString(R.string.logout)
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
                    var isPrivateAccount = false
                    val resultFromRemote = dataSnapshot.child("isPrivateAccount").getValue(Boolean::class.java)
                    if(resultFromRemote != null) {
                        isPrivateAccount = resultFromRemote
                    }

                    //uSwitchPrivateAccount.isChecked = isPrivateAccount

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
            binding.profileUsername.text = name ?: getString(R.string.no_name)
            binding.profileEmail.text = email ?: getString(R.string.no_email)
            binding.progressBar.visibility = View.INVISIBLE

        }
    }

    fun handleNewLanguage() {
        ProcessPhoenix.triggerRebirth(this)
    }

}































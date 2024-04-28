package com.cs501finalproj.justmovein.activities

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs501finalproj.justmovein.R
import com.cs501finalproj.justmovein.util.UiUtil

open class BaseActivity: AppCompatActivity() {

    lateinit var progressDialog: ProgressDialog

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        UiUtil.setApplicationLocale(this, UiUtil.getLocaleCode(this))
    }


    fun showProgressDialog() {
        if(!::progressDialog.isInitialized) {
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage(getString(R.string.loading))
            progressDialog.isIndeterminate = true
        }

        progressDialog.show()
    }

    fun hideProgressDialog() {
        if(::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        hideProgressDialog()
    }



}
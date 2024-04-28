package com.cs501finalproj.justmovein.util

import android.app.UiModeManager
import android.content.Context
import android.content.Context.UI_MODE_SERVICE
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import java.util.Locale


object UiUtil {

    fun showToast(context : Context,message : String){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }

    fun setupStyle(context: Context, isDarkMode: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager?
            if(isDarkMode) {
                uiModeManager!!.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            }
            else {
                uiModeManager!!.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            }
        } else {
            if(isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
            else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }
    }

    fun setupStyleWhenAppRun(context: Context) {
        val sharedPreferences = context.getSharedPreferences("Mode", Context.MODE_PRIVATE)
        UiUtil.setupStyle(context.applicationContext, sharedPreferences.getBoolean("night", false));
    }

    fun setApplicationLocale(context: Context, locale: String) {
        if(locale.isEmpty()) { return }
        val resources: Resources = context.getResources()
        val dm: DisplayMetrics = resources.getDisplayMetrics()
        val config: Configuration = resources.getConfiguration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(Locale(locale.lowercase(Locale.getDefault())))
        } else {
            config.locale = Locale(locale.lowercase(Locale.getDefault()))
        }
        resources.updateConfiguration(config, dm)
    }

    fun getLocaleCode(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "Default")
        return when(language) {
            "Default" -> Locale.getDefault().getLanguage()
            "Japanese" -> "ja"
            "English" -> "en"
            else -> ""
        }
    }

}
package com.dokugo.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dokugo.R

class ProfileAvatarActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_avatar)

        findViewById<View>(R.id.avatar1).setOnClickListener(this)
        findViewById<View>(R.id.avatar2).setOnClickListener(this)
        findViewById<View>(R.id.avatar3).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val avatarUrl = when (view.id) {
            R.id.avatar1 -> "https://storage.googleapis.com/dokugo-storage/avatar1.png"
            R.id.avatar2 -> "https://storage.googleapis.com/dokugo-storage/avatar2.png"
            R.id.avatar3 -> "https://storage.googleapis.com/dokugo-storage/avatar3.png"
            else -> ""
        }

        val resultIntent = Intent()
        resultIntent.putExtra("avatar_url", avatarUrl)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
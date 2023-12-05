package com.example.isable_capstone.ui.authentication.sign_in

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.isable_capstone.R

class sign_in_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val toolbar = findViewById<Toolbar>(R.id.xml_toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title=getString(R.string.sign_in)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Tindakan yang akan diambil saat tombol back ditekan
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
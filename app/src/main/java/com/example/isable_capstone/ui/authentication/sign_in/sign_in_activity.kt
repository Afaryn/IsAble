package com.example.isable_capstone.ui.authentication.sign_in

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.example.isable_capstone.MainActivity
import com.example.isable_capstone.R
import com.example.isable_capstone.databinding.ActivitySignInBinding

class sign_in_activity : AppCompatActivity() {

private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.xml_toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title=getString(R.string.sign_in)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSignIn.setOnClickListener{
            startActivity(Intent(this@sign_in_activity, MainActivity::class.java))
        }


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
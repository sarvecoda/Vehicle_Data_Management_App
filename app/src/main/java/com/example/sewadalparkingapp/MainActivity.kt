package com.example.sewadalparkingapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.sewadalparkingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding :ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val fixedPassword:String = "nirankar"
        val fixedUsername:String = "nirankar"


        binding?.welcomeToMainBtn?.setOnClickListener{
            val userName:String = binding?.userName?.text.toString()
            val password:String = binding?.password?.text.toString()
            if(password == fixedPassword && userName == fixedUsername){
                val intent = Intent(this, MainScreen::class.java)
                startActivity(intent)
            }else{
                Toast.makeText(this@MainActivity, "The username or the password is wrong", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
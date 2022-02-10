package com.ruta.perubus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class BeginningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beginning)

    }

    fun getIngresar(view: View){
        val intent = Intent(this, CredentialActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun getRegistro(view: View){
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
        finish()
    }
}
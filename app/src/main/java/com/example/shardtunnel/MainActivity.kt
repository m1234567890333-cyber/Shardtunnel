package com.example.shardtunnel

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var ipInput: EditText
    private lateinit var portInput: EditText
    private lateinit var keyInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        ipInput = EditText(this).apply { hint = "IP Сервера" }
        portInput = EditText(this).apply { setText("443") }
        keyInput = EditText(this).apply { hint = "Ключ из консоли" }
        val btnConnect = Button(this).apply { text = "ПОДКЛЮЧИТЬ" }

        layout.addView(ipInput); layout.addView(portInput)
        layout.addView(keyInput); layout.addView(btnConnect)
        setContentView(layout)

        btnConnect.setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent != null) startActivityForResult(intent, 0) else startVpn()
        }
    }

    private fun startVpn() {
        val intent = Intent(this, MyVpnService::class.java).apply {
            putExtra("server_ip", ipInput.text.toString())
            putExtra("server_port", portInput.text.toString().toIntOrNull() ?: 443)
            putExtra("key", keyInput.text.toString())
        }
        startService(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) startVpn()
    }
}

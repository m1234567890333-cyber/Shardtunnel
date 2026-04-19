package com.example.shardtunnel
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = true
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val serverIp = intent?.getStringExtra("server_ip") ?: return START_NOT_STICKY
        val serverPort = intent.getIntExtra("server_port", 443)
        val builder = Builder().setSession("ShardTunnel").addAddress("10.0.0.2", 24).addRoute("0.0.0.0", 0)
        vpnInterface = builder.establish()
        thread { runTunnel(serverIp, serverPort) }
        return START_STICKY
    }
    private fun runTunnel(ip: String, port: Int) {
        val fd = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(fd)
        val socket = DatagramSocket()
        val address = InetAddress.getByName(ip)
        val buffer = ByteArray(2048)
        try {
            while (isRunning) {
                val length = inputStream.read(buffer)
                if (length > 0) {
                    val dp = DatagramPacket(buffer, length, address, port)
                    socket.send(dp)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
    override fun onDestroy() { isRunning = false; super.onDestroy() }
}

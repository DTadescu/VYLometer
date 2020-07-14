package shdv.iotdev.vylometer.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

object BtUtils {

    /**
     * Our mobile bluetooth
     */
    private val btAdapter = BluetoothAdapter.getDefaultAdapter()
    private val baseUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    /**
     * Socket for data receiving/sending
     */
    private lateinit var btSocket: BluetoothSocket
    /**
     * Connected device
     */
    var vyd: BluetoothDevice? = null

    /**
     * Enabled and connected to device
     */
    fun work() = enabled() && connected()

    private fun ByteArray.toHexString(): String {
        return this.joinToString(" ") {
            java.lang.String.format("%02x", it)
        }
    }

    fun connected() = this::btSocket.isInitialized && btSocket.isConnected

    fun enabled() = btAdapter != null && btAdapter.isEnabled

    /**
     * Turn on bluetooth adapter on mobile device
     */
    fun enable(activity: Activity): Boolean {
        return if (btAdapter != null) {
            if (!btAdapter.isEnabled) {
                val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                btIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.applicationContext.startActivity(btIntent)
            }
            true
        } else {
            false
        }
    }

    /*
    0 - OK
    1 - Device with name VYD not founded
    2 - Could not create RFCOMM socket
    3 - Could not close connection
    4 - Could not connect
    5 - No Bluetooth adapter
     */

    /**
     * Try to connect to VYD
     * @return code, that informs if was connected or not
     */
    private fun connectToVyd(name: String): Int {

        //Get device with name == param

        if (btAdapter == null)
            return 5

        for (i in btAdapter.bondedDevices) {
            if (i.name == name)
                vyd = i
        }

        if (vyd == null) {
            return 1
        }

        //Create socket for sending/receiving data

        try {
            btSocket = vyd!!.createRfcommSocketToServiceRecord(baseUuid)
        } catch(e: IOException) {
            Log.d("CONNECTTHREAD", "Could not create RFCOMM socket: $e")
            return 2
        }

        try {
            btSocket.connect()
        } catch (e: IOException) {
            Log.d("CONNECTTHREAD", "Could not connect: $e")
            try {
                btSocket.close()
            } catch (ec: IOException) {
                Log.d("CONNECTTHREAD", "Could not close connection: $ec")
                return 3
            }
            return 4
        }

        return 0
    }

    /**
     * Do all for connection to Vyd and call onConnect or onError
     */
    suspend fun connect(onConnect: () -> Unit, onError: (Int) -> Unit, name: String) {
        //connecting = true

        val connectionDef = GlobalScope.async {
            if (!connected()) {
                connectToVyd(name)
            }
            else
                0
        }

        GlobalScope.launch(Dispatchers.Main) {
            val a = connectionDef.await()

            if (a == 0) {
                onConnect()
            }
            else {
                onError(a)
            }
        }
    }

    /**
     *  Read byte
     */
    fun readByteAsync() = GlobalScope.async {
        try {
            val a = ByteArray(1)
            btSocket.inputStream.read(a)
            return@async a
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readByteArrayAsync(size: Int, toutMs: Long) = GlobalScope.async {
        try {
            val a = ByteArray(size)
            var counter = toutMs
            while (btSocket.inputStream.available() < size){
                delay(1)
                counter--
                if (counter <= 0) return@async 0xFF
            }
            btSocket.inputStream.read(a)
            return@async a

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            btSocket.close()
        } catch (ec: IOException) {
            Log.d("CONNECTTHREAD", "Could not close connection: $ec")
            return
        }
    }

}
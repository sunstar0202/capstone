package com.example.capstone

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import java.util.*

class BluetoothManager(private val address: String) {
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = null

    fun connect(): Boolean {
        return try {
            val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun send(cmd: String) {
        try {
            socket?.outputStream?.write(cmd.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
package com.example.capstone

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import java.util.UUID

class BluetoothManager(private val address: String) {

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var socket: BluetoothSocket? = null

    fun connect(): Boolean {
        return try {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
            socket = null

            val device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)

            socket = device.javaClass
                .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                .invoke(device, 1) as BluetoothSocket

            socket?.connect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun send(cmd: String) {
        try {
            socket?.outputStream?.write((cmd + "\n").toByteArray())
            socket?.outputStream?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        socket = null
    }
}
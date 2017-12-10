package com.example.nico.bluetooth_raspberry;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Nico on 01/12/2017.
 */


public class ClientBluetooth extends Thread {
    private BluetoothSocket blueSocket ;
    private BluetoothDevice blueDevice;
    OutputStream out;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public ClientBluetooth(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        blueDevice = device;
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            blueSocket = tmp;
        } catch (IOException e) {
        }
    }

    public void run() {

        try {
            blueSocket.connect();
            out = blueSocket.getOutputStream();

        } catch (IOException connectException) {
            try {
                blueSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        manageConnectedSocket(blueSocket);
    }

    public void sendString (String message){
        try {
            if(out != null) {
                out.write(message.getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void manageConnectedSocket(BluetoothSocket blueSocket) {
    }

    public void cancel() {
        try {
            blueSocket.close();
        } catch (IOException e) { }
    }
}
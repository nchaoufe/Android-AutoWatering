package autowatering.fr.autowatering;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Nabil on 28/03/2015.
 */
public class BluetoothService implements Serializable {
    private static final String TAG = "BluetoothService";
    private static final int RETRY = 3;
    private static final int BT_WAIT_MS = 10; // Bluetooth wait millisecond
    private static final int TIMEOUT_THRESHOLD = 100;

    private MainActivity activity;
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean connected = false;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    BluetoothDevice device = null;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothService(MainActivity activity) {
        this.activity = activity;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            Toast.makeText(activity.getApplicationContext(), "Bluetooth Not supported. Aborting.", Toast.LENGTH_LONG).show();
            activity.finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void connectDevice(String address) {

        device = btAdapter.getRemoteDevice(address);
        if (device != null) {
            try {
//                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//                btAdapter.cancelDiscovery();
//                btSocket.connect();
                btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});

                btSocket = (BluetoothSocket) m.invoke(device, 1);
                btAdapter.cancelDiscovery();
                btSocket.connect();
                outStream = btSocket.getOutputStream();
                inStream = btSocket.getInputStream();
                //beginListenForData();
                setConnected(true);
                //Toast.makeText(activity, "Connecté à Arduino", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                disconnect();
                Log.v(TAG, e.getMessage());
                //Toast.makeText(activity, "Problem de connexion Bluetooth!", Toast.LENGTH_LONG).show();

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

/*    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            activity.processBTData(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        stopWorker = true;
                        disconnect();
                    }
                }
            }
        });

        workerThread.start();
    }*/

    public synchronized void sendRequest(String request) {
        int elapsed = 0;
        final byte delimiter = 10;
        readBuffer = new byte[1024];
        boolean responseReceived = false;
        try {
            outStream.write(request.getBytes());

            while (elapsed <= TIMEOUT_THRESHOLD && !responseReceived) {
                try {
                    int bytesAvailable = inStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        inStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;
                                activity.processBTData(data);
                                responseReceived = true;
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }

                    }
                    SystemClock.sleep(BT_WAIT_MS);
                } catch (Exception ex) {
                    disconnect();
                    ex.printStackTrace();
                }
                Log.v(TAG, "Retry=" + elapsed);
                elapsed++;
            }
        } catch (IOException e) {

            disconnect();
            Log.v(TAG, e.getMessage());
            //Toast.makeText(activity.getApplicationContext(), "Problem de connexion Bluetooth!", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        activity.updateBluetoothStatus();
    }

    public String getInfo() {
        StringBuilder result = new StringBuilder();
        if (connected && device != null) {
            result.append("Connecté à : ");
            result.append(device.getName());
        } else {
            result.append("Non Connecté");
        }
        return result.toString();
    }

    public void disconnect() {

        try {
            btSocket.close();
            if (outStream != null)
                outStream.close();
            if (inStream != null)
                inStream.close();
        } catch (IOException e) {
        }
        setConnected(false);
    }
}

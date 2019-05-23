package com.zeltak.app.callnotifier;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.UUID;


public class CallNotifier extends BroadcastReceiver {
    private final String TAG = "CallNotifier";
    private final String BT_DEV_MAC_ADDR = "00:11:22:33:44:55:66"; // Set for your device

    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean connected = false;
    private BluetoothSocket socket = null;

    private void bt_connect() {
        if(connected && socket != null) {
            return;
        }
        try {
            BluetoothAdapter bt_addapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bt_device = bt_addapter.getRemoteDevice(BT_DEV_MAC_ADDR);
            String dev_name = bt_device.getName() != null ? bt_device.getName() : bt_device.getAddress();
            socket = bt_device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            socket.connect();
            connected = true;
            Log.i(TAG, "connected to " + dev_name);
        } catch(Exception e) {
            connected = false;
            Log.w(TAG, "got exception: " + e.getMessage());
        }
        if(!connected && socket != null) {
            try {
                socket.close();
            } catch(Exception e) {
                Log.e(TAG, "got exception " + e.getMessage());
            }
            socket = null;
        }
    }

    private void bt_disconnect() {
        if(socket != null) {
            try {
                socket.close();
            } catch(Exception e) {
                Log.w(TAG, "got exception " + e.getMessage());
            }
            socket = null;
        }
        connected = false;
    }

    private int bt_write(byte[] data) {
        if(!connected || socket == null) {
            return 0;
        }
        try {
            socket.getOutputStream().write(data);
        } catch(Exception e) {
            Log.e(TAG, "got exception " + e.getMessage());
            return 0;
        }
        return data.length;
    }

    private String get_contact_name(Context ctx, String number) {
        String name = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name;
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        String phone_no;
        String name = "";
        boolean is_incoming = true;
        if(action != null && action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            phone_no = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            is_incoming = false;
            Log.v(TAG, "outgoing call to number " + phone_no);
        } else {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Service.TELEPHONY_SERVICE);
            switch (tm.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    is_incoming = true;
                    phone_no = intent.getStringExtra("incoming_number");
                    Log.v(TAG, "incoming call from number " + phone_no);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    phone_no = intent.getStringExtra("incoming_number");
                    Log.v(TAG, "accepted call from number " + phone_no);
                    break;
                default:
                    Log.i(TAG, "call state " + tm.getCallState());
                    return;
            }
        }
        // now map phone_no to contact name...
        String s = is_incoming ? "Incoming: " : "Outgoing: ";

        if(phone_no != null) {
            s += phone_no;
            name = get_contact_name(ctx, phone_no);
            name += "\n";
        } else {
            s += "unknown ";
        }
        s += "\n";

        int st;
        Log.e(TAG, s);
        bt_connect();
        st = bt_write(s.getBytes());
        st += bt_write(name.getBytes());
        bt_disconnect();
        if(st == 0) {
            Log.w(TAG, "bt_write: failed");
        }
    }
}

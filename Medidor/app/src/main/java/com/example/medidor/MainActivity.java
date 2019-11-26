package com.example.medidor;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listViewDevices;
    //ConectClient cC;

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice[] listPairedDevices;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //inicializar componentes xml
        listViewDevices = (ListView) findViewById(R.id.listViewPaired);


        // Closed aplication if the device don't have bluetooth conection
        if (bluetoothAdapter == null) {
            finish();
        }
        // Enable'll bluetooth conection if it isn't Enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        dispVinculados();

    }

    //Método para buscar los dispositivos vinculados
    public void dispVinculados() {
        //Search paired devices
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        if (bt.size() > 0) {

            int indexPaired = 0;
            String pairedDevicesList[] = new String[bt.size()];
            String pairedDevicesName[] = new String[bt.size()];
             listPairedDevices = new BluetoothDevice[bt.size()];

            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : bt) {
                pairedDevicesName[indexPaired] = device.getName();
                listPairedDevices[indexPaired] = device;
                indexPaired++;
            }
            ArrayAdapter<String> array = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevicesName);

            listViewDevices.setAdapter(array);

        }

        listViewDevices.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent UI = new Intent(getApplicationContext(),MedidorActivity.class);
        UI.putExtra("MAC",(String) listPairedDevices[position].getAddress());
        startActivity(UI);
    }
}

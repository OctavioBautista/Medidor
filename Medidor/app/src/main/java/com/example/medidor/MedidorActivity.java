package com.example.medidor;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MedidorActivity extends AppCompatActivity {


    static final int STATE_MESSAGE_RECEIVED = 5;
    int altura;
    int distanciaTinaco;
    int valores[] = new int[10];
    int porcentaje1;

    //valores XML
    ImageView imageView;
    EditText editTextAlturaTinaco;
    Button btnIniciar;
    TextView distanciaTextView;
    TextView porsentajeTextView;
    Toast toastSuccessfulConection, toastFailedConection;
    Vibrator vibrator;

    MainActivity activity = new MainActivity();
    ConectClient cC;
    CambiarImagen cambiarImagen;

    //MYUUID
    private static final UUID MY_UUID2 = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    //bluetoothAdapter
    BluetoothAdapter bluetoothAdapter = activity.bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medidor);

        //Inicialización de TOAST
        toastSuccessfulConection = Toast.makeText(getApplicationContext(),"Conexión Exitosa",Toast.LENGTH_LONG);
        toastFailedConection = Toast.makeText(getApplicationContext(),"Conexión Fallida",Toast.LENGTH_LONG);

        //inicializar XML
        imageView = findViewById(R.id.imageView);
        editTextAlturaTinaco = findViewById(R.id.idAltura);
        distanciaTextView = findViewById(R.id.mensaje);
        porsentajeTextView = findViewById(R.id.idPorcentaje);
        btnIniciar = findViewById(R.id.btnIniciar);
        vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        imageView.setImageResource(R.drawable.tinaco0);

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                altura = Integer.parseInt(editTextAlturaTinaco.getText().toString());
                editTextAlturaTinaco.setEnabled(false);
                btnIniciar.setEnabled(false);
                porcentaje1 =altura/10;
                for(int i = 0;i<10;i++){
                    valores[i]+=porcentaje1*(i+1);
                }
                cC = new ConectClient(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(getIntent().getStringExtra("MAC")));
                cC.start();
                cambiarImagen = new CambiarImagen();
                cambiarImagen.start();
            }
        });
    }

//Conectar como cliente
    public class ConectClient extends Thread {

        private BluetoothSocket btSocked;
        private BluetoothDevice btDevice;

        public ConectClient(BluetoothDevice device) {
            btDevice = device;
            try {
                btSocked = btDevice.createRfcommSocketToServiceRecord(MY_UUID2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                btSocked.connect();
                Log.i("x", "Conexión exitosa");
                toastSuccessfulConection.show();
                SendReceive dataTrasnfer = new SendReceive(btSocked);
                dataTrasnfer.start();
            } catch (Exception e) {
                Log.i("x", "Conexión Fallida");
                toastFailedConection.show();
                finish();
                try {
                    btSocked.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }
    }

    Handler handler = new Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    switch (msg.what) {
                        //Muestra el mensaje si se recibe correctamente
                        case STATE_MESSAGE_RECEIVED:
                            byte[] readBuff = (byte[]) msg.obj;
                            Log.i("readBuff",readBuff.toString());
                            String tempMsg = new String(readBuff, 0, msg.arg1);
                            Log.i("Dato",tempMsg);
                            distanciaTextView.setText(tempMsg);
                            distanciaTinaco =(int)Float.parseFloat(tempMsg);
                            break;
                    }
                    return true;
                }
            });
    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if(inputStream.available()>4) {
                        bytes = inputStream.read(buffer);
                        Log.i("bytes", Integer.toString(bytes));
                        handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                    }
                    else
                        SystemClock.sleep(200);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class CambiarImagen extends Thread{

        public CambiarImagen(){
        }
        public void run() {
            while (true) {
                if(distanciaTinaco-25>altura-valores[0]){
                    imageView.setImageResource(R.drawable.tinaco0);
                    porsentajeTextView.setText("0%");
                }else{
                    if(distanciaTinaco-25>=altura-valores[1]){
                        imageView.setImageResource(R.drawable.tinaco10);
                        porsentajeTextView.setText("10%");
                    }
                    else {
                        if(distanciaTinaco-25>=altura-valores[2]){
                            imageView.setImageResource(R.drawable.tinaco20);
                            porsentajeTextView.setText("20%");
                        }
                        else {
                            if (distanciaTinaco-25>=altura-valores[3]){
                                imageView.setImageResource(R.drawable.tinaco30);
                                porsentajeTextView.setText("30%");
                            }
                            else{
                                if (distanciaTinaco-25>=altura-valores[4]){
                                    imageView.setImageResource(R.drawable.tinaco40);
                                    porsentajeTextView.setText("40%");
                                }
                                else{
                                    if (distanciaTinaco-25>=altura-valores[5]){
                                        imageView.setImageResource(R.drawable.tinaco50);
                                        porsentajeTextView.setText("50%");
                                    }
                                    else{
                                        if (distanciaTinaco-20>=altura-valores[6]){
                                            imageView.setImageResource(R.drawable.tinaco60);
                                            porsentajeTextView.setText("60%");
                                        }
                                        else{
                                            if (distanciaTinaco-25>=altura-valores[7]){
                                                imageView.setImageResource(R.drawable.tinaco70);
                                                porsentajeTextView.setText("70%");
                                            }
                                            else{
                                                if (distanciaTinaco-25>=altura-valores[8]){
                                                    imageView.setImageResource(R.drawable.tinaco80);
                                                    porsentajeTextView.setText("80%");
                                                }
                                                else{
                                                    if (distanciaTinaco-25>=altura-valores[9]){
                                                        imageView.setImageResource(R.drawable.tinaco90);
                                                        porsentajeTextView.setText("90%");
                                                    }
                                                    else{
                                                        vibrator.vibrate(100);
                                                        imageView.setImageResource(R.drawable.tinaco100);
                                                        porsentajeTextView.setText("100%");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

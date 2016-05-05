package thesis.hoa.myo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends ActionBarActivity implements BluetoothAdapter.LeScanCallback {
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 1;
    public static final int CHECK_CONNECTION = 2;

    /** Device Scanning Time (ms) */
    private static final long SCAN_PERIOD = 5000;

    /** Intent code for requesting Bluetooth enable */
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String TAG = "BLE_Myo";

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt    mBluetoothGatt;
    private TextView         emgDataText;
    private TextView         gestureText;

    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();

    private String deviceName;

    private ImageView gestureImageView;
    private Button startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ready
        startButton = (Button) findViewById(R.id.bStart);
        gestureImageView = (ImageView) findViewById(R.id.gestureImageView);
        gestureImageView.setImageResource(GestureInfo.INIT_PICTURE);

        emgDataText = (TextView)findViewById(R.id.emgDataTextView);
        gestureText = (TextView)findViewById(R.id.gestureTextView);
        mHandler = new Handler();

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(ListActivity.TAG);

        if (deviceName != null) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // Scanning Time out by Handler.
                // The device scanning needs high energy.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(MainActivity.this);
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.startLeScan(this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, MENU_LIST, 0, "Find Myo");
        menu.add(0, MENU_BYE, 0, "Good Bye");
        menu.add(0, CHECK_CONNECTION, 0, "Check connection");
        return true;
    }

    @Override
    public void onStop(){
        super.onStop();
        this.closeBLEGatt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case MENU_LIST:
//                Log.d("Menu","Select Menu A");
                Intent intent = new Intent(this,ListActivity.class);
                startActivity(intent);
                return true;

            case MENU_BYE:
//                Log.d("Menu","Select Menu B");
                closeBLEGatt();
                Toast.makeText(getApplicationContext(), "Close GATT", Toast.LENGTH_SHORT).show();
                return true;
            case CHECK_CONNECTION:
                new CheckConnection().execute();
        }
        return false;
    }

    /** Define of BLE Callback */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceName.equals(device.getName())) {
            mBluetoothAdapter.stopLeScan(this);
            // Trying to connect GATT

            mMyoCallback = new MyoGattCallback(mHandler, emgDataText);
            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }
    }

    public void onClickVibration(View v){
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendVibration3())) {
            Log.d(TAG, "False Vibrate");
        }
    }

    public void onClickUnlock(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendUnLock())) {
            Log.d(TAG,"False UnLock");
        }
    }

    public void onClickEMG(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
            Log.d(TAG,"False EMG");
        } else {

        }
    }

    public void onClickNoEMG(View v) {
        if (mBluetoothGatt == null
                || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
            Log.d(TAG,"False Data Stop");
        }
    }

    public void onClickStart(View v) {
        gestureText.setText("Gesture");
        gestureImageView.setImageResource(GestureInfo.INIT_PICTURE);
        new CollectingData().execute();
    }

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void setGestureText(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                gestureText.setText(message);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(MainActivity.this);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(this);
        }
    }

    private class CheckConnection extends AsyncTask<Void, Void, String>{
        protected String doInBackground(Void... args){
            String result = "";
            StringBuilder sb = new StringBuilder();
            try{
                URL url = new URL(http);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                connection.connect();

                int HttpResult = connection.getResponseCode();
                Log.d("HOA", HttpResult + "");
                if(HttpResult == HttpURLConnection.HTTP_OK){
                    Log.d("HOA", "HTTP_OK");
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            connection.getInputStream(), "utf-8"
                    ));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    JSONObject resultObj = new JSONObject(sb.toString());
                    result = resultObj.getString("connection");
                    br.close();
                }
                else{
                    result = "failed";
                }
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result){
            Toast.makeText(getApplicationContext(), "Connection " + result, Toast.LENGTH_LONG).show();
        }
    }

    private class CollectingData extends AsyncTask<Void, Void, Integer> {
        protected Integer doInBackground(Void... args){
            GestureDetectManager.getInstance().setState(GestureDetectManager.State.Recording);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startButton.setText("Recording...");
                    startButton.setEnabled(false);
                }
            });
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("COLLECTING DATA", "sleep finished");
            return 1;
        }

        protected void onProgressUpdate(Integer... progress) {

        }
        protected void onPostExecute(Integer value){
            Log.d("COLLECTING DATA", "post");
            new GestureDetection().execute();
            GestureDetectManager.getInstance().setState(GestureDetectManager.State.Detecting);
            startButton.setText("Sending data");
        }
    }
    private String http = "http://192.168.143.1:5000/todo/api/v1.0/tasks";

    private class GestureDetection extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... args){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startButton.setText("Detecting");
                }
            });
            int result = -1;
            StringBuilder sb = new StringBuilder();
            try{
                URL url = new URL(http);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.connect();

                JSONObject params = new JSONObject();
                params.put("gesture", 0);
                params.put("seq", GestureDetectManager.getInstance().sendData());
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(params.toString());
                out.flush();
                out.close();

                int responseCode = connection.getResponseCode();
                Log.d("HOA", responseCode + "");
                if(responseCode == HttpURLConnection.HTTP_OK){
                    Log.d("HOA", "HTTP_OK");
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    JSONObject resultObj = new JSONObject(sb.toString());
                    result = resultObj.getInt("gesture_name");
                    br.close();
                }
                else{
                    result = responseCode;
                }
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer result){
            Log.d("SERVER RESULT: ", result + "");
            startButton.setText("Start");
            startButton.setEnabled(true);
            if(GestureInfo.checkGesture(result)) {
                GestureInfo gesture = GestureInfo.gestureName.get(result);
                gestureText.setText(String.valueOf(gesture.name));
                gestureImageView.setImageResource(gesture.pic);
            } else {
                gestureText.setText("Error! Try again!");
                gestureImageView.setImageResource(GestureInfo.INIT_PICTURE);
            }
            GestureDetectManager.getInstance().setState(GestureDetectManager.State.None);
        }
    }
}


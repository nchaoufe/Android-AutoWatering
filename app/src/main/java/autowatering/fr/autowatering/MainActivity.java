package autowatering.fr.autowatering;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import autowatering.fr.autowatering.Utils.JsonUtil;
import autowatering.fr.autowatering.domain.RelaySchedule;

import static autowatering.fr.autowatering.Constants.REQUEST_RELAYS_COUNT;
import static autowatering.fr.autowatering.Constants.REQUEST_SET_TIME;


public class MainActivity extends ActionBarActivity implements OnUpdateListener {

    private ListView relaysListView;
    private OnUpdateListener thisActivity;
    private RelayArrayAdapter relayArrayAdapter;
    private BluetoothService bluetoothService = null;
    private final List<RelaySchedule> relaySchedules = new ArrayList<RelaySchedule>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd|HH|mm|ss");
    private int relaysCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;
        //Init Bluetooth
        bluetoothService = new BluetoothService(this);
        bluetoothService.CheckBTState();

        //ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //init relays listView
        final ListView relaysListView = (ListView) findViewById(R.id.relay_listView);
        relayArrayAdapter = new RelayArrayAdapter(this, R.layout.relay_schedule_item, relaySchedules, this);
        relaysListView.setAdapter(relayArrayAdapter);
        relaysListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                RelaySchedule relaySchedule = (RelaySchedule) relaysListView.getItemAtPosition(position);
                if (!relaySchedule.isForced()) {
                    NewScheduleDialogFragment newScheduleDialogFragment = NewScheduleDialogFragment.newInstance(relaySchedule, thisActivity, bluetoothService);
                    newScheduleDialogFragment.show(getFragmentManager(), "diag");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_set_time) {
            Calendar cal = Calendar.getInstance();
            String request = REQUEST_SET_TIME +":"+ sdf.format(new Date());
            new AsyncBluetoothTask().execute(AsyncBluetoothTask.SEND_REQUEST,request) ;
            return true;
        }
        if (id == R.id.action_connect_to_device) {
            if (bluetoothService.isConnected()) {
                bluetoothService.disconnect();
            } else {
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CONNECT_DEVICE);
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    public void initRelaysListView() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Attempt to connect to the device
                    if (address != null) {
                        new AsyncBluetoothTask().execute(AsyncBluetoothTask.CONNECT,address);
                    }
                }
                break;

        }
    }

    // Async Task Class
    class AsyncBluetoothTask extends AsyncTask<String, String, String> {
        private ProgressDialog loadingDialog;
        public static final String CONNECT = "0";
        public static final String SEND_REQUEST = "1";
        public static final String UPDATE_RELAYS_INFO = "2";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new ProgressDialog((android.content.Context) thisActivity);
            loadingDialog.setMessage("Traitement en cours ....");
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            switch (params[0]) {
                case CONNECT:
                    bluetoothService.connectDevice(params[1]);
                    if (bluetoothService.isConnected()) {
                        //Find relays count
                        bluetoothService.sendRequest(REQUEST_RELAYS_COUNT);
                        sendRelaysInfoRequests();
                    }
                    break;
                case SEND_REQUEST:
                    bluetoothService.sendRequest(params[1]);
                    sendRelaysInfoRequests();
                    break;

                case UPDATE_RELAYS_INFO:
                    sendRelaysInfoRequests();
                    break;
            }

            return null;
        }

        void sendRelaysInfoRequests(){
            for (int i = 0; i < relaysCount; i++) {
                bluetoothService.sendRequest(Constants.REQUEST_RELAY_INFO + ":" + i);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            relayArrayAdapter.notifyDataSetChanged();
            loadingDialog.cancel();
        }

        public boolean isFinished(){
            return getStatus() == Status.FINISHED;
        }
    }


    public void updateRelaysListView() {

        new AsyncBluetoothTask().execute(AsyncBluetoothTask.UPDATE_RELAYS_INFO) ;
    }

    public void refresh(View view) {
        updateRelaysListView();
    }

    public synchronized void processBTData(String data) {
        if (data != null && data.split(":").length > 0) {
            String cmd = data.split(":")[0];
            String content = data.substring(data.indexOf(':') + 1);
            switch (cmd) {
                case REQUEST_RELAYS_COUNT:
                    relaysCount = JsonUtil.parseRelayCountResponse(content);
                    break;
                case Constants.REQUEST_RELAY_INFO:
                    RelaySchedule rs = JsonUtil.parseRelayInfoResponse(content);
                    for (RelaySchedule relaySchedule : relaySchedules) {
                        if (rs.getRelayId() != null && rs.getRelayId().equals(relaySchedule.getRelayId())) {
                            relaySchedules.remove(relaySchedule);
                            break;
                        }
                    }
                    relaySchedules.add(rs);
                    break;
            }
        }
    }

    public void updateBluetoothStatus() {
        final TextView bluetoothInfo = (TextView) findViewById(R.id.bluetoothInfo);
        final ActionMenuItemView connectItem = (ActionMenuItemView) findViewById(R.id.action_connect_to_device);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                bluetoothInfo.setText(bluetoothService.getInfo());
                if (bluetoothService.isConnected()) {
                    connectItem.setIcon(getResources().getDrawable(R.drawable.bluetooth_active));
                } else {
                    connectItem.setIcon(getResources().getDrawable(R.drawable.bluetooth_inactive));
                    relaySchedules.clear();
                    relayArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // cleanup app, save preferences, etc.
        if (keyCode == 4)
            android.os.Process.killProcess(android.os.Process.myPid());
        // finish(); // not working properly, especially not with asynchronous tasks running
        // return moveTaskToBack(true);
        return super.onKeyDown(keyCode, event);
    }

    public void sendBTRequest(String request){
        new AsyncBluetoothTask().execute(AsyncBluetoothTask.SEND_REQUEST,request) ;
    }


}

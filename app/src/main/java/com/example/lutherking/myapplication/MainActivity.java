package com.example.lutherking.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_READ_PHONE_STATE = 37;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "activity_main layout file rendered");
        hasPermissions();

        Spinner spinner = findViewById(R.id.data_unit_spinner);

        spinner.setOnItemSelectedListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.data_speed_units, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        //Setup a FAB to open Cricket score
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CricketScoreActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {


        // An item was selected. You can retrieve the selected item using
        String selected_unit = parent.getItemAtPosition(pos).toString();

        String[] data_units = getResources().getStringArray(R.array.data_speed_units);

        long divisor = 1024*1024*1024;
        String temp = "";

        for (String data_unit : data_units) {
            if (data_unit.equals(selected_unit))
                break;
            else
                divisor = divisor/1024;
        }

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String subscriberID = tm.getSubscriberId();

        //This code is used to monitor app data usage for a particular app
//        PackageManager manager = this.getPackageManager();
//        ApplicationInfo info = null;
//        try {
//            info = manager.getApplicationInfo("com.example.app", 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        int uid = info.uid;

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) this.getSystemService(Context.NETWORK_STATS_SERVICE);

        try {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy", Locale.US);
            String date_string = dateFormat.format(calendar.getTime());

            TextView textView = findViewById(R.id.start_date);
            textView.setText(date_string);

            long start = dateFormat.parse(date_string).getTime();
            long end = System.currentTimeMillis();
            NetworkStats.Bucket networkStatsByApp = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberID, start, end);

            if(networkStatsByApp == null){
                Log.i("Info", "Error");
            }else{
                double total_data_trans  = (networkStatsByApp.getRxBytes() + networkStatsByApp.getTxBytes())/(divisor*1.0);
                Log.i("Info", "Total: " + total_data_trans);


                textView = findViewById(R.id.text_view_network);
                textView.setText( String.format("%.3f", total_data_trans) + selected_unit );
            }
        } catch (RemoteException | ParseException e) {
            e.printStackTrace();
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private boolean hasPermissionToReadPhoneStats() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED;
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);//PERMISSION_READ_PHONE_STATE
    }

    private boolean hasPermissionToReadNetworkHistory() {
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
}

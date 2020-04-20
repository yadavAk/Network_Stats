package com.example.lutherking.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSION_READ_PHONE_STATE = 37;

    /** date variable to store datePicked by user*/
    private static String datePicked = "";

    /** Formatted date to format date */
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("M dd, yyyy", Locale.getDefault());

    /** Spinner field to enter the unit of the data usage */
    private Spinner mDataUnitSpinner;

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "activity_main layout file rendered");

        if(!hasPermissions())
                return ;

        mDataUnitSpinner = findViewById(R.id.data_unit_spinner);

        setupSpinner();


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

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") final String subscriberID = tm.getSubscriberId();

        final NetworkStatsManager networkStatsManager =
                (NetworkStatsManager) this.getSystemService(Context.NETWORK_STATS_SERVICE);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> dataUnitSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.data_speed_units, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        dataUnitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDataUnitSpinner.setAdapter(dataUnitSpinnerAdapter);

        mDataUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An item was selected. You can retrieve the selected item using
                String selected_unit = parent.getItemAtPosition(position).toString();

                String[] data_units = getResources().getStringArray(R.array.data_speed_units);

                long divisor = 1024*1024*1024;

                for (String data_unit : data_units) {
                    if (data_unit.equals(selected_unit))
                        break;
                    else
                        divisor = divisor/1024;
                }

                // This code is used to monitor app data usage for a particular app
                // PackageManager manager = this.getPackageManager();
                // ApplicationInfo info = null;
                // try {
                //  info = manager.getApplicationInfo("com.example.app", 0);
                // } catch (PackageManager.NameNotFoundException e) {
                //  e.printStackTrace();
                // }
                // int uid = info.uid;

                try {
                    String date_string;

                    if(datePicked.equals("")) {
                        Calendar calendar = Calendar.getInstance();
                        date_string = dateFormat.format(calendar.getTime());
                    }
                    else{
                        date_string = datePicked;
                    }

                    TextView textView = findViewById(R.id.start_date);
                    textView.setText(date_string);

                    long start = dateFormat.parse(date_string).getTime();
                    long end = System.currentTimeMillis();
                    NetworkStats.Bucket networkStatsByApp =
                            networkStatsManager.querySummaryForDevice(
                                    ConnectivityManager.TYPE_MOBILE,
                                    subscriberID, start, end);

                    if(networkStatsByApp == null){
                        Log.i("Info", "Error");
                        Toast.makeText(MainActivity.this,
                                "NULL Error", Toast.LENGTH_SHORT).show();
                    }else{
                        double total_data_trans  = networkStatsByApp.getRxBytes() + networkStatsByApp.getTxBytes();

                        total_data_trans = total_data_trans/(divisor*1.0);

                        Log.i("Info", "Total: " + total_data_trans);

                        String displayText = String.format(Locale.getDefault(),
                                "%.3f", total_data_trans) + " " + selected_unit;

                        Toast.makeText(MainActivity.this,
                                displayText, Toast.LENGTH_SHORT).show();


                        textView = findViewById(R.id.text_view_network);
                        textView.setText( displayText );
                    }
                } catch (RemoteException | ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,
                            "Remote or Parse Exception", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //When nothing is selected
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(Objects.requireNonNull(getActivity()), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            //Toast.makeText(getContext(),(month+1) + " " + (day) + ", " + year, Toast.LENGTH_SHORT).show();
            datePicked = (month+1) + " " + day + ", " + year;
        }
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

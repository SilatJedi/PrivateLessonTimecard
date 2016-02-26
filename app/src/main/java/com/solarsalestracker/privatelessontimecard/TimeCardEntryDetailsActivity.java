package com.solarsalestracker.privatelessontimecard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

public class TimeCardEntryDetailsActivity extends AppCompatActivity {

    EditText nameEditText;
    RadioButton AMRadioButton, PMRadioButton, showedUpRadioButton, notEligibleRadioButton;
    Spinner monthSpinner, daySpinner, yearSpinner, hourSpinner, minuteSpinner;
    Calendar calendar;
    boolean isNew = true;
    int yearNum;
    int entryID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        isNew= true;
        calendar = Calendar.getInstance();
        yearNum = calendar.get(Calendar.YEAR);

        monthSpinner = (Spinner)findViewById(R.id.monthSpinner);
        ArrayAdapter<CharSequence> monthAdapter =
                ArrayAdapter.createFromResource(this, R.array.month, android.R.layout.simple_spinner_item);
        monthSpinner.setAdapter(monthAdapter);

        daySpinner = (Spinner)findViewById(R.id.daySpinner);
        ArrayAdapter<CharSequence> dayAdapter =
                ArrayAdapter.createFromResource(this, R.array.day, android.R.layout.simple_spinner_item);
        daySpinner.setAdapter(dayAdapter);

        yearSpinner = (Spinner)findViewById(R.id.yearSpinner);
        ArrayList<Integer> years = new ArrayList<>();
        years.add(yearNum - 1);
        years.add(yearNum);
        ArrayAdapter<Integer> yearsAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, years);
        yearSpinner.setAdapter(yearsAdapter);
        yearSpinner.setSelection(1);

        hourSpinner = (Spinner)findViewById(R.id.hourSpinner);
        ArrayAdapter<CharSequence> hourAdapter =
                ArrayAdapter.createFromResource(this, R.array.hour, android.R.layout.simple_spinner_item );
        hourSpinner.setAdapter(hourAdapter);

        minuteSpinner = (Spinner)findViewById(R.id.minuteSpinner);
        ArrayAdapter<CharSequence> minuteAdapter =
                ArrayAdapter.createFromResource(this, R.array.minute, android.R.layout.simple_spinner_item);
        minuteSpinner.setAdapter(minuteAdapter);

        nameEditText = (EditText)findViewById(R.id.nameEditText);

        AMRadioButton = (RadioButton)findViewById(R.id.AMRadioButton);
        PMRadioButton = (RadioButton)findViewById(R.id.PMRadioButton);
        showedUpRadioButton = (RadioButton)findViewById(R.id.showedUpRadioButton);
        notEligibleRadioButton = (RadioButton)findViewById(R.id.notEligibleRadioButton);

        Bundle extras = getIntent().getExtras();

        if (extras != null){
            monthSpinner.setSelection(monthAdapter.getPosition(extras.getString("month")));
            daySpinner.setSelection(dayAdapter.getPosition(extras.getString("day")));
            yearSpinner.setSelection(yearsAdapter.getPosition(Integer.parseInt(extras.getString("year"))));
            hourSpinner.setSelection(hourAdapter.getPosition(extras.getString("hour")));
            minuteSpinner.setSelection(minuteAdapter.getPosition(extras.getString("minute")));
            entryID = extras.getInt("sqlPosition");

            Log.i("entry ID", String.valueOf(entryID));

            if(extras.getString("ampm").equals("AM")) {
                AMRadioButton.setChecked(true);
            } else {
                PMRadioButton.setChecked(true);
            }

            nameEditText.setText(extras.getString("name"));

            if(extras.getString("disposition").equals("Showed Up")){
                showedUpRadioButton.setChecked(true);
            }else{
                notEligibleRadioButton.setChecked(true);
            }

            isNew = false;

        }

    }

    public void addTimeCardEntry(String dateTime, String name, String disposition ){

        if(isNew) {

            MainActivity.timeCardDB.execSQL("INSERT INTO timecard (dateTime, name, disposition) " +
                    "VALUES ('" + dateTime + "', '" + name + "', '" + disposition + "')");

        } else {
            MainActivity.timeCardDB.execSQL("UPDATE timecard SET dateTime = '" + dateTime + "' WHERE id = " + entryID);
            MainActivity.timeCardDB.execSQL("UPDATE timecard SET name = '" + name + "' WHERE id = " + entryID);
            MainActivity.timeCardDB.execSQL("UPDATE timecard SET disposition = '" + disposition + "' WHERE id = " + entryID);
        }

    }

    public void setAMPM(View view) {
        String tag = (String) view.getTag();

        switch (tag){
            case "am" : PMRadioButton.setChecked(false);
                break;
            case "pm" : AMRadioButton.setChecked(false);
                break;
        }
    }

    public void setDisposition(View view) {
        String tag = (String) view.getTag();

        switch (tag){
            case "su" : notEligibleRadioButton.setChecked(false);
                break;
            case "ne" : showedUpRadioButton.setChecked(false);
                break;
        }
    }

    public void saveEntry(View view) {
        boolean isOkToSave = true;
        String AMPMHoldingMeUp = "";
        String dispositionHoldingMeUp = "";

        String month = monthSpinner.getSelectedItem().toString();

        String day = daySpinner.getSelectedItem().toString();

        String year = yearSpinner.getSelectedItem().toString();

        String hour = hourSpinner.getSelectedItem().toString();

        String minute = minuteSpinner.getSelectedItem().toString();

        String AmPm = "";
        if(AMRadioButton.isChecked()){
            AmPm = "AM";
        } else if (PMRadioButton.isChecked()){
            AmPm = "PM";
        } else if (!AMRadioButton.isChecked() &&
                !PMRadioButton.isChecked()) {
            AMPMHoldingMeUp = "Please check \"AM\" or \"PM\"";
            isOkToSave = false;
        }

        String disposition = "";
        if(showedUpRadioButton.isChecked()){
            disposition = "Showed Up";
        } else if (notEligibleRadioButton.isChecked()){
            disposition = "Not Eligible";
        } else if (!showedUpRadioButton.isChecked() &&
                !notEligibleRadioButton.isChecked()) {
            dispositionHoldingMeUp = "Please check \"Showed Up\" or \"Not Eligible\"";
            isOkToSave = false;
        }

        Log.i("timecard entry",month + "/" + day + "/" + year + " " +
                hour + ":" + minute + " " +AmPm +
                nameEditText.getText().toString() +
                disposition);

        if (isOkToSave){
            String dateTime = month + "/" + day + "/" + year + " " + hour + ":" + minute + " " +AmPm ;
            String name = nameEditText.getText().toString();

            addTimeCardEntry(dateTime, name, disposition);
            runOnUiThread(MainActivity.refreshListView);
            finish();
        }else{
            Toast.makeText(getApplicationContext(), AMPMHoldingMeUp + "\n" + dispositionHoldingMeUp, Toast.LENGTH_SHORT).show();
        }
    }


}

package com.solarsalestracker.privatelessontimecard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static SQLiteDatabase timeCardDB;
    static ArrayList<String> timeCardEntries = new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    ListView timeCardListView;
    static Runnable refreshListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timeCardListView = (ListView)findViewById(R.id.timeCardListView);
        refreshListView = new Runnable(){
            public void run(){
                timeCardEntries.clear();
                populateTimeCard();
                arrayAdapter.notifyDataSetChanged();
                timeCardListView.invalidateViews();
                timeCardListView.refreshDrawableState();
            }

        };


        timeCardEntries.clear();

        timeCardDB = this.openOrCreateDatabase("timeCard", MODE_PRIVATE, null);

        timeCardDB.execSQL("CREATE TABLE IF NOT EXISTS timeCard (id INTEGER PRIMARY KEY, " +
                "dateTime VARCHAR, name VARCHAR, disposition VARCHAR)");

        populateTimeCard();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, timeCardEntries);

        timeCardListView.setAdapter(arrayAdapter);

        timeCardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int sqlPosition = position + 1;

                try {

                    Cursor cursor = timeCardDB.rawQuery("SELECT * FROM timeCard", null);

                    int dateTimeIndex = cursor.getColumnIndex("dateTime");
                    int nameIndex = cursor.getColumnIndex("name");
                    int dispositionIndex = cursor.getColumnIndex("disposition");

                    cursor.moveToPosition(sqlPosition);

                    String dateTime = cursor.getString(dateTimeIndex);
                    String name = cursor.getString(nameIndex);
                    String disposition = cursor.getString(dispositionIndex);

                    String month = dateTime.substring(0, 2);
                    String day = dateTime.substring(3, 5);
                    String year = dateTime.substring(6, 10);

                    String hour = dateTime.substring(11, 13);
                    String minute = dateTime.substring(14, 16);
                    String ampm = dateTime.substring(17, 19);

                    Intent i = new Intent(getApplicationContext(), TimeCardEntryDetailsActivity.class);
                    i.putExtra("sqlPosition", sqlPosition);
                    i.putExtra("month", month);
                    i.putExtra("day", day);
                    i.putExtra("year", year);
                    i.putExtra("hour", hour);
                    i.putExtra("minute", minute);
                    i.putExtra("ampm", ampm);
                    i.putExtra("name", name);
                    i.putExtra("disposition", disposition);
                    startActivity(i);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });

        timeCardListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {

                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this timecard entry?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                int end = timeCardListView.getCount();
                                Log.i("NumS end", String.valueOf(end));
                                int sqlPosition = position + 1;
                                Log.i("NumS position", String.valueOf(sqlPosition));

                                timeCardDB.execSQL("DELETE FROM timeCard WHERE id = " + sqlPosition);

                                runOnUiThread(refreshListView);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                return true;
            }
        });

    }


    public void sendMail(String[] mailTo, String subject, String body, String attachmentFilePath)
    {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        emailIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);

        emailIntent.setType("plain/text");


        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, mailTo);

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(body));

        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + attachmentFilePath));

        getApplicationContext().startActivity(emailIntent);
    }

    public void populateTimeCard() {

        try {


            Cursor cursor = timeCardDB.rawQuery("SELECT * FROM timeCard", null);

            int idIndex = cursor.getColumnIndex("id");
            int dateTimeIndex = cursor.getColumnIndex("dateTime");
            int nameIndex = cursor.getColumnIndex("name");
            int dispositionIndex = cursor.getColumnIndex("disposition");

            cursor.moveToFirst();

            while(cursor != null) {

                int index = timeCardEntries.size() + 1;
                Log.i("dude idIndex", String.valueOf(idIndex));
                timeCardEntries.add(idIndex + ": " + cursor.getString(dateTimeIndex) + "\n" + cursor.getString(nameIndex) + " " + cursor.getString(dispositionIndex));
                cursor.moveToNext();
            }

        }
        catch (Exception e) {

            e.printStackTrace();
        }

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

        if (id == R.id.add) {

            Intent i = new Intent(MainActivity.this, TimeCardEntryDetailsActivity.class);
            startActivity(i);

            return true;
        }

        if (id == R.id.submit) {

            new AlertDialog.Builder(MainActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Turn In Timecard")
                    .setMessage("Are you ready to send off your timecard?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String h = DateFormat.format("MM-dd-yyyyy h-mm aa", System.currentTimeMillis()).toString();
                                // this will create a new name everytime and unique
                                File root = new File(Environment.getExternalStorageDirectory(), "Timecards");
                                // if external memory exists and folder with name Notes
                                if (!root.exists()) {
                                    root.mkdirs(); // this will create folder.
                                }
                                File filepath = new File(root, "timecard" + h + ".txt");  // file path to save
                                FileWriter writer = new FileWriter(filepath);



                                for(int i = 0; i < timeCardEntries.size(); i++) {
                                    writer.append(timeCardEntries.get(i) + "\n\n");
                                }

                                writer.flush();
                                writer.close();

                                String[] mailto = {"mpusa1@hotmail.com"};

                                sendMail(mailto, "Timecard Report", "Here is my timecard", filepath.toString());

                                timeCardDB.execSQL("DELETE FROM timeCard");
                                runOnUiThread(refreshListView);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return true;
        }


        if (id == R.id.deleteAll) {

            new AlertDialog.Builder(MainActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Delete Everything")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            timeCardDB.execSQL("DELETE FROM timeCard");
                            runOnUiThread(refreshListView);

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

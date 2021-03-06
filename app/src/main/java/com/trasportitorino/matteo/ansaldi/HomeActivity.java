package com.trasportitorino.matteo.ansaldi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageButton btt_search;
    private EditText txt_busStop;

    private Context context;
    private List<String> lsv_result;
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;

    private ListView lsv_savedStops;
    private ArrayAdapter<String> mAdapter;
    private DrawerLayout busStopLayout;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private SharedPreferences sharedPref;

    private GttBusStop busStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        context = this;
        lsv_result = new ArrayList<String>();

        btt_search = (ImageButton)findViewById(R.id.btt_search);
        txt_busStop = (EditText)findViewById(R.id.txt_stopNum);
        busStopLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        //create the result list
        adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,lsv_result);
        ListView listView = (ListView) findViewById(R.id.lsv_stopView);
        listView.setAdapter(adapter);

        busStop = new GttBusStop();

        //create the side drawer
        lsv_savedStops = (ListView)findViewById(R.id.lsv_savedStops);
        addDrawerItems();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        setupDrawer();



        //act on button presses in the side drawer
        lsv_savedStops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //parse the value read from the list(477 - S. Ottavio --> 477)
                String text = mAdapter.getItem(position);
                String stopNumToSearch = text.split(" - ")[0];
                txt_busStop.setText(stopNumToSearch);
                btt_search.performClick();
                busStopLayout.closeDrawers();
            }
        });


        //act on search button pressed
        btt_search.setOnClickListener(this);

        //act on enter pressed
        txt_busStop.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    btt_search.performClick();
                    return true;
                }
                return false;
            }
        });



    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    //act on search button pressed, fill the search list.
    @Override
    public void onClick(View v) {

        Editable stopNumRead;
        int stopNumReadInt;
        ArrayList<BusInfo> busInfos = null;
        stopNumRead = txt_busStop.getText();

        if(stopNumRead.length() > 0) {
            lsv_result.clear();

            RequestTimetable timetable = new RequestTimetable();
            if (!stopNumRead.equals("")) {
                timetable.execute(new RequestTimetableParams(stopNumRead.toString(), busStop));
            }
        }
    }


    //populate the side drawer
    private void addDrawerItems() {
        ArrayList<String> savedStops = new ArrayList<>();
        //savedStops.addAll(sharedPref.getAll().keySet());
        for(String stopNum : sharedPref.getAll().keySet()){
            savedStops.add(stopNum + " - " + sharedPref.getAll().get(stopNum));
        }
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedStops);
        lsv_savedStops.setAdapter(mAdapter);
    }

    //get the bus stop information from gtt website
    class RequestTimetable extends AsyncTask<RequestTimetableParams, Void, ArrayList<BusInfo>> {




        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            progressDialog = new ProgressDialog(HomeActivity.this);
            progressDialog.setMessage("Getting stop info, please wait.");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected ArrayList<BusInfo> doInBackground(RequestTimetableParams... params) {


            ArrayList<BusInfo> busInfos;
            GttBusStop busStop = params[0].getBusStop();
            int stopNumReadInt = Integer.parseInt(params[0].getStopNumRead());

            try {

                busStop.updateBusInfo(stopNumReadInt);

            } catch (Exception e) {
                e.printStackTrace();
            }

            busInfos = busStop.getStopInfo();
            return busInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<BusInfo> busInfos) {


            for (int i = 0; i < busInfos.size(); i++) {

                String listInfo = "";
                listInfo = listInfo.concat("Linea: " + busInfos.get(i).getBusNum());
                listInfo = listInfo.concat("\nOra: " + busInfos.get(i).getTime());
                listInfo = listInfo.concat("\n" + busInfos.get(i).getPassaggio());

                lsv_result.add(listInfo);

                adapter.notifyDataSetChanged();

            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

        }
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Saved stops");
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }



    //Create the 3 dot menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.save_stop:
                //TODO: code to add extra stop

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.add_stop_layout, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText txt_stopNumToSave = (EditText)promptsView.findViewById(R.id.stopNumberA);
                final EditText txt_stopNameToSave = (EditText)promptsView.findViewById(R.id.stopNameA);
                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // save the new stop number and stop name
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString(txt_stopNumToSave.getText().toString(), txt_stopNameToSave.getText().toString());
                                        editor.commit();
                                        addDrawerItems();

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });


                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;

            case R.id.delete_stop:

                // get prompts.xml view
                LayoutInflater li_delete = LayoutInflater.from(this);
                View promptsView_delete = li_delete.inflate(R.layout.delete_stop_layout, null);

                AlertDialog.Builder alertDialogBuilder_delete = new AlertDialog.Builder(this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder_delete.setView(promptsView_delete);

                final EditText txt_stopNumToDelete = (EditText)promptsView_delete.findViewById(R.id.stopNumberD);
                // set dialog message
                alertDialogBuilder_delete
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // delete stop number and name
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.remove(txt_stopNumToDelete.getText().toString());
                                        editor.commit();
                                        addDrawerItems();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });


                // create alert dialog
                AlertDialog alertDialog_delete = alertDialogBuilder_delete.create();

                // show it
                alertDialog_delete.show();
                return true;

            default:
                // Activate the navigation drawer toggle
                if (mDrawerToggle.onOptionsItemSelected(item)) {
                    return true;
                }
                return super.onOptionsItemSelected(item);

        }




    }



}





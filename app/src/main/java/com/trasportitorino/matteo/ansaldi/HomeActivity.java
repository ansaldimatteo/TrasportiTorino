package com.trasportitorino.matteo.ansaldi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
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



    private GttBusStop busStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                txt_busStop.setText(mAdapter.getItem(position));
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


    //TODO: READ STOP INFO FROM EXTERNAL FILE
    //populate the side drawer
    private void addDrawerItems() {
        String[] savedStopsArray = { "477","624" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedStopsArray);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}





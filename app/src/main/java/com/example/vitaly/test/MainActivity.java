package com.example.vitaly.test;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends ActionBarActivity{

    private EventsDBAdapter mDB;
    private EventsExpandableListAdapter expListAdapter;
    private ExpandableListView expListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //-- changing locale to en -----------------------------
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        //------------------------------------------------------

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mDB = new EventsDBAdapter (this);
        mDB.open();

        expListView = (ExpandableListView) findViewById(R.id.expandableListView);
        // placing the indicator to the right
        {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                expListView.setIndicatorBounds(width - (int)convertDpToPixel(80), width - (int)convertDpToPixel(30));
            } else {
                expListView.setIndicatorBoundsRelative(width - (int)convertDpToPixel(80), width - (int)convertDpToPixel(30));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        expListAdapter = new EventsExpandableListAdapter(this, mDB);
        expListView.setAdapter(expListAdapter);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public float convertDpToPixel(float dp){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    @Override
    protected void onDestroy() {
        mDB.close();

        super.onDestroy();
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

        return super.onOptionsItemSelected(item);
    }

    public void onClickAddEventButton(View view) {
        Intent intent = new Intent(this, AddEventActivity.class);
        startActivity(intent);
    }
}

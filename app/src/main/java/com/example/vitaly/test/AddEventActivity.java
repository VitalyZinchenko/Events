package com.example.vitaly.test;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Toast;

import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddEventActivity extends ActionBarActivity {

    private ScrollView scrollView;

    private EditText categoryEditText;
    private EditText eventEditText;

    private TwoWayGridView categoriesGridView;
    private TwoWayGridView eventsGridView;

    private DatePicker datePicker;
    private NumberPicker numberPicker;

    private Button doneButton;

    private EventsDBAdapter mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_event_activity);

        mDb = new EventsDBAdapter (this);
        mDb.open();

        scrollView = (ScrollView) findViewById(R.id.add_event_scrollView);

        categoryEditText = (EditText) findViewById(R.id.input_category_editText);
        eventEditText = (EditText) findViewById(R.id.input_event_editText);

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        //-- setting numberPicker ---------------------------
            String[] nums = new String[10];
            for(int i=0; i<nums.length; i++)
                nums[i] = Integer.toString(i+1);

            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(10);
            numberPicker.setWrapSelectorWheel(false);
            numberPicker.setDisplayedValues(nums);
            numberPicker.setValue(1);
        //---------------------------------------------------

        doneButton = (Button) findViewById(R.id.done_button);

        categoriesGridView = (TwoWayGridView) findViewById(R.id.categories_gridView);
        eventsGridView = (TwoWayGridView) findViewById(R.id.events_gridView);

        handleCategoriesGridView();
    }

    private void handleCategoriesGridView()
    {
        categoriesGridView.setScrollDirectionPortrait(1); // 1 - Horizontal
        categoriesGridView.setNumRows(1);
        categoriesGridView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        categoriesGridView.setHorizontalSpacing(10);

        List<Pair<Category, Integer>> sortedCategoriesList =  mDb.retrieveAllCategoriesSortedByPopularity();
        String[] categoriesNames = new String[sortedCategoriesList.size()];
        int counter = 0;
        for (Pair<Category, Integer> categoryAndPopularityPair : sortedCategoriesList)
        {
            Category category = categoryAndPopularityPair.getKey();
            categoriesNames[counter] = category.getName();
            counter++;
        }

        CustomGridAdapter adapter = new CustomGridAdapter(this, categoriesNames);
        categoriesGridView.setAdapter(adapter);
        categoriesGridView.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {

                String categoryName = (String) parent.getAdapter().getItem(position);
                categoryEditText.setText(categoryName);
                categoryEditText.requestFocus();
                categoryEditText.setSelection(categoryEditText.getText().length());

                eventEditText.setText("");

                long categoryId = mDb.retrieveCategoryId(new Category(categoryName));

                handleEventsGridView(categoryId);
            }
        });
    }

    private void handleEventsGridView(long categoryId)
    {
        eventsGridView.setScrollDirectionPortrait(1); // 1 - Horizontal
        eventsGridView.setNumRows(1);
        eventsGridView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        eventsGridView.setHorizontalSpacing(10);

        List<Pair<Event, Integer>> sortedEventsList =  mDb.retrieveEventsRelatedToCategorySortedByPopularity(categoryId);
        String[] eventsNames = new String[sortedEventsList.size()];
        int counter = 0;
        for (Pair<Event, Integer> eventAndPopularityPair : sortedEventsList)
        {
            Event event = eventAndPopularityPair.getKey();
            eventsNames[counter] = event.getName();
            counter++;
        }

        CustomGridAdapter adapter = new CustomGridAdapter(this, eventsNames);
        eventsGridView.setAdapter(adapter);
        eventsGridView.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {

                String eventName = (String) parent.getAdapter().getItem(position);
                eventEditText.setText(eventName);
                eventEditText.requestFocus();
                eventEditText.setSelection(eventEditText.getText().length());
            }
        });
    }

    @Override
    protected void onDestroy() {
        mDb.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
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

    public void onClickDoneButton(View view) {

        String categoryName = categoryEditText.getText().toString();
        if (categoryName.matches("")) {
            Toast.makeText(this, "You did not enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventName = eventEditText.getText().toString();
        if (eventName.matches("")) {
            Toast.makeText(this, "You did not enter an event name", Toast.LENGTH_SHORT).show();
            return;
        }

        // getting category's id
            Category category = new Category(categoryName);
            long categoryId = mDb.retrieveCategoryId(category);
            if (categoryId < 0)
            {
                categoryId = mDb.insertCategory(categoryName);
            }
            category.setId(categoryId);

        // getting event's id
            Event event = new Event(category, eventName);
            long eventId = mDb.retrieveEventId(event);
            if (eventId < 0)
                {
                eventId = mDb.insertEvent(categoryId, eventName);
            }
            event.setId(eventId);

        String monthStr = String.format("%02d", datePicker.getMonth()+1);
        String dayStr = String.format("%02d", datePicker.getDayOfMonth());
        String date = "" + datePicker.getYear() + "-" + monthStr + "-" + dayStr;

        int numberOfTimes = numberPicker.getValue();

        long eventOnDateId = mDb.insertEventOnDate(date, eventId, numberOfTimes);

        if (eventOnDateId > 0)
        {
            EventOnDate addedEvent = mDb.retrieveEventOnDate(eventOnDateId);
            Toast.makeText(this, "Event \"" + addedEvent.getEvent().getName() + "\" was successfully added",
                    Toast.LENGTH_SHORT).show();
        }

        categoryEditText.setText("");
        eventEditText.setText("");

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        datePicker.updateDate(mYear, mMonth, mDay);

        numberPicker.setValue(1);

        scrollView.fullScroll(ScrollView.FOCUS_UP);
        categoryEditText.requestFocus();

        handleCategoriesGridView();
    }
}

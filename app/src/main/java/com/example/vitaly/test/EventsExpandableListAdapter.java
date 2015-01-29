package com.example.vitaly.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EventsExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private EventsDBAdapter mDB;

    private List<Pair<String, Integer>> datesToNumberOfEventsList;

    // this map at start is empty
    // its content is filled when user taps on a date in ExpandableListView
    // on this action List containing all eventOnDate-objects is added to map
    // to get it use the date as a key
        HashMap<String, List<EventOnDate>> datesToEventsOnThatDayMap;

    public EventsExpandableListAdapter(Context context, EventsDBAdapter DBAdapter) {
        this.context = context;
        this.mDB = DBAdapter;

        datesToNumberOfEventsList = this.mDB.retrieveSortedDatesToNumberOfEventsMap();
        datesToEventsOnThatDayMap = new HashMap<String, List<EventOnDate>>();
    }

    @Override
    public int getGroupCount() {
        return datesToNumberOfEventsList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Pair<String, Integer> dateToNumberOfEvents = datesToNumberOfEventsList.get(groupPosition);
        return dateToNumberOfEvents.getValue();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return datesToNumberOfEventsList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_view, null);
        }

        TextView dateTextView = (TextView) convertView.findViewById(R.id.date_group_textView);
        TextView numberOfEventsTextView = (TextView) convertView.findViewById(R.id.numberOfEvents_group_textView);

        String dateString = datesToNumberOfEventsList.get(groupPosition).getKey();
        //-- reformatting date -----------------------------------------------------
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateFormat = new SimpleDateFormat("yyyy MMMM dd", Locale.US);
            dateString = dateFormat.format(date);
        //--------------------------------------------------------------------------

        Integer numberOfEvents = datesToNumberOfEventsList.get(groupPosition).getValue();

        dateTextView.setText("Date: " + dateString);
        numberOfEventsTextView.setText("Number of events: " + String.valueOf(numberOfEvents));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_view, null);
        }

        String date = datesToNumberOfEventsList.get(groupPosition).getKey();


        List<EventOnDate> eventsOnThisDate = datesToEventsOnThatDayMap.get(date);
        if (eventsOnThisDate == null)
        {
            eventsOnThisDate = mDB.retrieveEventsOnParticularDate(date);
            datesToEventsOnThatDayMap.put(date, eventsOnThisDate);
        }

        Event event = eventsOnThisDate.get(childPosition).getEvent();
        String eventDescription = event.getCategory().getName()+ ": " + event.getName();
        int numberOfTimes = eventsOnThisDate.get(childPosition).getNumberOfTimes();
        if (numberOfTimes > 1)
        {
            eventDescription = eventDescription + " (" + numberOfTimes + " times)";
        }

        TextView eventDescriptionTextView = (TextView) convertView.findViewById(R.id.eventDescription_child_textView);
        eventDescriptionTextView.setText(eventDescription);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

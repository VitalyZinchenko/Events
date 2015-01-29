package com.example.vitaly.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomGridAdapter extends BaseAdapter {

    private Context context;
    private String[] stringsList;

    CustomGridAdapter(Context context, String[] stringsList){
        this.context = context;
        this.stringsList = stringsList;
    }

    @Override
    public int getCount() {
        return stringsList.length;
    }

    @Override
    public Object getItem(int position) {
        return stringsList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item_view, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.grid_item_textView);

        textView.setText(stringsList[position]);

        return convertView;
    }
}

package com.example.appusetracker2;

import android.app.ActivityManager;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<RunningAppProcessInfo>
{
    private final Context context;
    private final List<RunningAppProcessInfo> values;

    public ListAdapter (Context context, List <RunningAppProcessInfo> values){
        super(context, R.layout.activity_process_list, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView= inflater.inflate(R.layout.activity_main, parent, false);
        //TextView appName= (TextView) rowView.findViewById(R.id.this);
        //appName.setText(values.get(position).processName);
        return rowView;
    }
}
package com.openforce.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.openforce.R;

import java.util.List;

public class SkillLevelAdapter extends ArrayAdapter<String> {

    private List<String> objects;
    private Context context;

    public SkillLevelAdapter(Context context, int resourceId,
                              List<String> objects) {
        super(context, resourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_item_spinner, parent, false);
        final TextView label= row.findViewById(R.id.spinner_text);
        label.setText(objects.get(position));
        return row;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(final int position, View convertView, ViewGroup parent) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_spinner, parent, false);
        final TextView label = row.findViewById(R.id.spinner_text);
        label.setText(objects.get(position));
        return row;
    }

}

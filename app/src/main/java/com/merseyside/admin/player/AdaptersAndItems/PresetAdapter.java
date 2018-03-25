package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.merseyside.admin.player.R;
import java.util.ArrayList;

/**
 * Created by Admin on 24.01.2017.
 */

public class PresetAdapter extends ArrayAdapter<String>{

    private Context c;
    private int id;
    private ArrayList<String> items;

    public PresetAdapter(Context context, int resource, ArrayList<String>list) {
        super(context, resource, list);
        c = context;
        id = resource;
        items = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, parent, false);
        }
        String preset = items.get(position);

        final TextView tv = (TextView) v.findViewById(R.id.preset_tv);
        tv.setText(preset);
        return v;
    }
}

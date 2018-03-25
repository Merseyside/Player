package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.merseyside.admin.player.ActivitesAndFragments.FileChooserActivity;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.ArrayList;

public class FileArrayAdapter extends ArrayAdapter<Item> {

    private Context c;
    private int id;
    private ArrayList<Item> items;
    private Settings settings;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            ArrayList<Item> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
        settings = new Settings(c);
    }
    public Item getItem(int i)
    {
        return items.get(i);
    }

    public boolean changeSelected(int i) {
        if(items.get(i).getSelected())
        {
            items.get(i).setSelected(false);
            return false;
        }
        else
        {
            items.get(i).setSelected(true);
            return true;
        }
    }

    public void setSelected(int i)
    {
        items.get(i).setSelected(true);
    }


    public void setNoSelected(int i)
    {
        items.get(i).setSelected(false);
    }

    public void checkAll()
    {
        for(int i = 0; i<items.size(); i++)
        {
            setSelected(i);
        }
    }

    public void misCheckAll()
    {
        for(int i = 0; i<items.size(); i++)
        {
            setNoSelected(i);
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, parent, false);
        }

               /* create a new view of my layout and inflate it in the row */
        //convertView = ( RelativeLayout ) inflater.inflate( resource, null );

        final Item o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.FAA_TextView01);
            TextView t2 = (TextView) v.findViewById(R.id.FAA_TextView02);
            TextView t3 = (TextView) v.findViewById(R.id.FAA_TextViewDate);
                       /* Take the ImageView from layout and set the city's image */
            ImageView imageCity = (ImageView) v.findViewById(R.id.FAA_fd_Icon1);
            CheckBox cb = (CheckBox) v.findViewById(R.id.FAA_checkBox);

            if (FileChooserActivity.MULTI_CHOOSE_ENABLE) {
                imageCity.setVisibility(View.GONE);
                cb.setVisibility(View.VISIBLE);
                cb.setChecked(o.getSelected());
            }
            else
            {
                imageCity.setVisibility(View.VISIBLE);
                cb.setVisibility(View.GONE);
                imageCity.setImageResource(o.getImage());
            }

            if(t1!=null)
                t1.setText(o.getName());
            if(t2!=null)
                t2.setText(o.getData());
            if(t3!=null)
                t3.setText(o.getDate());
        }

        return v;
    }

    public ArrayList<Item> getAll(){
        return items;
    }
}

package com.merseyside.admin.player.Utilities;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.merseyside.admin.player.ActivitesAndFragments.MainActivity;
import com.merseyside.admin.player.ActivitesAndFragments.Player_Fragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 01.12.2016.
 */

public class ParentFragment extends Fragment {

    protected Player_Fragment player_fragment;
    protected Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
    }

    public void putArguments(HashMap<String, String> data){
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            bundle.putString(key, value);
        }
        this.setArguments(bundle);
        data.clear();
    }

    public ParentFragment(){
        player_fragment = MainActivity.get_player_fragment();
    }

}

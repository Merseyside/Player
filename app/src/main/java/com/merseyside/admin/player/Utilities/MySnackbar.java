package com.merseyside.admin.player.Utilities;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import com.merseyside.admin.player.R;

/**
 * Created by Admin on 07.01.2017.
 */

public class MySnackbar {
    private int string, background_color, text_color;
    private View view;
    private Context context;
    private boolean length;
    private Settings settings;

    public MySnackbar(Context context, View view, int string){
        this.settings = new Settings(context);
        this.string = string;
        this.view = view;
        this.background_color =  settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_accent_color);
        this.text_color = R.color.white;
        this.context = context;
        this.length = false;

    }

    public MySnackbar(Context context, View view, int string, boolean length){
        this.settings = new Settings(context);
        this.string = string;
        this.view = view;
        this.background_color =  settings.getAttributeId(settings.getThemeByString(), R.attr.theme_dependent_accent_color);;
        this.context = context;
        this.text_color = R.color.white;
        this.length = length;
    }

    public void show(){
        Snackbar snackbar;
        snackbar = Snackbar.make(view, context.getResources().getString(string), Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(background_color);
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(settings.getColor(text_color));
        if (length) snackbar.setDuration(Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}

package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.ArrayList;

/**
 * Created by Admin on 10.03.2017.
 */

public class InfoDialog extends Dialog {
    private String title;
    private ArrayList<String> info;
    private CheckBox checkBox;
    private Settings settings;
    private boolean isShowCheckBox;
    private String checkboxTitle;
    private Context context;
    private InfoDialogListener infoDialogListener;

    private TextView title_tw, info_tw;

    public interface InfoDialogListener{
        void checkboxClicked(boolean isChecked);
    }

    public void setInfoDialogListener(InfoDialogListener infoDialogListener){
        if (infoDialogListener != null) this.infoDialogListener = infoDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.info_dialog);
        settings = new Settings(getContext());
        title_tw = (TextView) findViewById(R.id.title);
        info_tw = (TextView) findViewById(R.id.info);
        checkBox = (CheckBox) findViewById(R.id.show_again_cb);
        if (!isShowCheckBox) checkBox.setVisibility(View.GONE);
        else {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    infoDialogListener.checkboxClicked(isChecked);
                }
            });
        }

        title_tw.setText(title);
        String full = "";
        for (String str : info){
            full += str + "\n\n";
        }
        info_tw.setText(full);
        if (checkboxTitle!=null && checkboxTitle.length()!=0) checkBox.setText(checkboxTitle);

    }

    public InfoDialog(Context context, String title, ArrayList<String> info, boolean isShowCheckBox) {
        super(context);
        this.title = title;
        this.info = info;
        this.isShowCheckBox = isShowCheckBox;
    }

    public InfoDialog(Context context, String title, ArrayList<String> info, String checkboxTitle) {
        super(context);
        this.context = context;
        this.title = title;
        this.info = info;
        this.isShowCheckBox = true;
        this.checkboxTitle = checkboxTitle;
    }

    public InfoDialog(Context context, String title, ArrayList<String> info) {
        super(context);
        this.title = title;
        this.info = info;
        this.isShowCheckBox = false;
    }
}

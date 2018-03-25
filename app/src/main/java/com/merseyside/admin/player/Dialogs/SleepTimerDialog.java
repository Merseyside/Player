package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 27.05.2017.
 */

public class SleepTimerDialog extends Dialog implements View.OnClickListener {

    private SeekBar seekBar;
    private TextView text;
    private Button positive, negative;
    private SleepTimerDialogListener sleepTimerDialogListener;
    private boolean isTimer;
    private Context context;

    public interface SleepTimerDialogListener{
        void setTimerClicked(int minutes);
        void negativeButtonClicked();
        void stopTimerClicked();
    }

    public SleepTimerDialog(@NonNull Context context, boolean isTimer) {
        super(context);
        this.context = context;
        this.isTimer = isTimer;
    }

    public void setSleepTimerDialogListener(SleepTimerDialogListener sleepTimerDialogListener){
        this.sleepTimerDialogListener = sleepTimerDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.sleep_timer_dialog);

        seekBar = (SeekBar) findViewById(R.id.sleep_timer_sb);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setText(i+1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        text = (TextView) findViewById(R.id.sleep_timer_tw);
        positive = (Button) findViewById(R.id.ok);
        positive.setOnClickListener(this);
        negative = (Button) findViewById(R.id.cancel);
        negative.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        if (!isTimer){
            setText(1);
        } else {
            seekBar.setVisibility(View.GONE);
            text.setText(context.getString(R.string.timer_already_running));
            positive.setText(context.getString(R.string.stop_timer));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ok:
                if (isTimer) sleepTimerDialogListener.stopTimerClicked();
                else sleepTimerDialogListener.setTimerClicked(seekBar.getProgress()+1);
                dismiss();
                break;

            case R.id.cancel:
                sleepTimerDialogListener.negativeButtonClicked();
                dismiss();
                break;
        }
    }

    private void setText(int minutes){
        String str = context.getResources().getString(R.string.start_timer) + " " + String.valueOf(minutes) + " " + context.getString(R.string.minutes);
        text.setText(str);
    }
}

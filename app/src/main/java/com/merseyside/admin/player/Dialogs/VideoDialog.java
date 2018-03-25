package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 25.05.2017.
 */

public class VideoDialog extends Dialog implements View.OnClickListener{

    private VideoDialogListener videoDialogListener;
    private Bitmap image;
    private ImageView image_view;
    private Button cancel;
    private String title;
    private TextView title_tw;

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.video_image:
                videoDialogListener.userClickedVideo();
                dismiss();
                break;

            case R.id.video_cancel:
                dismiss();
                videoDialogListener.userClickedCancel();
                break;
        }
    }

    public interface VideoDialogListener{
        void userClickedVideo();
        void userClickedCancel();
    }

    public VideoDialog(@NonNull Context context, Bitmap image, String title, VideoDialogListener videoDialogListener) {
        super(context);
        this.videoDialogListener = videoDialogListener;
        this.image = image;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.video_dialog);

        image_view = (ImageView) findViewById(R.id.video_image);
        image_view.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.video_cancel);
        cancel.setOnClickListener(this);
        title_tw = (TextView) findViewById(R.id.video_title);

    }

    @Override
    protected void onStart() {
        super.onStart();
        image_view.setImageBitmap(image);
        title_tw.setText(title);
    }
}

package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 21.02.2017.
 */

public class CommentDialog extends Dialog implements View.OnClickListener {

    private Button save;
    private EditText editName;
    private CommentDialogListener commentDialogListener;
    private String comment;


    public interface CommentDialogListener {
        void commentSaved(String comment);
    }


    public CommentDialog(Context context, String comment, CommentDialogListener commentDialogListener) {
        super(context);
        this.commentDialogListener = commentDialogListener;
        if (comment.equals("null")) this.comment = "";
        else this.comment = comment;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.comment_dialog);
        save = (Button) findViewById(R.id.save_comment_button);
        save.setOnClickListener(this);
        editName = (EditText) findViewById(R.id.comment_edittext);
        editName.setText(this.comment);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_comment_button:
                commentDialogListener.commentSaved(editName.getText().toString());
                dismiss();
        }
    }
}

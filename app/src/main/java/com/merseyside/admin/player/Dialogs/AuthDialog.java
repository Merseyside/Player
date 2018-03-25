package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.merseyside.admin.player.R;

/**
 * Created by Admin on 14.03.2017.
 */

public class AuthDialog extends Dialog implements View.OnClickListener {

    public interface AuthDialogListener{
        void userPressedLogIn(String user, String password, boolean remember);
        void userPressedCancel(String user, String password);
        void userPressedSignIn();
    }
    private EditText user, password;
    private CheckBox remember;
    private Button login, cancel, sign_in;
    private AuthDialogListener authDialogListener;
    private String user_str, password_str;

    public AuthDialog(Context context) {
        super(context);
    }

    public void setUserAndPassword(String user, String password){
        user_str = user;
        password_str = password;
    }

    public void setAuthDialogListener(AuthDialogListener authDialogListener){
        this.authDialogListener = authDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.auth_dialog);

        user = (EditText) findViewById(R.id.user_edittext);
        password = (EditText) findViewById(R.id.password_edittext);
        remember = (CheckBox) findViewById(R.id.remember_lastfm);
        login = (Button) findViewById(R.id.log_in_button);
        login.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(this);
        sign_in = (Button) findViewById(R.id.sign_in);
        sign_in.setOnClickListener(this);

        password.setText(password_str);
        user.setText(user_str);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.log_in_button:
                authDialogListener.userPressedLogIn(user.getText().toString(), password.getText().toString(), remember.isChecked());
                dismiss();
                break;
            case R.id.cancel_button:
                authDialogListener.userPressedCancel(user.getText().toString(), password.getText().toString());
                dismiss();
                break;
            case R.id.sign_in:
                authDialogListener.userPressedSignIn();
                break;
        }
    }
}

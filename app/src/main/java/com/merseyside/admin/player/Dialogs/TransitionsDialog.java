package com.merseyside.admin.player.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.merseyside.admin.player.AdaptersAndItems.TransitionCreator;
import com.merseyside.admin.player.AdaptersAndItems.TransitionsAdapter;
import com.merseyside.admin.player.R;

/**
 * Created by Admin on 24.05.2017.
 */

public class TransitionsDialog extends Dialog implements View.OnClickListener{

    private Context context;
    private ListView listView;
    private MyTransitionDialogListener myTransitionDialogListener;
    private TransitionsAdapter transitionsAdapter;
    private Button cancel;

    public interface MyTransitionDialogListener{
        void userSelectedTransition(int transition);
        void userDoNotChooseAnything();
    }

    public TransitionsDialog(@NonNull Context context, MyTransitionDialogListener myTransitionDialogListener) {
        super(context);
        this.context = context;
        this.myTransitionDialogListener = myTransitionDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.transition_dialog);

        cancel = (Button) findViewById(R.id.transition_cancel);
        cancel.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.transition_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                myTransitionDialogListener.userSelectedTransition(i+1);
                transitionsAdapter.stopPlayer();
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        transitionsAdapter = new TransitionsAdapter(context, R.layout.transition_view, TransitionCreator.getAllTransitions(context));
        listView.setAdapter(transitionsAdapter);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.transition_cancel:
                myTransitionDialogListener.userDoNotChooseAnything();
                transitionsAdapter.stopPlayer();
                dismiss();
                break;
        }
    }
}

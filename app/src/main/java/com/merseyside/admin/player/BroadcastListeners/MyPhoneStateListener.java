package com.merseyside.admin.player.BroadcastListeners;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by Admin on 19.12.2016.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    public interface MyCallingListener{
        void callingStart();
        void callingEnd();
    }

    private MyCallingListener myCallingListener;

    public void setCallingListener(MyCallingListener myCallingListener){
        this.myCallingListener = myCallingListener;
    }

    public void onCallStateChanged(int state, String incomingNumber)
    {
        super.onCallStateChanged(state, incomingNumber);
        switch (state)
        {
            case TelephonyManager.CALL_STATE_IDLE:
                myCallingListener.callingEnd();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                myCallingListener.callingStart();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                myCallingListener.callingStart();
                break;
            default:
                break;
        }
    }
}
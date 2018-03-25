package com.merseyside.admin.player.AdaptersAndItems;

import android.content.Context;

import com.merseyside.admin.player.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Admin on 17.05.2017.
 */

public class TransitionCreator {
    public static final int TRANSITION_1 = 1;
    public static final int TRANSITION_2 = 2;
    public static final int TRANSITION_3 = 3;
    public static final int TRANSITION_4 = 4;
    public static final int TRANSITION_5 = 5;
    public static final int TRANSITION_6 = 6;
    public static final int RANDOM_TRANSITION = 100;

    private static final int TRANSITION_5_DURATION = 16901;
    private static final int TRANSITION_2_DURATION = 13740;
    private static final int TRANSITION_3_DURATION = 20636;
    private static final int TRANSITION_4_DURATION = 5172;
    private static final int TRANSITION_1_DURATION = 8046;
    private static final int TRANSITION_6_DURATION = 6922;

    private static final int TRANSITION_5_TURNING_POINT = 14000;
    private static final int TRANSITION_2_TURNING_POINT = 11700;
    private static final int TRANSITION_3_TURNING_POINT = 13500;
    private static final int TRANSITION_4_TURNING_POINT = 3300;
    private static final int TRANSITION_1_TURNING_POINT = 3700;
    private static final int TRANSITION_6_TURNING_POINT = 5000;

    public static Transition CreateTransition(int transition, Context context) throws IllegalArgumentException{
        String path = "asset:///";
        String[] list = context.getResources().getStringArray(R.array.megamix_transition_entries);
        switch (transition){
            case (TRANSITION_1):
                return new Transition(list[transition], path + "transition_1.mp3", TRANSITION_1_DURATION, TRANSITION_1_TURNING_POINT, false, false);
            case (TRANSITION_2):
                return new Transition(list[transition], path + "transition_2.mp3", TRANSITION_2_DURATION, TRANSITION_2_TURNING_POINT, false, false);
            case (TRANSITION_3):
                return new Transition(list[transition], path + "transition_3.mp3", TRANSITION_3_DURATION, TRANSITION_3_TURNING_POINT, false, false);
            case (TRANSITION_4):
                return new Transition(list[transition], path + "transition_4.mp3", TRANSITION_4_DURATION, TRANSITION_4_TURNING_POINT, false, false);
            case (TRANSITION_5):
                return new Transition(list[transition], path + "transition_5.mp3", TRANSITION_5_DURATION, TRANSITION_5_TURNING_POINT, true, true);
            case (TRANSITION_6):
                return new Transition(list[transition], path + "transition_6.mp3", TRANSITION_6_DURATION, TRANSITION_6_TURNING_POINT, true, true);
            case (RANDOM_TRANSITION):
                Random random = new Random();
                return CreateTransition(random.nextInt(6)+1, context);
            default:
                throw new IllegalArgumentException();
        }
    }

    public static ArrayList<Transition> getAllTransitions(Context context){
        ArrayList<Transition> transitions = new ArrayList<>();
        String path = "asset:///";
        String[] list = context.getResources().getStringArray(R.array.megamix_transition_entries);
        transitions.add(new Transition(list[TRANSITION_1], path + "transition_1.mp3", TRANSITION_1_DURATION, TRANSITION_1_TURNING_POINT, false, false));
        transitions.add(new Transition(list[TRANSITION_2], path + "transition_2.mp3", TRANSITION_2_DURATION, TRANSITION_2_TURNING_POINT, false, false));
        transitions.add(new Transition(list[TRANSITION_3], path + "transition_3.mp3", TRANSITION_3_DURATION, TRANSITION_3_TURNING_POINT, false, false));
        transitions.add(new Transition(list[TRANSITION_4], path + "transition_4.mp3", TRANSITION_4_DURATION, TRANSITION_4_TURNING_POINT, false, false));
        transitions.add(new Transition(list[TRANSITION_5], path + "transition_5.mp3", TRANSITION_5_DURATION, TRANSITION_5_TURNING_POINT, false, false));
        transitions.add(new Transition(list[TRANSITION_6], path + "transition_6.mp3", TRANSITION_6_DURATION, TRANSITION_6_TURNING_POINT, false, false));
        return transitions;
    }

}

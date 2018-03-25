package com.merseyside.admin.player.ActivitesAndFragments;

import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.merseyside.admin.player.Dialogs.PresetDialog;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.EqualizerEngine;
import com.merseyside.admin.player.Utilities.PrintString;
import com.merseyside.admin.player.Utilities.SeekCircle;
import com.merseyside.admin.player.Utilities.Settings;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Created by Admin on 20.01.2017.
 */

public class EqualizerFragment extends android.support.v4.app.Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

    private CheckBox enabled = null;
    private  Button reset = null, preset = null;

    private  SeekCircle bassBoost, volume;
    private TextView bassBoostText, volumeText, speedText;
    private ImageView header;
    private TextView header_textView;


    private final int MIN_LEVEL = -1500;
    private final int MAX_LEVEL = 1500;

    static final int MAX_SLIDERS = 5;
    private SeekBar sliders[] = new SeekBar[MAX_SLIDERS];
    private TextView slider_labels[] = new TextView[MAX_SLIDERS];
    private SeekBar speed_bar;
    int num_sliders = 0;

    private EqualizerEngine equalizerEngine;
    private Settings settings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.equalizer_fragment, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        equalizerEngine = new EqualizerEngine(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        settings = new Settings(getActivity());
        header = (ImageView) getView().findViewById(R.id.equalizer_header);
        PrintString.printLog("lifeCycle", Settings.getScreenHeight() + " " + Settings.getScreenWidth());
        header.setImageBitmap(Settings.equalizer_header);

        header_textView = (TextView) getView().findViewById(R.id.equalizer_textview);
        settings.setTextViewFont(header_textView, null);

        reset = (Button) getView().findViewById(R.id.reset_btn);
        reset.setOnClickListener(this);

        enabled = (CheckBox) getView().findViewById(R.id.enable_cb);
        enabled.setOnCheckedChangeListener(this);

        preset = (Button) getView().findViewById(R.id.preset_btn);
        preset.setOnClickListener(this);

        sliders[0] = (SeekBar)getView().findViewById(R.id.frequenceBar_1);
        slider_labels[0] = (TextView)getView().findViewById(R.id.frequenceValue_1);
        sliders[1] = (SeekBar)getView().findViewById(R.id.frequenceBar_2);
        slider_labels[1] = (TextView)getView().findViewById(R.id.frequenceValue_2);
        sliders[2] = (SeekBar)getView().findViewById(R.id.frequenceBar_3);
        slider_labels[2] = (TextView)getView().findViewById(R.id.frequenceValue_3);
        sliders[3] = (SeekBar)getView().findViewById(R.id.frequenceBar_4);
        slider_labels[3] = (TextView)getView().findViewById(R.id.frequenceValue_4);
        sliders[4] = (SeekBar)getView().findViewById(R.id.frequenceBar_5);
        slider_labels[4] = (TextView)getView().findViewById(R.id.frequenceValue_5);

        if (equalizerEngine.getEqualizer() == null || equalizerEngine.getBassBoost() == null){
            MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.scndl);
            int session = mp.getAudioSessionId();
            equalizerEngine.setEqualizers(session);
        }

        num_sliders = (int) equalizerEngine.getEqualizer().getNumberOfBands();

        for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++) {
            int[] freq_range = equalizerEngine.getEqualizer().getBandFreqRange((short)i);
            sliders[i].setOnSeekBarChangeListener(this);
            slider_labels[i].setText (formatBandLabel(freq_range));
        }

        for (int i = num_sliders ; i < MAX_SLIDERS; i++) {
            sliders[i].setVisibility(View.GONE);
            slider_labels[i].setVisibility(View.GONE);
        }

        bassBoostText = (TextView) getView().findViewById(R.id.bass_boost_text);
        bassBoost = (SeekCircle)getView().findViewById(R.id.bass_boost_circle);
        bassBoost.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {}
            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle){}

            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser)
            {
                updateBassBoost();
            }
        });

        speed_bar = (SeekBar) getView().findViewById(R.id.speed_bar);
        speed_bar.setOnSeekBarChangeListener(this);
        speedText = (TextView) getView().findViewById(R.id.speed_koef);

        volumeText = (TextView) getView().findViewById(R.id.volume_text);
        volume = (SeekCircle)getView().findViewById(R.id.volume_circle);
        volume.setOnSeekCircleChangeListener(new SeekCircle.OnSeekCircleChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekCircle seekCircle) {}
            @Override
            public void onStartTrackingTouch(SeekCircle seekCircle){}

            @Override
            public void onProgressChanged(SeekCircle seekCircle, int progress, boolean fromUser){
                updateVolume();
            }
        });
        updateUI();
    }

    private void updateUI () {
        int[] frequences = equalizerEngine.getFrequences();
        updateSliders(frequences);
        enabled.setChecked(equalizerEngine.getEqualizerEnable());
        setBassBoost(frequences[0]);
        setVolume(equalizerEngine.getVolumeInt());
        setSpeed(equalizerEngine.getSpeedInt());
    }

    private void setVolume(int volume_value){
        volume.setProgress(volume_value);
        volumeText.setText(Integer.toString(volume_value) + "%");
    }

    private void updateVolume(){
        int volume_value = volume.getProgress();
        volume.setProgress(volume_value);
        volumeText.setText(Integer.toString(volume_value) + "%");
        equalizerEngine.setVolume(volume_value);
        PrintString.printLog("Equalizer", volume_value + "%");
    }

    private void updateSliders (int frequences[])
    {
        for (int i = 1; i < frequences.length; i++)
        {
            int level = frequences[i];
            int pos = 100 * level / (MAX_LEVEL - MIN_LEVEL) + 50;
            sliders[i-1].setProgress (pos);
        }
    }

    private void setFlat () {
        for (int i = 0; i < num_sliders; i++)
        {
            equalizerEngine.setEqualiserBand(i, 0);
        }
        equalizerEngine.setBassBoost(0);
        updateUI();
    }

    private String formatBandLabel (int[] band) {
        return milliHzToString(band[0]) + "-" + milliHzToString(band[1]);
    }

    private String milliHzToString (int milliHz) {
        if (milliHz < 1000) return "";
        if (milliHz < 1000000) return "" + (milliHz / 1000) + "Hz";
        else return "" + (milliHz / 1000000) + "kHz";
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        equalizerEngine.saveEqualizerSettings();
        equalizerEngine.getEqualizerFrequences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.reset_btn:{
                setFlat();
                break;
            }
            case R.id.preset_btn:{
                ArrayList<String> presets = new ArrayList<>();
                for (short i = 0; i<equalizerEngine.getEqualizer().getNumberOfPresets(); i++){
                    presets.add(equalizerEngine.getEqualizer().getPresetName(i));
                    PrintString.printLog("Preset", presets.get(i));
                }
                PresetDialog dialog = new PresetDialog(getActivity(), presets, new PresetDialog.PresetDialogListener() {
                    @Override
                    public void userPressedItem(int position) {
                        try {
                            equalizerEngine.getEqualizer().usePreset((short) position);
                            updateFromEqualizer();
                        } catch (RuntimeException ignored){}
                    }
                });
                dialog.show();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()){
            case R.id.enable_cb:{
                equalizerEngine.setEqualizerEnable(isChecked);
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int level, boolean fromPosition) {
        if (seekBar == speed_bar){
            float new_level = setSpeed(level);
            equalizerEngine.setSpeed(new_level);
        } else {
            int new_level = MIN_LEVEL + (MAX_LEVEL - MIN_LEVEL) * level / 100;
            for (int i = 0; i < num_sliders; i++) {
                if (sliders[i] == seekBar) {
                    equalizerEngine.setEqualiserBand(i, new_level);
                    PrintString.printLog("Equalizer", new_level + " " + i);
                    break;
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}


    private void updateBassBoost() {
        if (bassBoostText != null && bassBoost != null){
            int progress = bassBoost.getProgress();
            bassBoostText.setText(Integer.toString(progress) + "%");
            equalizerEngine.setBassBoost(progress*10);
        }
    }

    private void setBassBoost(int value){
        if (bassBoostText != null && bassBoost != null){
            bassBoost.setProgress(value/10);
            int progress = bassBoost.getProgress();
            bassBoostText.setText(Integer.toString(progress) + "%");
        }
    }

    private float setSpeed(int value){
        if (speed_bar!=null && speedText != null){
            speed_bar.setProgress(value);
            float s = (float) value * 0.5f / 10;
            String speed = "x" + String.format(Locale.US, "%.2f", s);
            speedText.setText(speed);
            return s;
        }
        return 1f;
    }

    private void updateFromEqualizer(){
        Equalizer eq = equalizerEngine.getEqualizer();
        for (int i = 0; i<num_sliders; i++){
            int level = eq.getBandLevel((short) i);
            int pos = 100 * level / (MAX_LEVEL - MIN_LEVEL) + 50;
            sliders[i].setProgress(pos);
            equalizerEngine.setEqualiserBand((short)i, (short)level);
        }
    }
}

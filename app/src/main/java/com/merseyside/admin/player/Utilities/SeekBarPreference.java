package com.merseyside.admin.player.Utilities;

/**
 * Created by Admin on 28.12.2016.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.preference.DialogPreference;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.merseyside.admin.player.R;


public final class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {

    // Namespaces to read attributes
    private static final String PREFERENCE_NS = "http://schemas.android.com/apk/lib/com.merseyside.admin.player.Utilities";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Attribute names
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "minValue";
    private static final String ATTR_MAX_VALUE = "maxValue";

    // Default values for defaults
    private static final int DEFAULT_CURRENT_VALUE = 0;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 10;

    // Real defaults
    private final int mDefaultValue;
    private final int mMaxValue;
    private final int mMinValue;

    // Current value
    private int mCurrentValue;

    // View elements
    private SeekBar mSeekBar;
    private TextView mValueText;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Read parameters from attributes
        mMinValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
        mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);
        mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
    }

    @Override
    protected View onCreateDialogView() {
        // Get current value from preferences
        mCurrentValue = getPersistedInt(mDefaultValue);

        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_slider, null);

        // Setup minimum and maximum text labels
        ((TextView) view.findViewById(R.id.min_value)).setText(String.valueOf(mMinValue));
        ((TextView) view.findViewById(R.id.max_value)).setText(String.valueOf(mMaxValue));

        // Setup SeekBar
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mCurrentValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        // Setup text label for current value
        mValueText = (TextView) view.findViewById(R.id.current_value);
        mValueText.setText(Integer.toString(mCurrentValue));

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (!positiveResult) {
            return;
        }

        if (shouldPersist()) {
            persistInt(mCurrentValue);
        }

        notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
        // Format summary string with current value
        String summary = super.getSummary().toString();
        int value = getPersistedInt(mDefaultValue);
        return String.format(summary, value);
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        // Update current value
        mCurrentValue = value + mMinValue;
        // Update label with current value
        mValueText.setText(Integer.toString(mCurrentValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
        // Not used
    }

    public void onStopTrackingTouch(SeekBar seek) {
        // Not used
    }
}

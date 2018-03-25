package com.merseyside.admin.player.ActivitesAndFragments;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.merseyside.admin.player.Dialogs.InfoDialog;
import com.merseyside.admin.player.R;
import com.merseyside.admin.player.Utilities.LicenseCheckerEngine;
import com.merseyside.admin.player.Utilities.Settings;

import java.util.ArrayList;

/**
 * Created by Admin on 10.03.2017.
 */

public class AboutPlayerFragment extends Fragment implements View.OnClickListener {

    private static ArrayList<String> player_log, greeting, author_info;
    private RelativeLayout changelog, author, check_license, purchase, rate, youtube, kryptex;
    private TextView license, header_textView;
    private Settings settings;
    private LicenseCheckerEngine licenseCheckerEngine;
    private ImageView license_imageView;
    int a = 2;


    public static ArrayList<String> getLog(Resources resources){
        player_log = new ArrayList<>();
        player_log.add(resources.getString(R.string.log_v0_8));
        player_log.add(resources.getString(R.string.log_v0_7));
        player_log.add(resources.getString(R.string.log_v0_6));
        player_log.add(resources.getString(R.string.log_v0_5));
        player_log.add(resources.getString(R.string.log_v0_4));
        player_log.add(resources.getString(R.string.log_v0_3));
        player_log.add(resources.getString(R.string.log_v0_2));
        player_log.add(resources.getString(R.string.log_v0_1));
        return player_log;
    }

    public static ArrayList<String> getGreeting(Resources resources){
        greeting = new ArrayList<>();
        if (!Settings.isProVersion()) greeting.add(resources.getString(R.string.greeting));
        else greeting.add(resources.getString(R.string.customer_greeting));
        return greeting;
    }

    public static ArrayList<String> getAuthor(Resources resources){
        author_info = new ArrayList<>();
        author_info.add(resources.getString(R.string.author_info));
        return author_info;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
    }

    private String setLicensePassed(){
        String str = getActivity().getResources().getString(R.string.license_passed);
        license_imageView.setImageResource(R.drawable.check);
        check_license.setVisibility(View.GONE);
        license.setText(str);
        return str;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        header_textView = (TextView) getView().findViewById(R.id.about_textview);
        settings.setTextViewFont(header_textView, null);

        changelog = (RelativeLayout) getView().findViewById(R.id.changelog_layout);
        changelog.setOnClickListener(this);
        license = (TextView) getView().findViewById(R.id.license_textView);
        author= (RelativeLayout) getView().findViewById(R.id.author_layout);
        author.setOnClickListener(this);
        rate= (RelativeLayout) getView().findViewById(R.id.rate_layout);
        rate.setOnClickListener(this);
        youtube= (RelativeLayout) getView().findViewById(R.id.youtube_layout);
        youtube.setOnClickListener(this);
        license_imageView = (ImageView) getView().findViewById(R.id.license_imageview);
        kryptex = (RelativeLayout) getView().findViewById(R.id.kryptex_layout);
        kryptex.setOnClickListener(this);
        check_license = (RelativeLayout) getView().findViewById(R.id.check_license_layout);
        purchase = (RelativeLayout) getView().findViewById(R.id.buy_license_layout);
        if (Settings.isProVersion()) {
            purchase.setVisibility(View.GONE);
            check_license.setVisibility(View.VISIBLE);
            check_license.setOnClickListener(this);
        } else {
            purchase.setOnClickListener(this);
        }
        String str;
        if (!Settings.isProVersion()){
            str = getActivity().getResources().getString(R.string.no_license) + String.valueOf(Settings.getLeftTrial());
        } else {
            if (Settings.LICENSE) str = setLicensePassed();
            else str = getActivity().getResources().getString(R.string.license_not_passed);
        }
        license.setText(str);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_fragment, null);
    }

    @Override
    public void onClick(View view) {
        InfoDialog dialog;
        switch (view.getId()){
            case R.id.changelog_layout:
                dialog = new InfoDialog(getActivity(), getActivity().getResources().getString(R.string.changelog), getLog(getActivity().getResources()));
                dialog.show();
                break;
            case R.id.author_layout:
                dialog = new InfoDialog(getActivity(), getActivity().getResources().getString(R.string.about_author), getAuthor(getActivity().getResources()), "VK");
                dialog.setInfoDialogListener(new InfoDialog.InfoDialogListener() {
                    @Override
                    public void checkboxClicked(boolean isChecked) {
                        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://vk.com/merseyside313"));
                        startActivity(rateIntent);
                    }
                });
                dialog.show();
                break;
            case R.id.check_license_layout:
                settings.checkLicense(false, new LicenseCheckerEngine.LicenseListener() {
                    @Override
                    public void licenseResult(boolean result, String message) {
                        if (result) setLicensePassed();
                    }
                });
                break;
            case R.id.buy_license_layout:
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://market.android.com/details?id=" + Settings.getProVersionPackageName()));
                startActivity(marketIntent);
                break;
            case R.id.rate_layout:
                Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://market.android.com/details?id=" + Settings.getTrialVersionPackageName()));
                startActivity(rateIntent);
                break;
            case R.id.youtube_layout:
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://www.youtube.com/channel/UC_aclUyoM4RigWAiHpLQLNg"));
                startActivity(youtubeIntent);
                break;
            case R.id.kryptex_layout:
                Intent kryptexIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://www.kryptex.org/?ref=fb64386f"));
                startActivity(kryptexIntent);
                break;
        }
    }
}

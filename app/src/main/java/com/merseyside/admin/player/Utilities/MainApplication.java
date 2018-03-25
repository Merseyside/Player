package com.merseyside.admin.player.Utilities;

import android.app.Application;
import android.content.Context;


public class MainApplication extends Application {
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
	}
}

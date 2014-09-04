package my.beta.bounce.library.util;

import android.util.Log;

public class Logger {
	private static final boolean LOG_ENABLED = true;

	public static void i(String tag, String msg) {
		if(!LOG_ENABLED) return;
		Log.i(tag, msg);
	}

	public static void d(String tag, String msg) {
		if(!LOG_ENABLED) return;
		Log.d(tag, msg);
	}

	public static void e(String tag, String msg) {
		if(!LOG_ENABLED)return;
		Log.e(tag, msg);
	}
	
	public static void v(String tag, String msg) {
		if(!LOG_ENABLED)return;
		Log.v(tag, msg);
	}
	
	public static void w(String tag, String msg) {
		if(!LOG_ENABLED)return;
		Log.w(tag, msg);
	}
}

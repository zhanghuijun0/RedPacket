package com.zhj.luckymoney;

import java.util.List;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 */
public class LuckyApplication extends Application {
	public static boolean autoGetMoney = false;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static void unlockScreen(Application activity) {
		KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
		final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("MyKeyguardLock");
		keyguardLock.disableKeyguard();

		PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
				"MyWakeLock");

		wakeLock.acquire();
	}

	@SuppressLint("NewApi")
	public static boolean isAccessibleEnabled(Application activity) {
		AccessibilityManager manager = (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);

		List<AccessibilityServiceInfo> runningServices = manager
				.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
		for (AccessibilityServiceInfo info : runningServices) {
			if (info.getId().equals(activity.getPackageName() + "/.MonitorService")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNotificationEnabled(Application activity) {
		ContentResolver contentResolver = activity.getContentResolver();
		String enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");

		if (!TextUtils.isEmpty(enabledListeners)) {
			return enabledListeners
					.contains(activity.getPackageName() + "/" + activity.getPackageName() + ".NotificationService");
		} else {
			return false;
		}
	}
}

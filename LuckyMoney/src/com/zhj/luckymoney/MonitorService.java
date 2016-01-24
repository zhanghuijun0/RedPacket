package com.zhj.luckymoney;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * 
 * @author Administrator
 *
 */
public class MonitorService extends AccessibilityService {
	static final String TAG = "MonitorService";
	static final String WECHAT_PACKAGENAME = "com.tencent.mm";// 微信的包名
	// static final String QQ_PACKAGENAME = "com.tencent.mobileqq";// QQ的包名
	static final String WeiXin_TEXT_KEY = "[微信红包]";// 红包消息的关键字
	// static final String QQ_TEXT_KEY = "[QQ红包]";
	private String mLuckyMoneyReceive = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
	private String mLuckyMoneyDetail = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
	private String mLauncherUI = "com.tencent.mm.ui.LauncherUI";// 聊天界面（从桌面进去）
	private String mListView = "android.widget.ListView";// 聊天界面（从联系人界面进去）
	private String mLoadingView = "com.tencent.mm.ui.base.p";// 拆红包加载界面
	// private String mQQListView =
	// "com.tencent.mobileqq.activity.SplashActivity";// 拆红包加载界面
	Handler handler = new Handler();

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		final int eventType = event.getEventType();
		Log.d(TAG, "消息内容：" + event.getText() + ",消息类型：" + eventType
				+ ",event.getParcelableData():" + event.getParcelableData());
		// 通知变化事件，打开通知
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence str : texts) {
					String text = String.valueOf(str);
					if (text.contains(WeiXin_TEXT_KEY)) {
						LuckyApplication.unlockScreen(getApplication());
						openNotify(event);
						break;
					} else {
						Log.i(TAG, "没有包含红包关键字，这不是红包！");
					}
				}
			} else {
				Log.i(TAG, "message is Empty！");
			}
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
				|| eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
			Log.d(TAG, "-事件autoGetMoney:" + LuckyApplication.autoGetMoney);
			openHongBao(event);
		}
	}

	/*
	 * @Override protected boolean onKeyEvent(KeyEvent event) {
	 * 
	 * //接收按键事件 //return super.onKeyEvent(event); return true; }
	 */

	@Override
	public void onInterrupt() {
		// 服务中断，如授权关闭或者将服务杀死
		Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		// 连接服务后,一般是在授权成功后会接收到
		Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
	}

	private void sendNotifyEvent() {
		AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
		if (!manager.isEnabled()) {
			return;
		}
		AccessibilityEvent event = AccessibilityEvent
				.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
		event.setPackageName(WECHAT_PACKAGENAME);
		event.setClassName(Notification.class.getName());
		CharSequence tickerText = WeiXin_TEXT_KEY;
		event.getText().add(tickerText);
		manager.sendAccessibilityEvent(event);
	}

	/**
	 * 打开通知栏消息
	 * 
	 * @param event
	 */
	private void openNotify(AccessibilityEvent event) {
		if (event.getParcelableData() != null
				&& (event.getParcelableData() instanceof Notification)) {
			// 以下是精华，将微信的通知栏消息打开
			Notification notification = (Notification) event
					.getParcelableData();
			PendingIntent pendingIntent = notification.contentIntent;
			try {
				LuckyApplication.autoGetMoney = true;
				pendingIntent.send();// 打开通知栏
				Log.i(TAG, "打开通知栏!");
			} catch (PendingIntent.CanceledException e) {
				Log.e(TAG, "打开通知栏异常！");
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "I dont konwn what happends!");
		}
	}

	/**
	 * 打开红包
	 * 
	 * @param event
	 */
	private void openHongBao(AccessibilityEvent event) {
		String className = String.valueOf(event.getClassName());
		Log.i(TAG, "event.getClassName():" + event.getClassName());
		if (className.equals(mLuckyMoneyReceive)) {
			LuckyApplication.autoGetMoney = false;
			checkKey1();
			Log.w(TAG, "拆红包界面：" + LuckyApplication.autoGetMoney);
		} else if (className.equals(mLuckyMoneyDetail)) {
			// 拆完红包后看详细的纪录界面
			LuckyApplication.autoGetMoney = false;
			Log.w(TAG, "红包详情界面" + LuckyApplication.autoGetMoney);
		} else if (className.equals(mLauncherUI) || className.equals(mListView)) {
			// 在微信聊天界面
			Log.w(TAG, "微信聊天界面autoGetMoney:" + LuckyApplication.autoGetMoney);
			if (LuckyApplication.autoGetMoney) {
				LuckyApplication.autoGetMoney = false;
				checkKey2();// 打开微信红包
			}
		} else if (className.equals(mLoadingView)) {
			// 加载……
		} else {
			Log.w(TAG, "其他……" + className);
		}
	}

	/**
	 * 拆红包
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void checkKey1() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			LuckyApplication.autoGetMoney = false;
			Log.w(TAG, "rootWindow为空");
			return;
		}

		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText("拆红包");
		for (AccessibilityNodeInfo n : list) {
			Log.i(TAG, "-->拆红包:" + n);
			LuckyApplication.autoGetMoney = false;
			n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}

		int parent_count = nodeInfo.getChildCount();
		Log.i(TAG, "parent_count:" + parent_count);
		for (int i = 0; i < parent_count; i++) {
			AccessibilityNodeInfo info = nodeInfo.getChild(i);
			Log.i(TAG, "info isClick:" + info);
			if (info.isClickable()) {
				LuckyApplication.autoGetMoney = false;
				info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				break;
			}
		}
		/*
		 * while(parent != null){ Log.i(TAG, "parent isClick:"+parent);
		 * if(parent.isClickable()){ LuckyApplication.autoGetMoney = false;
		 * parent.performAction(AccessibilityNodeInfo.ACTION_CLICK); break; }
		 * parent = parent.getParent(); }
		 */
	}

	/**
	 * 打开微信红包
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void checkKey2() {
		Log.i(TAG, "打开红包！");
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			LuckyApplication.autoGetMoney = false;
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText("领取红包");
		if (!list.isEmpty()) {
			// 最新的红包领起
			for (int i = list.size() - 1; i >= 0; i--) {
				AccessibilityNodeInfo parent = list.get(i).getParent();
				Log.i(TAG, "------==>领取红包:" + parent);
				if (parent != null) {
					LuckyApplication.autoGetMoney = false;
					parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					break;
				} else {
					Log.e(TAG, "parent is null，无法领取红包！");
				}
			}
		} else {
			Log.e(TAG, "没有找到“领取红包”的焦点，无法领取红包！");
		}
	}

}

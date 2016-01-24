package com.zhj.luckymoney;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class QQMonitorService extends AccessibilityService {
	private String TAG = "QQMonitorService";
	private static final String WECHAT_OPEN_EN = "Open";
	private static final String WECHAT_OPENED_EN = "You've opened";
	private final static String QQ_DEFAULT_CLICK_OPEN = "点击拆开";
	private final static String QQ_HONG_BAO_PASSWORD = "口令红包";
	private final static String QQ_CLICK_TO_PASTE_PASSWORD = "点击输入口令";
	private boolean mLuckyMoneyReceived;
	private String lastFetchedHongbaoId = null;
	private long lastFetchedTime = 0;
	private static final int MAX_CACHE_TOLERANCE = 5000;
	private AccessibilityNodeInfo rootNodeInfo;
	private List<AccessibilityNodeInfo> mReceiveNode;

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		List<CharSequence> message = event.getText();
		Log.i(TAG, "Event message:" + eventType + ",message:" + message);
		// 通知变化事件，打开通知
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence str : texts) {
					String text = String.valueOf(str);
					if (text.contains("[QQ红包]")) {
						LuckyApplication.unlockScreen(getApplication());
						openNotify(event);
						break;
					} else {
						Log.i(TAG, "不是QQ红包……" + texts);
					}
				}
			}
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
				|| eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
			Log.d(TAG, "=====事件autoGetMoney:" + LuckyApplication.autoGetMoney);
			openHongBao(event);
			// List<AccessibilityNodeInfo> nodes1 = getText(event);
			// recycle(getRootInActiveWindow());
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
		if (className.equals("android.widget.EditText")
				|| className
						.equals("com.tencent.mobileqq.activity.SplashActivity")) {
			// 聊天界面
			Log.i(TAG, "QQ聊天界面" + className);
			if (LuckyApplication.autoGetMoney) {
				LuckyApplication.autoGetMoney = false;
				checkKey2();// 打开微信红包
			}
		} else if (className.equals("android.widget.AbsListView")) {
			// 消息列表界面
			Log.i(TAG, "QQ消息列表界面" + className);
		} else if (className
				.equals("cooperation.qwallet.plugin.QWalletPluginProxyActivity")) {
			// 打开红包界面
			Log.i(TAG, "QQ红包详情界面" + className);
		} else {
			Log.w(TAG, "其他……" + className);
		}
	}

	/**
	 * 打开微信红包
	 */
	private void checkKey2() {
		Log.i(TAG, "打开红包！");
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			LuckyApplication.autoGetMoney = false;
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByText("QQ红包");
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
	
	public void recycle(AccessibilityNodeInfo info) {
		if (info.getChildCount() == 0) {
			Log.i(TAG, "----countText:" + info.getText() + "--countClassName:"
					+ info.getClassName());
			/* 这个if代码的作用是：匹配“点击输入口令的节点，并点击这个节点” */
			if (info.getText() != null
					&& info.getText().toString()
							.equals(QQ_CLICK_TO_PASTE_PASSWORD)) {
				info.getParent().performAction(
						AccessibilityNodeInfo.ACTION_CLICK);
			}
			/* 这个if代码的作用是：匹配文本编辑框后面的发送按钮，并点击发送口令 */
			if (info.getClassName().toString().equals("android.widget.Button")
					&& info.getText().toString().equals("发送")) {
				info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if (info.getChild(i) != null) {
					recycle(info.getChild(i));
				}
			}
		}
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

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	private List<AccessibilityNodeInfo> getText(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		AccessibilityNodeInfo nodeInfo = event.getSource();
		String[] arrays = new String[] { QQ_DEFAULT_CLICK_OPEN,
				QQ_HONG_BAO_PASSWORD, QQ_CLICK_TO_PASTE_PASSWORD, "发送" };
		for (String str : arrays) {
			if (str == null) {
				continue;
			}
			List<AccessibilityNodeInfo> nodes = nodeInfo
					.findAccessibilityNodeInfosByText(str);
			if (!nodes.isEmpty()) {
				if (str.equals(WECHAT_OPEN_EN)
						&& !nodeInfo.findAccessibilityNodeInfosByText(
								WECHAT_OPENED_EN).isEmpty()) {
					continue;
				}
				return nodes;
			}
		}
		return null;
	}

	private String getHongbaoText(AccessibilityNodeInfo node) {
		/* 获取红包上的文本 */
		String content;
		try {
			AccessibilityNodeInfo i = node.getParent().getChild(0);
			content = i.getText().toString();
		} catch (NullPointerException npe) {
			return null;
		}
		return content;
	}
}
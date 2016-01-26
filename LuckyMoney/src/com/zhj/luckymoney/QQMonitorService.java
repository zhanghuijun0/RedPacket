package com.zhj.luckymoney;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * QQ服务监控
 * 
 * @author Administrator
 *
 */
@SuppressLint("NewApi")
public class QQMonitorService extends AccessibilityService {
	private String TAG = "QQMonitorService";
	private String mMoney = "0.00";
	private String mName = "";
	private boolean mIfReply = false;// 是否
	private boolean mIfOpenNotify = false;
	private Helper mHelper = new Helper();

	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int eventType = event.getEventType();
		List<CharSequence> message = event.getText();
		Log.i(TAG, "Event message:" + eventType + ",message:" + message);
		String className = String.valueOf(event.getClassName());
		Log.i(TAG, "event.getClassName():" + event.getClassName());
		// 通知变化事件，打开通知
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence str : texts) {
					String text = String.valueOf(str);
					if (text.contains("[QQ红包]")) {
						LuckyApplication.unlockScreen(getApplication());
						openNotify(event);
						Log.i(TAG, "---------收到QQ红包---------" + texts);
						break;
					} else {
						Log.i(TAG, "不是QQ红包……" + texts);
					}
				}
			}
		} else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
				|| eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
			Log.i(TAG, "-----------mIfOpenNotify:" + mIfOpenNotify);
			if (mIfOpenNotify) {
				if (!Constant.ISREPLY) {
					mIfOpenNotify = false;
				}
				openHongBao(event);
			}
		}
	}

	/**
	 * 打开红包
	 * 
	 * @param event
	 */
	private void openHongBao(AccessibilityEvent event) {
		String className = String.valueOf(event.getClassName());
		if (className.equals("android.widget.EditText")
				|| className.equals("com.tencent.mobileqq.activity.SplashActivity")) {
			// 聊天界面
			Log.d(TAG, "QQ聊天界面" + className);
			if (LuckyApplication.autoGetMoney) {
				LuckyApplication.autoGetMoney = false;
				checkKey2();// 打开QQ红包
			}
			// 是否回复
			if (Constant.ISREPLY) {
				if (mIfReply) {
					mIfOpenNotify = false;
					String message = mHelper.getRandomReplyMessage(mName, mMoney);
					AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
					setReplyText(nodeInfo, message);
					setSendClick(getRootInActiveWindow());
					mIfReply = false;
					if (Constant.ISGOHOME) {
						setBackHome();// 返回主界面
					}
				}
			}

		} else if (className.equals("cooperation.qwallet.plugin.QWalletPluginProxyActivity")) {
			// 对打开红包界面进行处理
			if (Constant.ISREPLY) {
				if (!mIfReply) {
					AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
					mMoney = getMoney(nodeInfo);
					mName = getReplyPerson(nodeInfo);
					setCloseDetailPage(nodeInfo);
					mIfReply = true;
				}
			}

			Log.i(TAG, "QQ红包详情界面" + className);
		} else if (className.equals("android.widget.AbsListView")) {
			// 消息列表界面
			Log.i(TAG, "QQ消息列表界面" + className);
		} else {
			Log.w(TAG, "其他……" + className);
		}
	}

	/**
	 * 打开通知栏消息
	 * 
	 * @param event
	 */
	private void openNotify(AccessibilityEvent event) {
		if (Constant.ISDELAY) {
			mHelper.setDelayRandom(Constant.DELAY);
		}
		if (event.getParcelableData() != null && (event.getParcelableData() instanceof Notification)) {
			// 以下是精华，将QQ的通知栏消息打开
			Notification notification = (Notification) event.getParcelableData();
			PendingIntent pendingIntent = notification.contentIntent;
			try {
				LuckyApplication.autoGetMoney = true;
				pendingIntent.send();// 打开通知栏
				mIfOpenNotify = true;
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
	 * 打开QQ红包
	 */
	private void checkKey2() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		if (nodeInfo == null) {
			LuckyApplication.autoGetMoney = false;
			Log.w(TAG, "rootWindow为空");
			return;
		}
		List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("QQ红包");// 找到“QQ红包”关键字的节点
		if (!list.isEmpty()) {
			for (int i = list.size() - 1; i >= 0; i--) {
				AccessibilityNodeInfo parent = list.get(i).getParent();
				if (parent != null) {
					List<AccessibilityNodeInfo> pwdMoney = parent.findAccessibilityNodeInfosByText("口令红包");// 口令红包
					List<AccessibilityNodeInfo> normalMoney = parent.findAccessibilityNodeInfosByText("点击拆开");// 普通红包
					LuckyApplication.autoGetMoney = false;
					parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					// 如果是口令红包，需要特殊处理
					if (pwdMoney != null && !pwdMoney.isEmpty()) {
						sendPassword(nodeInfo);// 点击口令并发送
						Log.e(TAG, "口令红包");
					} else if (normalMoney != null && !normalMoney.isEmpty()) {
						Log.e(TAG, "普通类型QQ红包");
					} else {
						Log.e(TAG, "未知类型红包");
					}
					break;
				} else {
					Log.e(TAG, "parent is null，无法领取红包！");
				}
			}
		} else {
			Log.e(TAG, "没有找到“QQ红包”的焦点，无法领取红包！");
		}
	}

	/**
	 * 点击口令并发送（针对口令红包）
	 * 
	 * @param nodeInfo
	 */
	private void sendPassword(AccessibilityNodeInfo nodeInfo) {
		if (Constant.ISDELAY) {
			mHelper.setDelayRandom(Constant.DELAY);
		}
		List<AccessibilityNodeInfo> clickMeList = nodeInfo.findAccessibilityNodeInfosByText("点击输入口令");//
		if (!clickMeList.isEmpty()) {
			for (int j = clickMeList.size() - 1; j >= 0; j--) {
				AccessibilityNodeInfo clickMe = clickMeList.get(j).getParent();
				clickMe.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				Log.e(TAG, "“点击输入口令”已点击！");
			}
		}
		List<AccessibilityNodeInfo> sendList = nodeInfo.findAccessibilityNodeInfosByText("发送");//
		if (!sendList.isEmpty()) {
			for (int j = sendList.size() - 1; j >= 0; j--) {
				AccessibilityNodeInfo send = sendList.get(j);
				send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				Log.e(TAG, "“发送”已点击！");
			}
		}
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	// --------------------------------------------------------------------

	/**
	 * 获取“发送红包的人”
	 * 
	 * @param nodeInfo
	 * @return
	 */
	private String getReplyPerson(AccessibilityNodeInfo nodeInfo) {
		// TODO Auto-generated method stub
		String sendName = "zhj";
		List<AccessibilityNodeInfo> senderInfo = nodeInfo
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/sender_info");// 找到“发送人”关键字的节点
		if (!senderInfo.isEmpty()) {
			for (int i = senderInfo.size() - 1; i >= 0; i--) {
				sendName = (String) senderInfo.get(i).getText();
				sendName = sendName.replaceAll("来自", "@");
				Log.i(TAG, "---------------发送人：" + sendName);
			}
		}
		return sendName;
	}

	/**
	 * 获取抢到的钱数
	 * 
	 * @param nodeInfo
	 * @return
	 */
	private String getMoney(AccessibilityNodeInfo nodeInfo) {
		// TODO Auto-generated method stub
		String money = "0.00";
		List<AccessibilityNodeInfo> list = nodeInfo
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/hb_count_tv");// 找到“元”关键字的节点
		if (!list.isEmpty()) {
			for (int i = list.size() - 1; i >= 0; i--) {
				money = (String) list.get(i).getText();
				Log.i(TAG, "---------------你抢到了：" + money);
			}
		} else {
			Log.i(TAG, "---------------来晚了，红包已被抢完！");
		}
		return money;
	}

	/**
	 * 关闭红包详情界面
	 * 
	 * @param nodeInfo
	 * @return
	 */
	private boolean setCloseDetailPage(AccessibilityNodeInfo nodeInfo) {
		if (!Constant.ISREPLY) {
			return false;
		}
		if (Constant.ISDELAY) {
			mHelper.setDelayRandom(Constant.DELAY);
		}
		List<AccessibilityNodeInfo> closeBtnList = nodeInfo
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/close_btn");// 找到“关闭”关键字
		if (!closeBtnList.isEmpty()) {
			AccessibilityNodeInfo closeBtn = closeBtnList.get(0);
			closeBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		return true;
	}

	/**
	 * 设置要发送的内容
	 * 
	 * @param nodeInfo
	 * @return
	 */
	private boolean setReplyText(AccessibilityNodeInfo nodeInfo, String message) {
		if (!Constant.ISREPLY) {
			return false;
		}
		if (Constant.ISDELAY) {
			mHelper.setDelayRandom(Constant.DELAY);
		}
		List<AccessibilityNodeInfo> inputList = nodeInfo
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/input");// 找到“关闭”关键字
		if (!inputList.isEmpty()) {
			AccessibilityNodeInfo input = inputList.get(0);
			Bundle arguments = new Bundle();
			arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message);
			input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
		}
		return true;
	}

	/**
	 * 点击“发送”
	 * 
	 * @param nodeInfo
	 * @return
	 */
	private boolean setSendClick(AccessibilityNodeInfo nodeInfo) {
		if (!Constant.ISREPLY) {
			return false;
		}
		if (Constant.ISDELAY) {
			mHelper.setDelayRandom(Constant.DELAY);
		}
		List<AccessibilityNodeInfo> sendList = nodeInfo.findAccessibilityNodeInfosByText("发送");//
		if (!sendList.isEmpty()) {
			for (int j = sendList.size() - 1; j >= 0; j--) {
				AccessibilityNodeInfo send = sendList.get(j);
				send.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				Log.i(TAG, "“发送”已点击！");
			}
		}
		return true;
	}

	/**
	 * 模拟点击Home按键
	 * 
	 * @return
	 */
	private boolean setBackHome() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		return false;
	}
}
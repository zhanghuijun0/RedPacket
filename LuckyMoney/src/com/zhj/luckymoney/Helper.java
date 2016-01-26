package com.zhj.luckymoney;

/**
 * 帮助类
 * 
 * @author Administrator
 *
 */
public class Helper {

	/**
	 * 执行延时
	 * 
	 * @param m
	 *            延时的毫秒数
	 * @return
	 */
	public boolean setDelayRandom(int m) {
		// TODO Auto-generated method stub
		double delay;
		if (m == 0) {
			delay = (Math.random() * 800);
		} else {
			delay = m;
		}
		try {
			Thread.sleep(Math.round(delay));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		System.out.println(Math.round(delay));
		return true;
	}

	/**
	 * 
	 * @param name
	 *            回复人的名字
	 * @param money
	 *            收到的钱数
	 * @return
	 */
	public String getRandomReplyMessage(String name, String money) {
		String message = null;
		Double x = Double.valueOf(money);
		if (!x.equals(0.00)) {
			String[] messageArray = { "谢谢!", name + "你的红包已收到", "非常感谢！", "抢到了" + money, "土豪，再来一个！" };
			message = messageArray[getRandom() % 5];
		}
		return message;
	}

	/**
	 * 产生0-9的随机数
	 * 
	 * @return
	 */
	private int getRandom() {
		// TODO Auto-generated method stub
		return (int) (Math.random() * 10);
	}

}

package com.zhj.luckymoney;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class DonationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donation);
		details();
	}

	/**
	 * 注意事项界面
	 */
	private void details() {
		// TODO Auto-generated method stub
		TextView copyRight = (TextView) findViewById(R.id.tv_bottom);
		String html = "<a href=\"http://weibo.com/u/3177677015\">©2016 俊俊  版权所有 v-0.1</a>";
		CharSequence charSequence = Html.fromHtml(html);
		copyRight.setMovementMethod(LinkMovementMethod.getInstance());
		copyRight.setText(charSequence);
	}
}

package com.zhj.luckymoney;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * 注意事项界面
 * @author zhj
 *
 */
public class DetailActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.detail);
		TextView copyRight = (TextView) findViewById(R.id.tv_bottom);
		String html = "<a href=\"http://weibo.com/u/3177677015\">©2016 俊俊  版权所有</a>";
		CharSequence charSequence = Html.fromHtml(html);
		copyRight.setMovementMethod(LinkMovementMethod.getInstance());
		copyRight.setText(charSequence);
		TextView back = (TextView) this
				.findViewById(R.id.imageview_above_menu_teacher);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
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

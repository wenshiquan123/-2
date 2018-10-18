package com.hlzx.ljdjsj.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.ScreenManager;

import java.util.ArrayList;

public class ScoreView extends LinearLayout {

	private final Context context;
	private final ArrayList<View> list;
	private static LayoutParams params;

	public ScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);

		//获取子定义资源 wenshiquan
		TypedArray a=context.obtainStyledAttributes(attrs, R.styleable.myratingbar);
		//评分条星星数
		int numStar=a.getInt(R.styleable.myratingbar_numStar,5);
		//评了几分
		int rating=a.getInt(R.styleable.myratingbar_rating,3);
		//空评分的图
		int empty_bg=a.getResourceId(R.styleable.myratingbar_empty,R.mipmap.star_dark);
		//亮评分的图
		int full_bg=a.getResourceId(R.styleable.myratingbar_full,R.mipmap.star_bright);
		a.recycle();
		this.context = context;
		params = new LayoutParams(
				ScreenManager.dp2px(getContext(),15), ScreenManager.dp2px(getContext(),15));
		params.setMargins(3, 0, 3, 0);
		list = new ArrayList<View>();
		for (int x = 0; x < numStar; x++) {
			View v = new View(context);
			v.setLayoutParams(params);

			v.setBackgroundResource(empty_bg);
			list.add(v);
			this.addView(v);
		}
		setScore(rating,empty_bg,full_bg);
	}

	public void setScore(int index,int dark,int bright) {
		for (int x = 0; x < list.size(); x++) {
			if (x < index)
				list.get(x).setBackgroundResource(bright);
			else
				list.get(x).setBackgroundResource(dark);
		}
	}

}

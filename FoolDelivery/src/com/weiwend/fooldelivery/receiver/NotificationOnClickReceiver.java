package com.weiwend.fooldelivery.receiver;

import java.io.File;

import com.weiwend.fooldelivery.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//用于在检测版本更新时，用户点击系统状态栏“正在下载”时的触发事件事件
public class NotificationOnClickReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String path=intent.getStringExtra("path");
		
		//如果软件下载成功，则打开系统安装软件的界面
		Utils.installApp(context, new File(path));
	}

}

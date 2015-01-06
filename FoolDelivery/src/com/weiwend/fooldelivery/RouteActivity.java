package com.weiwend.fooldelivery;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.overlay.BusRouteOverlay;
import com.amap.api.maps2d.overlay.DrivingRouteOverlay;
import com.amap.api.maps2d.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;

public class RouteActivity extends BaseActivity implements OnRouteSearchListener,OnClickListener{
		
	private AMap aMap;// 高德地图对象
	private MapView mapView;// 高德地图显示View
	
	private int busMode = RouteSearch.BusDefault;// 公交默认模式
	
	private int drivingMode = RouteSearch.DrivingDefault;// 驾车默认模式
	
	private int walkMode = RouteSearch.WalkDefault;// 步行默认模式
	
	private BusRouteResult busRouteResult;// 公交模式查询结果
	
	private DriveRouteResult driveRouteResult;// 驾车模式查询结果
	
	private WalkRouteResult walkRouteResult;// 步行模式查询结果
	
	private int routeType = 1;// 1代表公交模式，2代表驾车模式，3代表步行模式
	
	private RouteSearch routeSearch;//路径规划类
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_route);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(bundle);
		
		init();
	}

	//初始化界面信息
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);    //设置路径规划的监听器
		
		mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_route, RouteActivity.this);
		mProgressDialogUtils.setProgressDialogCancelable();
		mProgressDialogUtils.showDialog();
		
		double fromLat=getIntent().getDoubleExtra("FromLat", 0.0);
		double fromLon=getIntent().getDoubleExtra("FromLon", 0.0);
		double toLat=getIntent().getDoubleExtra("ToLat", 0.0);
		double toLon=getIntent().getDoubleExtra("ToLon", 0.0);
		
		LatLonPoint startLatLonPoint=new LatLonPoint(fromLat, fromLon);   //用户的当前位置
		LatLonPoint endLatLonPoint=new LatLonPoint(toLat,toLon);      //网点的位置
		
		routeType=3;   //设置路径规划模式:步行模式
		searchRouteResult(startLatLonPoint,endLatLonPoint);
	}

	 //方法必须重写
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	//方法必须重写
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	//方法必须重写
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	//方法必须重写
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	
	//开始搜索路径规划方案
	public void searchRouteResult(LatLonPoint startPoint, LatLonPoint endPoint) {
		
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				startPoint, endPoint);
		if (routeType == 1) {// 公交路径规划
			BusRouteQuery query = new BusRouteQuery(fromAndTo, busMode, "北京", 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
			routeSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
		} else if (routeType == 2) {// 驾车路径规划
			DriveRouteQuery query = new DriveRouteQuery(fromAndTo, drivingMode,
					null, null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
			routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
		} else if (routeType == 3) {// 步行路径规划
			WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
			routeSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
		}
	}

	//公交路线查询回调
	@Override
	public void onBusRouteSearched(BusRouteResult result, int rCode) {

		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {      //公交路线规划成功
				busRouteResult = result;
				BusPath busPath = busRouteResult.getPaths().get(0);
				aMap.clear();// 清理地图上的所有覆盖物
				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, aMap,
						busPath, busRouteResult.getStartPos(),
						busRouteResult.getTargetPos());
				routeOverlay.removeFromMap();
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
			} else {
				MyToast.makeText(RouteActivity.this, getString(R.string.no_result), MyToast.LENGTH_LONG).show();
			}
		} else if (rCode == 27) {
			MyToast.makeText(RouteActivity.this, getString(R.string.error_network), MyToast.LENGTH_LONG).show();
		} else if (rCode == 32) {
			MyToast.makeText(RouteActivity.this, getString(R.string.error_key), MyToast.LENGTH_LONG).show();
		} else {
			MyToast.makeText(RouteActivity.this, getString(R.string.error_other), MyToast.LENGTH_LONG).show();
		}
	}


	//驾车结果回调
	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {

		mProgressDialogUtils.dismissDialog();
		
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {      //驾车路径规划成功
				driveRouteResult = result;
				DrivePath drivePath = driveRouteResult.getPaths().get(0);
				aMap.clear();// 清理地图上的所有覆盖物
				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
						this, aMap, drivePath, driveRouteResult.getStartPos(),
						driveRouteResult.getTargetPos());
				drivingRouteOverlay.removeFromMap();
				drivingRouteOverlay.addToMap();
				drivingRouteOverlay.zoomToSpan();
			} else {
				MyToast.makeText(RouteActivity.this, getString(R.string.no_result), MyToast.LENGTH_LONG).show();
			}
		} else if (rCode == 27) {
			MyToast.makeText(RouteActivity.this, getString(R.string.error_network), MyToast.LENGTH_LONG).show();
		} else if (rCode == 32) {
			MyToast.makeText(RouteActivity.this, getString(R.string.error_key), MyToast.LENGTH_LONG).show();
		} else {
			MyToast.makeText(RouteActivity.this, getString(R.string.error_other), MyToast.LENGTH_LONG).show();
		}
	}

	//步行路线结果回调
	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
		
		mProgressDialogUtils.dismissDialog();
		
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {             //步行路径规划成功
				walkRouteResult = result;
				WalkPath walkPath = walkRouteResult.getPaths().get(0);
				aMap.clear();// 清理地图上的所有覆盖物
				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
						aMap, walkPath, walkRouteResult.getStartPos(),
						walkRouteResult.getTargetPos());
				walkRouteOverlay.removeFromMap();
				walkRouteOverlay.addToMap();
				walkRouteOverlay.zoomToSpan();
				
				MyToast.makeText(RouteActivity.this, getString(R.string.navigation_success), MyToast.LENGTH_LONG).show();
				
			} else {
				MyToast.makeText(RouteActivity.this, getString(R.string.no_result), MyToast.LENGTH_LONG).show();
			}
		} else if (rCode == 27) {
			//MyToast.makeText(RouteActivity.this, getString(R.string.error_network), MyToast.LENGTH_LONG).show();
			MyToast.makeText(RouteActivity.this, getString(R.string.navigation_failed), MyToast.LENGTH_LONG).show();
		} else if (rCode == 32) {
			//MyToast.makeText(RouteActivity.this, getString(R.string.error_key), MyToast.LENGTH_LONG).show();
			MyToast.makeText(RouteActivity.this, getString(R.string.navigation_failed), MyToast.LENGTH_LONG).show();
		} else {
			//MyToast.makeText(RouteActivity.this, getString(R.string.error_other), MyToast.LENGTH_LONG).show();
			MyToast.makeText(RouteActivity.this, getString(R.string.navigation_failed), MyToast.LENGTH_LONG).show();
		}
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_navigation));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.leftBtn:      //标题栏的返回功能
			
			finish();
			break;

		default:
			break;
		}
	}
}

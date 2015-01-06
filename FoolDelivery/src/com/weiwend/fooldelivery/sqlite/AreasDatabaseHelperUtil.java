package com.weiwend.fooldelivery.sqlite;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.weiwend.fooldelivery.utils.PinyinUtils;
import com.weiwend.invalid.AddressItem2;
import com.weiwend.invalid.AreaItem2;

//******************************该类由于需求问题，暂时已经作废*******************************
public class AreasDatabaseHelperUtil {
	
	private static AreasDatabaseHelper mDatabaseHelper;
	private Context mContext;
	
	private static String TABLE_NAME_AREA="areas_table";
	private static String TABLE_ADDRESS_SENDER="address_sender_table";
	
	public AreasDatabaseHelperUtil(Context context)
	{
		mContext=context;
		mDatabaseHelper=new AreasDatabaseHelper(mContext);
	}
	
	public static void addNewAreaInfo(String area_id,int parent_id,int area_name,String sort)
	{
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("area_id",area_id);
		values.put("parent_id",parent_id);
		values.put("area_name",area_name);
		values.put("sort",sort);
		db.insert(TABLE_NAME_AREA, null, values);
		db.close();
		
		Log.e("sqlite","add a new area info...");
	}
	
	public void addNewAreaInfosWithJsonArray(JSONArray jsonArray)
	{
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		String area_id="",parent_id="",area_name="",sort="",first_letters="";
		JSONObject jsonObject;
		ContentValues values;
		
		try {
			for(int i=0;i<jsonArray.length();i++){
				
				jsonObject=jsonArray.getJSONObject(i);
				area_id=jsonObject.getString("area_id");
				parent_id=jsonObject.getString("parent_id");
				area_name=jsonObject.getString("area_name");
				sort=jsonObject.getString("sort");
				
				first_letters=PinyinUtils.getPinYinHeadChar(area_name);
				
				values= new ContentValues();
				values.put("area_id",area_id);
				values.put("parent_id",parent_id);
				values.put("area_name",area_name);
				values.put("first_letters",first_letters);
				values.put("sort",sort);
				
				db.insert(TABLE_NAME_AREA, null, values);
				
				Log.e("zyf","add area info "+i+"succcess...");
			}
			
			Log.e("sqlite","add area infos with JsonArray success...");
		} catch (Exception e) {
			Log.e("sqlite","insert into Area exception: "+e.toString());
		}
		
		if(db.isOpen()){
			db.close();
		}
	}
	
	public boolean isAreaInfosCreated()
	{
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME_AREA, null, null, null, null, null, null);
		if(cursor==null)
		{
			db.close();
			return false;
		}
		while(cursor.moveToNext()){
			cursor.close();
			db.close();
			return true;
		}
		
		cursor.close();
		db.close();
		return false;
	}
	
	public ArrayList<AreaItem2> getFirstAreas()
	{
		ArrayList<AreaItem2> areaItems=new ArrayList<AreaItem2>();
		
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME_AREA, null, "parent_id=?", new String[]{"0"}, null, null, null);
		
		if(cursor!=null&&cursor.getCount()>0)
    	{
			AreaItem2 areaItem;
    		while(cursor.moveToNext())
        	{
    			areaItem=new AreaItem2();
    			
    			areaItem.setArea_id(cursor.getString(cursor.getColumnIndex("area_id")));
    			areaItem.setArea_name(cursor.getString(cursor.getColumnIndex("area_name")));
    			areaItem.setParent_id(cursor.getString(cursor.getColumnIndex("parent_id")));
    			areaItem.setSort(cursor.getString(cursor.getColumnIndex("sort")));
    			
    			areaItems.add(areaItem);
        	}
    	}else
    	{
    		Log.e("sqlite","no area info ...");
    	}
        
		cursor.close();
		db.close();
		
		return areaItems;
	}
	
	public ArrayList<AreaItem2> getNextAreas(String parent_id)
	{
		ArrayList<AreaItem2> areaItems=new ArrayList<AreaItem2>();
		
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME_AREA, null, "parent_id=?", new String[]{parent_id}, null, null, null);
		
		if(cursor!=null&&cursor.getCount()>0)
    	{
			AreaItem2 areaItem;
    		while(cursor.moveToNext())
        	{
    			areaItem=new AreaItem2();
    			
    			areaItem.setArea_id(cursor.getString(cursor.getColumnIndex("area_id")));
    			areaItem.setArea_name(cursor.getString(cursor.getColumnIndex("area_name")));
    			areaItem.setParent_id(cursor.getString(cursor.getColumnIndex("parent_id")));
    			areaItem.setSort(cursor.getString(cursor.getColumnIndex("sort")));
    			
    			areaItems.add(areaItem);
        	}
    	}else
    	{
    		Log.e("sqlite","no area info ...");
    	}
        
		cursor.close();
		db.close();
		
		return areaItems;
	}
	
	//Fuzzy query with Chinese key 
	public ArrayList<AreaItem2> getFuzzyAreasWithChineseKey(String key)
	{
		ArrayList<AreaItem2> areaItems=new ArrayList<AreaItem2>();
		
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME_AREA, null, "area_name LIKE?", new String[]{"%"+key+"%"}, null, null, null);
		
		if(cursor!=null&&cursor.getCount()>0)
    	{
			AreaItem2 areaItem;
    		while(cursor.moveToNext())
        	{
    			areaItem=new AreaItem2();
    			
    			areaItem.setArea_id(cursor.getString(cursor.getColumnIndex("area_id")));
    			areaItem.setArea_name(cursor.getString(cursor.getColumnIndex("area_name")));
    			areaItem.setParent_id(cursor.getString(cursor.getColumnIndex("parent_id")));
    			areaItem.setSort(cursor.getString(cursor.getColumnIndex("sort")));
    			
    			areaItems.add(areaItem);
        	}
    	}else{
    		Log.e("sqlite","no fuzzy area info ...");
    	}
        
		cursor.close();
		db.close();
		
		return areaItems;
	}
	
	//Fuzzy query with English key 
	public ArrayList<AreaItem2> getFuzzyAreasWithEnglishKey(String key)
	{
		ArrayList<AreaItem2> areaItems=new ArrayList<AreaItem2>();
		
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME_AREA, null, "first_letters LIKE?", new String[]{"%"+key+"%"}, null, null, null);
		
		if(cursor!=null&&cursor.getCount()>0)
    	{
			AreaItem2 areaItem;
    		while(cursor.moveToNext())
        	{
    			areaItem=new AreaItem2();
    			
    			areaItem.setArea_id(cursor.getString(cursor.getColumnIndex("area_id")));
    			areaItem.setArea_name(cursor.getString(cursor.getColumnIndex("area_name")));
    			areaItem.setParent_id(cursor.getString(cursor.getColumnIndex("parent_id")));
    			areaItem.setSort(cursor.getString(cursor.getColumnIndex("sort")));
    			
    			areaItems.add(areaItem);
        	}
    	}else{
    		Log.e("zyf","no fuzzy area info ...");
    	}
        
		cursor.close();
		db.close();
		
		return areaItems;
	}
	
	//get addresses of Sender or Recipient
	public void addNewSenderAddress(AddressItem2 item)
	{
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("userId",item.getUserId());
		values.put("name",item.getName());
		values.put("phone",item.getPhone());
		values.put("area",item.getArea());
		values.put("detailAddress",item.getDetailAddress());
		db.insert(TABLE_ADDRESS_SENDER, null, values);
		db.close();
		
		Log.e("zyf","add a new sender address...");
	}
	
	public void deleteSenderAddress(String senderId)
	{
        SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
        String[] args={String.valueOf(senderId)};
        db.delete(TABLE_ADDRESS_SENDER, "senderId=?", args);
		
		Log.e("zyf","delete a sender address success...");
	}
	
	public ArrayList<AddressItem2> getAllSenderAddresses(String userId)
	{
		ArrayList<AddressItem2> senderAddresses=new ArrayList<AddressItem2>();
		
		SQLiteDatabase db=mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query(TABLE_ADDRESS_SENDER, null, "userId=?", new String[]{userId}, null, null, null);
		
		if(cursor!=null&&cursor.getCount()>0)
    	{
			AddressItem2 addressItem;
    		while(cursor.moveToNext())
        	{
    			addressItem=new AddressItem2();
    			
    			addressItem.setSenderId(cursor.getString(cursor.getColumnIndex("senderId")));
    			addressItem.setUserId(cursor.getString(cursor.getColumnIndex("userId")));;
    			addressItem.setName(cursor.getString(cursor.getColumnIndex("name")));
    			addressItem.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
    			addressItem.setArea(cursor.getString(cursor.getColumnIndex("area")));
    			addressItem.setDetailAddress(cursor.getString(cursor.getColumnIndex("detailAddress")));
    			
    			senderAddresses.add(addressItem);
        	}
    	}else{
    		Log.e("zyf","no sender address ...");
    	}
        
		cursor.close();
		db.close();
		
		return senderAddresses;
	}
}

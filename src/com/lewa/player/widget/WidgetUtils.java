package com.lewa.player.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import android.util.Log;
import 	android.graphics.Bitmap;
import  android.graphics.Canvas;
import 	android.graphics.Rect;
import 	android.graphics.Paint;
import 	java.lang.Exception;
import 	android.graphics.Bitmap.Config;
import android.graphics.PorterDuffXfermode;
import 	android.graphics.PorterDuff.Mode;
import 	android.graphics.RectF;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import 	android.graphics.Matrix;
import java.io.File;  
import com.lewa.player.online.DownLoadAllPicsAsync;
import android.os.Environment;




public class WidgetUtils {
	public static final String TAG = "WidgetUtils";

	public static final int thresholdColor = 0xffd3d3d3; //0xffffcf21;//0xffd9d9d9; 

	private static int getMaxNumColor(int[] oriArray, int w) {
		int max = 0;
		int oriLen = oriArray.length;
		int[] array = new int[oriLen / 10 ];
		int step = w - w / 10;
		int len = w / 10;

		int j = 0;
		int k = 0;

		for(int i = step; i < oriLen; i += w) {
			for(j = i; j < i+len; j++) {
				array[k] = oriArray[j];
				k++;
			}
		}
		//Log.i(TAG, " oriArray.length = " +  oriArray.length);
		//Log.i(TAG, " array.length = " +  array.length);
		if(null != array && 0 < array.length) {
			
			HashMap <Integer, Integer> nums = new HashMap<Integer, Integer>();
			int temp = array[0];
			int count = 0;
			for(int i = array.length -1; i >= 0; i--) {
				if(temp == array[i]) {
					count ++;
				} else  {					
					if(nums.containsKey(temp)) {
						int value = nums.get(temp);
						nums.put(temp, count + value);						
					} else {
						nums.put(temp, count);
					}
					temp = array[i];
					count = 1;
				}
			}
			

			/*if(nums.containsKey(temp)) {
				int value = nums.get(temp);
				nums.put(temp, count + value);
			} else {
				nums.put(temp, count);
			}
			
			 List arrayList = new ArrayList<Map.Entry<Integer,Integer>>(nums.entrySet()); 
			Collections.sort(arrayList, new Comparator<Map.Entry<Integer, Integer>>() {   
	            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {   
	                return (o2.getValue() - o1.getValue());   
	            }   
			}); 
			int length = arrayList.size();
			
			Map.Entry<Integer,Integer> map = (Entry<Integer, Integer>) arrayList.get(0);
			Log.i(TAG, "color key = " + map.getKey());
			return map.getKey();*/

                    int maxValue = -1;
			int colorKey = 0xff000000;
			for(Entry<Integer, Integer> entry : nums.entrySet()) {
				int key = entry.getKey();
				int value = entry.getValue();

				if(value > maxValue) {
					maxValue = value;
					colorKey = key;
				}
			}			
			
			
			return colorKey;
		}
		return max;
	}

	public static class BgBitmap {
		public int color;
		public Bitmap bitmap;

		public BgBitmap(int c, Bitmap bg) {
			this.color = c;
			this.bitmap = bg;
		}
	}
	
	public static BgBitmap getWidgetBG(Resources rs, Bitmap bm, int maskBitmapId) {
		if(null == bm || bm.isRecycled()) {
			Log.i(TAG, "--------getBlurBitmap-----(null == bm || bm.isRecycled())--------------");
			return null;
		}
		Bitmap mask = BitmapFactory.decodeResource(rs, maskBitmapId);
		if(null == mask ) {
			return null;
		}
		
		int w = bm.getWidth();
		int h = bm.getHeight();
		int len = w * h;
		int[] srcColorArray = new int[len];

		int maskW = mask.getWidth();
		int maskH = mask.getHeight();
		int maskLen = maskW * maskH;

		bm.getPixels(srcColorArray, 0, w, 0, 0, w, h);		
		int color = getMaxNumColor(srcColorArray, w);
		if(0xffffff == color) {
			color = 0xff2f2f2f; //default color when color is white
		}
		srcColorArray = null;
				
		Bitmap bg = Bitmap.createBitmap(maskW, maskH, Config.ARGB_8888);	//background color bitmap
						
		Canvas canvas = new Canvas(bg);
				
		Paint sPaint = new Paint();
		sPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
		
		Rect rect = new Rect(0, 0, maskW, maskH);

		canvas.drawColor(color);
		
		canvas.save();
		canvas.translate(0, 0);
		canvas.drawBitmap(mask, rect, rect, sPaint);
		canvas.restore();
		
		mask.recycle();
		Log.i(TAG, "color = " + Integer.toHexString(color));
		BgBitmap mBgBitmap = new BgBitmap(color, bg);
		return mBgBitmap;
		//return bg;
	}

	public static Bitmap getBlurBitmap(Resources rs,Bitmap bm, int maskBitmapId) {
			if(null == bm || bm.isRecycled()) {
				Log.i(TAG, "--------getBlurBitmap-----(null == bm || bm.isRecycled())--------------");
				return null;
			}
			
			Paint sPaint = new Paint();
			sPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
			Bitmap mask = BitmapFactory.decodeResource(rs, maskBitmapId);
			int maskWidth = mask.getWidth();
			int maskHeight = mask.getHeight();
			
			Bitmap bbm = Bitmap.createBitmap(maskWidth, mask
					.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(bbm);


			//scale bitmap start
			int width = bm.getWidth();
			int height = bm.getHeight();
			float scaleWidth = (((float)maskWidth) / width);
			float scaleHeight = (((float)maskWidth) / height);
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,true);
			//scale bitmap end
			int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,
        			Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
        			| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
        			| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
        			| Canvas.CLIP_TO_LAYER_SAVE_FLAG);


			canvas.drawBitmap(newbm, new Rect(0, 0, maskWidth, maskWidth), new Rect(0, 0, maskWidth, 
								maskWidth), new Paint());
			
			
			Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			canvas.save();
			canvas.translate(0, 0);
			canvas.drawBitmap(mask, rect, rect, sPaint);
			canvas.restore();
			
			canvas.restoreToCount(sc);
			newbm.recycle();
			mask.recycle();
			return bbm;
	}

	public static final String ARTIST_FULL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+DownLoadAllPicsAsync.ARTIST_PATH;
	public static String getAtristName(String artistName) {	//get artist cover image `name from artist folder 
		if(null == artistName) {
			return null;
		}
		String fileName = artistName;
		String fileter = trimExtension(artistName);
		File artistFolder = new File(ARTIST_FULL_PATH);
		File[] files = artistFolder.listFiles();
		if(null == files) {
			return artistName;
		}
		for(File file : files) {
			String name = trimExtension(file.getName());

			if(name.indexOf(fileter)!=-1 || fileter.indexOf(name)!=-1) {
				fileName = name;
				
			}
		}
		
		if(artistName.indexOf(".jpg")!=-1 && fileName.indexOf(".jpg") == -1) {
			fileName = fileName + ".jpg";
		}
		
		return fileName;
	}

	public static String trimExtension(String filename) {   
	    if ((filename != null) && (filename.length() > 0)) {   
	        int i = filename.lastIndexOf('.');   
	        if ((i >-1) && (i < (filename.length()))) {   
	            return filename.substring(0, i);   
	        }   
	    }   
	    return filename;   
	}   
}

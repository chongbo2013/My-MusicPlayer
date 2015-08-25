package com.lewa.player;

import com.lewa.ExitApplication;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.ViewTreeObserver;




public class SleepModeManager implements OnSeekBarChangeListener{
    private static final String TAG = "SleepModeManager";

    public static final int DEFAULT_TIME = 29;
    private static final int MAX_TIME = 89;
    private TextView mPopupTextView;
    private SeekBar mSeekBar;
    private int mProgress;
    private int mSeekBarWidth;
    private int mLeftOffset;
    private Context mContext;
    private static Editor mEditor;
    public SleepModeManager(Context context) {
        mContext = context;
        //mSeekBarWidth = context.getResources().getDimensionPixelOffset(R.dimen.sleep_width);
//        mLeftOffset = context.getResources().getDimensionPixelOffset(R.dimen.left_offset);
//        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(context);
//        mEditor = sharePref.edit();

        final View localView = LayoutInflater.from(context).inflate(R.layout.sleep_mode_set, null);        
        mSeekBar = (SeekBar) localView.findViewById(R.id.sleep_seekbar);
        mPopupTextView = (TextView) localView.findViewById(R.id.sleep_text);
        
        mProgress = PreferenceManager.getDefaultSharedPreferences(context).getInt("sleep_mode_time", 0); 

        int progress = mProgress - 1;
        if(progress == -1) {
            progress = DEFAULT_TIME;
        }
        mSeekBar.setProgress(progress);
        mSeekBar.setMax(MAX_TIME);
        mSeekBar.setOnSeekBarChangeListener(this);        
        
        showSelectTimeDialog(localView, context);

        ViewTreeObserver viewTreeObserver = (ViewTreeObserver)localView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            public void onGlobalLayout() {
                localView.getViewTreeObserver().removeGlobalOnLayoutListener(this);


                    refreshPopUpTime();

            }
        });
    }    

    public void showSelectTimeDialog(View view, final Context context) {
        
        if(mProgress == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.sleep_tip);
            
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            	
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    int time = mProgress;
                    setSleepTime(context, time);
                    Intent intent = new Intent(MediaPlaybackService.SLEEP);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + time * 60 * 1000, pendingIntent);
                    /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
                    Context activity = ExitApplication.getTopActivity();
                    if(null == activity) {
                    	activity = context;
                    }
                    Toast.makeText(activity, context.getString(R.string.sleep_set_ok, time), Toast.LENGTH_SHORT).show();
                    /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
                    return;
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {                    

            	public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub                        
                }
            });
            
            builder.setView(view).create().show();
            refreshPopUpTime();
        } else {
            setSleepTime(context, 0);
            deleteSleepTime(context);
            /*Added by ruiwei, for Modifying Toast style, 20150211, start*/
            Context activity = ExitApplication.getTopActivity();
            if(null == activity) {
            	activity = context;
            }
            Toast.makeText(activity, context.getString(R.string.sleep_close_ok), Toast.LENGTH_SHORT).show();
            /*Added by ruiwei, for Modifying Toast style, 20150211, end*/
        }
    }
    
    public static void setSleepTime(Context context, int time) {
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = sharePref.edit();
        mEditor.putInt("sleep_mode_time", time);
        mEditor.commit();    
    }
    
    public static void deleteSleepTime(Context context) {
        Intent intent = new Intent(MediaPlaybackService.SLEEP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        setSleepTime(context, 0);
    }
    
    public void refreshPopUpTime() {
        mProgress = mSeekBar.getProgress() + 1;
        
        mPopupTextView.setText(mContext.getString(R.string.sleep_set, mProgress));
        
        LinearLayout.LayoutParams localLayoutParams = 
                (LinearLayout.LayoutParams) mPopupTextView.getLayoutParams();
        mSeekBarWidth = mSeekBar.getWidth() - mSeekBar.getPaddingLeft() - mSeekBar.getPaddingRight();
        int i = mProgress;
        float thumbLeft = (float) ((mProgress-1) * mSeekBarWidth / MAX_TIME);
        


        int[] seekBarLoc = new int[2];
        mSeekBar.getLocationInWindow(seekBarLoc);
        int seekBarLeftMargin = seekBarLoc[0];
        int halfpopTextViewWidth = mPopupTextView.getWidth() / 2;
        
        
        int popTextViewMargin = seekBarLeftMargin - halfpopTextViewWidth;

        localLayoutParams.leftMargin= (int)(popTextViewMargin + thumbLeft + mSeekBar.getThumbOffset());
            
        mPopupTextView.setLayoutParams(localLayoutParams);
    }
    
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        // TODO Auto-generated method stub
        refreshPopUpTime();
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    //    refreshPopUpTime();
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }

}

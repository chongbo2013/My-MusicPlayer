package com.lewa.player.activity;
import android.util.Log;
//import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

import com.lewa.ExitApplication;
import com.lewa.player.R;
import com.lewa.view.VerticalSeekBar;

import java.util.ArrayList;

//import android.view.Window;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ListView;
//import android.widget.TextView;

import android.os.RemoteException;
import android.preference.PreferenceManager;
import lewa.support.v7.app.ActionBar;
import lewa.support.v7.app.ActionBarActivity;
import lewa.support.v7.app.ActionBar.Tab;
import lewa.support.v7.app.ActionBar.TabListener;
import lewa.support.v7.app.ActionBar.LayoutParams;
import lewa.support.v7.view.ActionMode;
import lewa.support.v7.view.ActionMode.Callback;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
public class MusicEQActivity extends ActionBarActivity {

	private Spinner eqSpinner;
	private AudioManager mAudioManager;
	private SeekBar volSeekBar;
	private VerticalSeekBar[] freqGainSeekBars = new VerticalSeekBar[5];

	private Button msureBtn;
    private Button mcanslBtn;
	private SharedPreferences musicSettings;
	private Editor prefsPrivateEditor;	
	private ArrayList<Short[]> genre = new ArrayList<Short[]>();
	private String[] genresName = null;
	int currentSelectedItem = 0;
	int lastSelectedItem = 0;
	int mPreEqchoise = 0;
	private int[] verticalSeekBarIds = new int[]{R.id.lowerSeekbar, R.id.lowSeekbar, R.id.middleSeekbar, R.id.highSeekbar, R.id.higherSeekbar};
	short[] userSettingLevel = new short[5];
    private Short[][] defaultGenres = {
    						  {0, 0, 0, 0, 0},
    						  {450, 0, 0, 0, 450},
    						  {750, 450, -300, 600, 600},
    						  {900, 0, 300, 600, 150},
    						  {450, 0, 0, 300, -150},
    						  {600, 150, 1350, 450, 0},
    						  {750, 450, 0, 150, 450},
    						  {600, 300, -300, 300, 750},
    						  {-150, 300, 750, 150, -300},
    						  {750, 450, -150, 450, 750},
    						  {0, 0, 0, 0, 0}};
    

    Short [] lastUserLevels = new Short [5];
    Short [] tempLevels = new Short [5];
    boolean isCustom = false;
    public static final String ACTION_UPDATE_EQ = "com.lewa.player.EQUPDATE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.music_eq);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setTitle(getString(R.string.EQ_title_text));
		musicSettings = this.getSharedPreferences("Music_setting", 0);
		prefsPrivateEditor = musicSettings.edit();
		currentSelectedItem = musicSettings.getInt("whicheq", 0);
		lastSelectedItem = currentSelectedItem;
		mPreEqchoise = currentSelectedItem;
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		eqSpinner = (Spinner) findViewById(R.id.eqChoose);
        String[] arrays=this.getResources().getStringArray(R.array.eq_genres);
        ArrayAdapter<CharSequence> adapter =  new ArrayAdapter(this, com.lewa.internal.R.layout.v5_simple_spinner_item,android.R.id.text1,arrays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eqSpinner.setAdapter(adapter);
        eqSpinner.setSelection(currentSelectedItem);
        eqSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
        
		volSeekBar = (SeekBar) findViewById(R.id.volSetSeekbar);
		setVolum();		
        volSeekBar.setOnSeekBarChangeListener(volChangeListener);
        new Thread(new volThread()).start();
		
		for(int i=0; i< 5; i++) {
			freqGainSeekBars[i] = (VerticalSeekBar) findViewById(verticalSeekBarIds[i]);
			freqGainSeekBars[i].setOnTouchListener(verticalSeekbarTouchListener);
		}
		msureBtn = (Button) findViewById(R.id.okayButton);
		mcanslBtn = (Button) findViewById(R.id.cancelButton);
		msureBtn.setOnClickListener(buttonClickListener);
		mcanslBtn.setOnClickListener(buttonClickListener);
		genresName = getResources().getStringArray(R.array.eq_genres);

		initGenres();

		ExitApplication exit = (ExitApplication) getApplication();
        exit.addActivity(this);
	}
	
	private View.OnTouchListener verticalSeekbarTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {        	
            int viewId = view.getId();
            Log.i("VVV", "viewId is" + viewId);
            for(int i=0; i<5; i++){
            	Log.i("VVV", "verticalSeekBarIds[" + i + "] :" + verticalSeekBarIds[i]);	
            	if(verticalSeekBarIds[i] == viewId) {
            		int progress = freqGainSeekBars[i].getProgress();
                    short value = (short) getGainValueFromProgress(progress);
                    Log.i("VVV", "new value is" + value +" old is " + defaultGenres[currentSelectedItem][i]);
                    if(value != defaultGenres[currentSelectedItem][i]) {
                    	if(currentSelectedItem != defaultGenres.length - 1) {//if not user settings item, change it to user settings item
	                    	for(int j=0; j<5; j++) {// change the value of user setting item to those of default item
	                    		defaultGenres[defaultGenres.length - 1][j] = defaultGenres[currentSelectedItem][j];
	                    	}
	                    	defaultGenres[defaultGenres.length - 1][i] = defaultGenres[currentSelectedItem][i];
	                    	//notify spinner to change selected item
	                    	currentSelectedItem = defaultGenres.length - 1;
	                    	eqSpinner.setSelection(genresName.length - 1);
                    	} else {
                    		defaultGenres[currentSelectedItem][i] = defaultGenres[currentSelectedItem][i];
                    		tempLevels[i] = value;
                        	updateEq();
                    	}
                    }

                    return false;
            	}
            }

            return false;
        }
    };
	
	Handler volHandler = new Handler(){
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case 1:  
                setVolum();  
                break;
           }
        }
    };
    
    private void setVolum() {        
        volSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int pro = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volSeekBar.setProgress(pro);
    }     
    
    class volThread implements Runnable {   
        public void run() {  
            while (!Thread.currentThread().isInterrupted()) {

                Message message = new Message();
                message.what = 1;
                volHandler.sendMessage(message);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
	
    View.OnClickListener buttonClickListener = new View.OnClickListener(){

		public void onClick(View v) {
			if (v.getId() == R.id.okayButton) {
				if(currentSelectedItem == defaultGenres.length -1) {
					prefsPrivateEditor.putInt("LowerEQ", tempLevels[0]);
					prefsPrivateEditor.putInt("LowEQ", tempLevels[1]);
					prefsPrivateEditor.putInt("MiddleEQ", tempLevels[2]);
					prefsPrivateEditor.putInt("HighEQ", tempLevels[3]);
					prefsPrivateEditor.putInt("HigherEQ", tempLevels[4]);
				}
				prefsPrivateEditor.putInt("whicheq", currentSelectedItem);
				prefsPrivateEditor.commit();
				
				updateEq();				
			} else if (v.getId() == R.id.cancelButton) {
				restoreEq();				
			}
			
			finish();
		}
		
	};
	
	public void updateEq() {
	    Intent intent = new Intent();
        intent.putExtra("levles", setEqAsString(tempLevels));
        intent.setAction(ACTION_UPDATE_EQ);        
        sendBroadcast(intent);
	}
	
	public void restoreEq() {
	    Intent intent = new Intent();
        intent.putExtra("levles", setEqAsString(lastUserLevels));
        intent.setAction(ACTION_UPDATE_EQ);
        sendBroadcast(intent);
	}
	
	private String setEqAsString(Short[] levels) {
	    String eqString = "";
	    
	    for(int i=0; i<5; i++) {
	        eqString += levels[i].toString();
	        if(i < 4) {
	            eqString += ";";
	        }
	    }
	    
	    return eqString;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		restoreEq();
	}
	
	private static final short FREQUENCY_GAIN_MIN_VALUE = -1500;
	private static final short FREQUENCY_GAIN_MAX_VALUE = 1500;
	
	public int getProgressFromGainValue(int value) {
		return 100 * (value - FREQUENCY_GAIN_MIN_VALUE) / (FREQUENCY_GAIN_MAX_VALUE - FREQUENCY_GAIN_MIN_VALUE);
	}
	
	public int getGainValueFromProgress(int progress) {
		return (FREQUENCY_GAIN_MAX_VALUE -  FREQUENCY_GAIN_MIN_VALUE) * progress /100 + FREQUENCY_GAIN_MIN_VALUE;
	}
	
	private OnItemSelectedListener spinnerItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	
            currentSelectedItem = position;
            //if change from user settings to other default item, restore the modified user settings value
            if(lastSelectedItem == defaultGenres.length -1 && currentSelectedItem != lastSelectedItem) {
            	for(int i=0; i<5; i++) {
            		defaultGenres[lastSelectedItem][i] = userSettingLevel[i];
            	}
            }
            lastSelectedItem = position;
            
            for(int i=0; i<5; i++){
            	tempLevels[i] = defaultGenres[position][i];
        		freqGainSeekBars[i].setProgress(getProgressFromGainValue(defaultGenres[position][i]));
        		freqGainSeekBars[i].doAfterSetProgress();
        	}
            
            updateEq();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            
        }
    };
	
	private void initGenres() {
		userSettingLevel[0] = (short) musicSettings.getInt("LowerEQ", 0);
		userSettingLevel[1] = (short) musicSettings.getInt("LowEQ", 0);
		userSettingLevel[2] = (short) musicSettings.getInt("MiddleEQ", 0);
		userSettingLevel[3] = (short) musicSettings.getInt("HighEQ", 0);
		userSettingLevel[4] = (short) musicSettings.getInt("HigherEQ", 0);
		
		for(int i=0; i<5; i++) {
			defaultGenres[defaultGenres.length-1][i] = userSettingLevel[i];//load user item value
			lastUserLevels[i]  = defaultGenres[currentSelectedItem][i];//load last selected value
			tempLevels[i] = lastUserLevels[i]; //backup last value into temp buffer
			freqGainSeekBars[i].setProgress(getProgressFromGainValue(lastUserLevels[i]));
			freqGainSeekBars[i].doAfterSetProgress();
			
		}
	}
	
	OnSeekBarChangeListener volChangeListener = new OnSeekBarChangeListener() {
	    @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
	        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }
	    
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
        
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        
    };

    @Override
    protected void onDestroy() {
        isCustom = false;
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.removeActivity(this);
        super.onDestroy();
    } 
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return false;
    }
}

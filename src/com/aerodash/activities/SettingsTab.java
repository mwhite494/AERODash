package com.aerodash.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.aerodash.R;
import com.aerodash.interfaces.Constants;
import com.aerodash.interfaces.GPSCallback;
import com.aerodash.managers.GPSManager;

public class SettingsTab extends Activity implements OnItemSelectedListener, GPSCallback {
	private GPSManager gpsManager = null;
	Button maindashbtn, drivercambtn, settingsbtn, restoreDefaultBtn;
	String strSpeedUnit;
	private double newLat, newLong, oldLat, oldLong, oldSpeed, avgSpeed, distanceSum;
    private double speed = 0.0;
    private int updateCount, measurement_index, roundedSpeed, seconds, minutes, hours, oldSeconds, oldMinutes, oldHours;
    private double[] avgSpeedResults = new double[1];
    private double[] distResults = new double[2];
	private EditText editPictureSetting, editVideoSettingSpeed, editVideoSettingMin, editVideoSettingSec;
	private LinearLayout linearlayout_focus;
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	CheckBox checkPictureSetting, checkVideoSetting, checkEverythingAEROfont;
	Chronometer stopWatch;
    private long startTime;
    TextView txtPictureUnit, txtVideoUnit, staticPictureSetting, staticVideoSetting, staticVideoSettingMin, staticVideoSettingSec;
    RadioButton whiteRadio, redRadio, orangeRadio, yellowRadio, greenRadio, blueRadio, purpleRadio;
    Spinner speed_measure_spinner;
    TextView staticSettingsMainDash, staticSettingsDriverCam, staticSettingsMisc, staticSpeedMeasure;
    TextView staticColorScheme, staticEverythingText, staticFontText;
    Typeface squealer;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_settings_tab);
		setupUI(findViewById(R.id.parent));
				
		maindashbtn = (Button)findViewById(R.id.maindashbtn);
		maindashbtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					startActivity(new Intent(getApplicationContext(),MainDash.class));
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
				}
				return false;
			}
		});
		
		drivercambtn = (Button)findViewById(R.id.drivercambtn);
		drivercambtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					startActivity(new Intent(getApplicationContext(),DriverCam.class));
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
				}
				return false;
			}
		});
		//Retrieve the current preferences
		sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
		strSpeedUnit = sharedPreferences.getString("SpeedUnit", "mph");
		txtPictureUnit = (TextView) findViewById(R.id.picture_setting_measure);
		txtVideoUnit = (TextView) findViewById(R.id.video_setting_measure);
		if(strSpeedUnit.equals("mph")){
			measurement_index = 1;
			txtPictureUnit.setText("mph is reached");
			txtVideoUnit.setText("mph is reached, for");
		}
		else {
			measurement_index = 0;
			txtPictureUnit.setText("km/h is reached");
			txtVideoUnit.setText("km/h is reached, for");
		}
		
		settingsbtn = (Button) findViewById(R.id.settingsbtn);
		restoreDefaultBtn = (Button) findViewById(R.id.restore_default_btn);
		speed_measure_spinner = (Spinner) findViewById(R.id.speed_measure_spinner);
		checkPictureSetting = (CheckBox) findViewById(R.id.check_picture_setting);
        checkVideoSetting = (CheckBox) findViewById(R.id.check_video_setting);
        checkEverythingAEROfont = (CheckBox) findViewById(R.id.everything_aero_font);
        editPictureSetting = (EditText)findViewById(R.id.picture_setting_edit);
		editVideoSettingSpeed = (EditText)findViewById(R.id.video_setting_edit);
		editVideoSettingMin = (EditText)findViewById(R.id.video_setting_time_min);
		editVideoSettingSec = (EditText)findViewById(R.id.video_setting_time_sec);
		staticSettingsMainDash = (TextView) findViewById(R.id.settings_main_dash);
		staticSettingsDriverCam = (TextView) findViewById(R.id.settings_driver_cam);
		staticSettingsMisc = (TextView) findViewById(R.id.settings_misc);
		staticSpeedMeasure = (TextView) findViewById(R.id.speed_measure);
		staticPictureSetting = (TextView) findViewById(R.id.picture_setting);
		staticVideoSetting = (TextView) findViewById(R.id.video_setting);
		staticVideoSettingMin = (TextView) findViewById(R.id.video_setting_min);
		staticVideoSettingSec = (TextView) findViewById(R.id.video_setting_sec);
		staticColorScheme = (TextView) findViewById(R.id.color_scheme);
		staticEverythingText = (TextView) findViewById(R.id.txt_everything);
		staticFontText = (TextView) findViewById(R.id.txt_font);
		
		whiteRadio = (RadioButton) findViewById(R.id.white_button);
		redRadio = (RadioButton) findViewById(R.id.red_button);
		orangeRadio = (RadioButton) findViewById(R.id.orange_button);
		yellowRadio = (RadioButton) findViewById(R.id.yellow_button);
		greenRadio = (RadioButton) findViewById(R.id.green_button);
		blueRadio = (RadioButton) findViewById(R.id.blue_button);
		purpleRadio = (RadioButton) findViewById(R.id.purple_button);
		
		if (sharedPreferences.getString("colorSchemeActive", "blue").equals("white")) {
			whiteRadio.setChecked(true);
			setSchemeWhite();
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("red")) {
			redRadio.setChecked(true);
			setSchemeRed();
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("orange")) {
			orangeRadio.setChecked(true);
			setSchemeOrange();
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("yellow")) {
			yellowRadio.setChecked(true);
			setSchemeYellow();
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("green")) {
			greenRadio.setChecked(true);
			setSchemeGreen();
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("blue")) {
			blueRadio.setChecked(true);
			setSchemeBlue();
		}
		else {
			purpleRadio.setChecked(true);
			setSchemePurple();
		}
		
		// Create the AERO font
		squealer = Typeface.createFromAsset(getAssets(),"fonts/squealer.ttf");
		
		if(sharedPreferences.getBoolean("checkEverythingAEROfont", false) == true) {
			setTypefaceSquealer();
		}
		
		// Set on touch listeners for the color scheme radio group
		whiteRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					whiteRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "white");
					editor.commit();
					setSchemeWhite();
				}
				return false;
			}
		});
		
		redRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					redRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "red");
					editor.commit();
					setSchemeRed();
				}
				return false;
			}
		});
		
		orangeRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					orangeRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "orange");
					editor.commit();
					setSchemeOrange();
				}
				return false;
			}
		});
		
		yellowRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					yellowRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "yellow");
					editor.commit();
					setSchemeYellow();
				}
				return false;
			}
		});
		
		greenRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					greenRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "green");
					editor.commit();
					setSchemeGreen();
				}
				return false;
			}
		});
		
		blueRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					blueRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "blue");
					editor.commit();
					setSchemeBlue();
				}
				return false;
			}
		});
		
		purpleRadio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					purpleRadio.setChecked(true);
					editor = sharedPreferences.edit();
					editor.putString("colorSchemeActive", "purple");
					editor.commit();
					setSchemePurple();
				}
				return false;
			}
		});
		
		// Set on check changed listener for the everything AERO font checkbox
		checkEverythingAEROfont.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if ( isChecked ) {
		            setTypefaceSquealer();
		        }
		        else {
		        	setTypefaceDefault();
		        }
		    }
		});
		
		stopWatch = (Chronometer) findViewById(R.id.chrono_settings);
		
		// Create an ArrayAdapter using the string array and a default spinner layout
    	ArrayAdapter<CharSequence> speed_measure_adapter = ArrayAdapter.createFromResource(this,
    	R.array.speed_units, R.layout.spinner_layout);
    	// Specify the layout to use when the list of choices appears
    	speed_measure_adapter.setDropDownViewResource(R.layout.spinner_layout);
    	// Apply the adapter to the spinner
    	speed_measure_spinner.setAdapter(speed_measure_adapter);
		// Set listener for the spinner
		speed_measure_spinner.setOnItemSelectedListener(this);
		//Set the speed_measure_spinner to the last saved selection
		speed_measure_spinner.setSelection(getIndex(speed_measure_spinner,"   "+strSpeedUnit), true);
		
		TextView txt_aero = (TextView)findViewById(R.id.txt_aero); 
		txt_aero.setTypeface(squealer);
		
		linearlayout_focus = (LinearLayout)findViewById(R.id.linearlayout_focus);
		
		gpsManager = new GPSManager();
        
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);
        
        updateCount = sharedPreferences.getInt("updateCount", 0);
        oldLat = Double.parseDouble(sharedPreferences.getString("strOldLat", "0.0"));
        oldLong = Double.parseDouble(sharedPreferences.getString("strOldLong", "0.0"));
        oldSpeed = Double.parseDouble(sharedPreferences.getString("strOldSpeed", "0.0"));
        avgSpeed = Double.parseDouble(sharedPreferences.getString("strAvgSpeed", "0.0"));
        distanceSum = Double.parseDouble(sharedPreferences.getString("strDistanceSum", "0.0"));
        
        if(sharedPreferences.getBoolean("checkPictureSetting", false) == true) {
        	checkPictureSetting.setChecked(true);
        }
        else {
        	checkPictureSetting.setChecked(false);
        	editPictureSetting.setEnabled(false);
        }
        if(sharedPreferences.getBoolean("checkVideoSetting", false) == true) {
        	checkVideoSetting.setChecked(true);
        }
        else {
        	checkVideoSetting.setChecked(false);
        	editVideoSettingSpeed.setEnabled(false);
            editVideoSettingMin.setEnabled(false);
            editVideoSettingSec.setEnabled(false);
        }
        if(sharedPreferences.getBoolean("checkEverythingAEROfont", false) == true) {
        	checkEverythingAEROfont.setChecked(true);
        }
        else {
        	checkEverythingAEROfont.setChecked(false);
        }
        editPictureSetting.setText("" + sharedPreferences.getInt("editPictureSetting", 0));
        editVideoSettingSpeed.setText("" + sharedPreferences.getInt("editVideoSettingSpeed", 0));
        editVideoSettingMin.setText("" + sharedPreferences.getInt("editVideoSettingMin", 0));
        editVideoSettingSec.setText("" + sharedPreferences.getInt("editVideoSettingSec", 0));
        // When done is pressed the edit text focus is cleared and the keyboard is hidden
        editPictureSetting.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	hideSoftKeyboard(editPictureSetting);
                	editPictureSetting.clearFocus();
                }
                return false;
            }
        });
        editVideoSettingSpeed.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	hideSoftKeyboard(editVideoSettingSpeed);
                	editVideoSettingSpeed.clearFocus();
                }
                return false;
            }
        });
        editVideoSettingMin.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	hideSoftKeyboard(editVideoSettingMin);
                	editVideoSettingMin.clearFocus();
                }
                return false;
            }
        });
        editVideoSettingSec.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                	hideSoftKeyboard(editVideoSettingSec);
                	editVideoSettingSec.clearFocus();
                }
                return false;
            }
        });
        // Save the value when focus changes (value entered)
        // If value is empty set the value to 0
        editPictureSetting.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                	editor = sharedPreferences.edit();
                	if (editPictureSetting.getText().toString().equals("") == true) {
                		editPictureSetting.setText("0");
                	}
                	editor.putInt("editPictureSetting", Integer.parseInt(editPictureSetting.getText().toString()));
                	editor.commit();
                }
            }
        });
        editVideoSettingSpeed.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                	editor = sharedPreferences.edit();
                	if (editVideoSettingSpeed.getText().toString().equals("") == true) {
                		editVideoSettingSpeed.setText("0");
                	}
                	editor.putInt("editVideoSettingSpeed", Integer.parseInt(editVideoSettingSpeed.getText().toString()));
                	editor.commit();
                }
            }
        });
        editVideoSettingMin.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                	editor = sharedPreferences.edit();
                	if (editVideoSettingMin.getText().toString().equals("") == true) {
                		editVideoSettingMin.setText("0");
                	}
                	editor.putInt("editVideoSettingMin", Integer.parseInt(editVideoSettingMin.getText().toString()));
                	editor.commit();
                }
            }
        });
        editVideoSettingSec.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                	editor = sharedPreferences.edit();
                	if (editVideoSettingSec.getText().toString().equals("") == true) {
                		editVideoSettingSec.setText("0");
                	}
                	editor.putInt("editVideoSettingSec", Integer.parseInt(editVideoSettingSec.getText().toString()));
                	editor.commit();
                }
            }
        });
        
        // Restore default button
        restoreDefaultBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		speed_measure_spinner.setSelection(getIndex(speed_measure_spinner,"   "+strSpeedUnit), true);
        		editPictureSetting.setText("0");
        		editVideoSettingSpeed.setText("0");
        		editVideoSettingMin.setText("0");
        		editVideoSettingSec.setText("0");
        		editPictureSetting.setEnabled(false);
        		editVideoSettingSpeed.setEnabled(false);
        		editVideoSettingMin.setEnabled(false);
        		editVideoSettingSec.setEnabled(false);
        		checkPictureSetting.setChecked(false);
        		checkVideoSetting.setChecked(false);
        		checkEverythingAEROfont.setChecked(false);
        		blueRadio.setChecked(true);
        		setSchemeBlue();
        		editor = sharedPreferences.edit();
        		editor.putBoolean("checkPictureSetting", false);
        		editor.putBoolean("checkVideoSetting", false);
        		editor.putBoolean("checkEverythingAEROfont", false);
        		editor.putInt("editPictureSetting", 0);
        		editor.putInt("editVideoSettingSpeed", 0);
        		editor.putInt("editVideoSettingMin", 0);
        		editor.putInt("editVideoSettingSec", 0);
        		editor.putString("colorSchemeActive", "blue");
        		editor.commit();
        	}
        });
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	@Override
	public void onGPSUpdate(Location location) {
		editor = sharedPreferences.edit();
		
		newLat = location.getLatitude();
        newLong = location.getLongitude();
        speed = location.getSpeed();
        
        roundedSpeed = double2Int(convertSpeed(speed));
        if(roundedSpeed == 0) {
        	stopWatch.stop();
        	if(updateCount == 0) {
    			seconds = 0;
        		minutes = 0;
        		hours = 0;
        		oldSeconds = 0;
        		oldMinutes = 0;
        		oldHours = 0;
        		editor = sharedPreferences.edit();
    			editor.putInt("oldSeconds", oldSeconds);
    			editor.putInt("oldMinutes", oldMinutes);
    			editor.putInt("oldHours", oldHours);
    			editor.putInt("seconds", seconds);
    			editor.putInt("minutes", minutes);
    			editor.putInt("hours", hours);
    			editor.commit();
    		}
    		else {
    			startTime = SystemClock.elapsedRealtime();
    			oldSeconds = sharedPreferences.getInt("seconds", 0);
    			oldMinutes = sharedPreferences.getInt("minutes", 0);
    			oldHours = sharedPreferences.getInt("hours", 0);
    			editor = sharedPreferences.edit();
    			editor.putLong("startTime", startTime);
    			editor.putInt("oldSeconds", oldSeconds);
    			editor.putInt("oldMinutes", oldMinutes);
    			editor.putInt("oldHours", oldHours);
    			editor.commit();
    		}
        }
        else {
        	if(updateCount == 0) {
    			startTime = SystemClock.elapsedRealtime();
    			editor = sharedPreferences.edit();
    			editor.putLong("startTime", startTime);
    			editor.commit();
    		}
    		else {
    			startTime = sharedPreferences.getLong("startTime", SystemClock.elapsedRealtime());
    			oldSeconds = sharedPreferences.getInt("oldSeconds", 0);
    			oldMinutes = sharedPreferences.getInt("oldMinutes", 0);
    			oldHours = sharedPreferences.getInt("oldHours", 0);
    		}
    		stopWatch.setOnChronometerTickListener(new OnChronometerTickListener(){
    			@Override
    			public void onChronometerTick(Chronometer arg0) {
    				seconds = (((int) (SystemClock.elapsedRealtime() - startTime)) / 1000) + oldSeconds;
    				minutes = seconds / 60 + oldMinutes;
    				hours = minutes / 60 + oldHours;
    				seconds = seconds % 60;
    				minutes = minutes % 60;
    				editor = sharedPreferences.edit();
        			editor.putInt("seconds", seconds);
        			editor.putInt("minutes", minutes);
        			editor.putInt("hours", hours);
        			editor.commit();
    			}
    		});
    		stopWatch.start();
        }
        
        oldSpeed = updateMaxSpeed(speed,oldSpeed);
        avgSpeedResults = updateAvgSpeed(speed,updateCount,avgSpeed);
        avgSpeed = avgSpeedResults[0];
        updateCount = (int) avgSpeedResults[1];
        distResults = updateDistanceSum(oldLat,oldLong,newLat,newLong,distanceSum);
        oldLat = distResults[0];
        oldLong = distResults[1];
        distanceSum = distResults[2];
        
        editor.putInt("updateCount", updateCount);
        editor.putString("strOldLat", Double.toString(oldLat));
        editor.putString("strOldLong", Double.toString(oldLong));
        editor.putString("strOldSpeed", Double.toString(oldSpeed));
        editor.putString("strAvgSpeed", Double.toString(avgSpeed));
        editor.putString("strDistanceSum", Double.toString(distanceSum));
        editor.commit();
	}
	
	@Override
	protected void onDestroy() {
        super.onDestroy();
    	gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
            
        gpsManager = null;
        
        stopWatch.stop();
    }
	
	@Override
	protected void onResume()
	{
	    super.onResume();

	    //Do not give the EditText focus automatically when activity starts
	    editPictureSetting.clearFocus();
	    linearlayout_focus.requestFocus();
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
		String speed_unit = parent.getItemAtPosition(pos).toString();
		if(speed_unit.equals("   mph")) {
			if (sharedPreferences.getString("SpeedUnit", "mph").equals("km/h")) {
				editPictureSetting.setText("" + double2Int(convertCamSpeed(Integer.parseInt(editPictureSetting.getText().toString()), 1)));
				editVideoSettingSpeed.setText("" + double2Int(convertCamSpeed(Integer.parseInt(editVideoSettingSpeed.getText().toString()), 1)));
			}
			SavePreferences("SpeedUnit","mph");
			SavePreferences("DistanceUnit","mi");
			txtPictureUnit.setText("mph is reached");
			txtVideoUnit.setText("mph is reached, for");
		}
		else {
			SavePreferences("SpeedUnit","km/h");
			SavePreferences("DistanceUnit","km");
			txtPictureUnit.setText("km/h is reached");
			txtVideoUnit.setText("km/h is reached, for");
			editPictureSetting.setText("" + double2Int(convertCamSpeed(Integer.parseInt(editPictureSetting.getText().toString()), 0)));
			editVideoSettingSpeed.setText("" + double2Int(convertCamSpeed(Integer.parseInt(editVideoSettingSpeed.getText().toString()), 0)));
		}
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }
	
    //Finds the index of a string in a certain spinner
    private int getIndex(Spinner spinner, String string){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(string)){
                index = i;
            }
        }
        return index;
    }
    
    //Saves preferences so that they can be accessed by different activities
    private void SavePreferences(String key, String value){
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
       }
    
    public double convertSpeed(double speed){
        return ((speed * Constants.hour_multiplier) * Constants.unit_multipliers[measurement_index]); 
	}
    
    public double convertCamSpeed(int currentSetting, int cam_measurement_index) {
    	double current = (double) currentSetting;
    	return (current * Constants.cam_unit_multipliers[cam_measurement_index]);
    }
    
    public int double2Int(double value)
	{
		double valueAbs = Math.abs(value);
		int output = (int) valueAbs;
		double result = valueAbs - (double) output;
		if(result<.5){
		return value<0 ? -output : output;
		}
		else{
		return value<0 ? -(output+1) : output+1;
		}
	}
    
    // Collection of functions to update main dash info when driving outside the activity
    public double updateMaxSpeed(double newSpeed, double oldSpeed){
		double returnSpeed;
		if(newSpeed>oldSpeed){
			returnSpeed = newSpeed;
			return returnSpeed;
		}
		else{
			returnSpeed = oldSpeed;
			return returnSpeed;
		}
	}
    
    public double[] updateAvgSpeed(double newSpeed, int count, double avgSpeed){
		double returnSpeed;
		if(roundedSpeed == 0){
			double newCount = (double) count;
			returnSpeed = avgSpeed;
			double[] results = {returnSpeed, newCount};
			return results;
		}
		else{
			count++;
			double newCount = (double) count;
			returnSpeed = (newSpeed + (avgSpeed * (newCount - 1))) / newCount;
			double[] results = {returnSpeed, newCount};
			return results;
		}
	}
    
    public double[] updateDistanceSum(double startLatitude, double startLongitude, double endLatitude, double endLongitude, double distanceSum){
		if(Double.isNaN(startLatitude)==true||Double.isNaN(startLongitude)==true){
			double oldLat, oldLong;
			oldLat = endLatitude;
			oldLong = endLongitude;
			
			double[] results = {oldLat, oldLong, distanceSum};
			return results;
		}
		else{
			Location startLocation = new Location("start point");
			startLocation.setLatitude(startLatitude);
			startLocation.setLongitude(startLongitude);
			Location endLocation = new Location("end point");
			endLocation.setLatitude(endLatitude);
			endLocation.setLongitude(endLongitude);
		
			double distance = startLocation.distanceTo(endLocation);
			distanceSum = distanceSum + distance;
		
			double oldLat, oldLong;
			oldLat = endLatitude;
			oldLong = endLongitude;
		
			double[] results = {oldLat, oldLong, distanceSum};
			return results;
		}
	}
    
    // Custom check clicked method to create a check box group where only one can be active but all can be inactive
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        
    	editor = sharedPreferences.edit();
        
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.check_picture_setting:
                if (checked) {
                	checkVideoSetting.setChecked(false);
                	editPictureSetting.setEnabled(true);
                	editVideoSettingSpeed.setEnabled(false);
                    editVideoSettingMin.setEnabled(false);
                    editVideoSettingSec.setEnabled(false);
                	editor.putBoolean("checkPictureSetting", true);
                	editor.putBoolean("checkVideoSetting", false);
                	editor.commit();
                }
                else {
                	editPictureSetting.setEnabled(false);
                	editor.putBoolean("checkPictureSetting", false);
                	editor.commit();
                }
                break;
            case R.id.check_video_setting:
                if (checked) {
                    checkPictureSetting.setChecked(false);
                    editVideoSettingSpeed.setEnabled(true);
                    editVideoSettingMin.setEnabled(true);
                    editVideoSettingSec.setEnabled(true);
                    editPictureSetting.setEnabled(false);
                    editor.putBoolean("checkVideoSetting", true);
                    editor.putBoolean("checkPictureSetting", false);
                    editor.commit();
                }
                else {
                	editVideoSettingSpeed.setEnabled(false);
                    editVideoSettingMin.setEnabled(false);
                    editVideoSettingSec.setEnabled(false);
                    editor.putBoolean("checkVideoSetting", false);
                    editor.commit();
                }
                break;
            case R.id.everything_aero_font:
            	if (checked) {
            		editor.putBoolean("checkEverythingAEROfont", true);
            		editor.commit();
            	}
            	else {
            		editor.putBoolean("checkEverythingAEROfont", false);
            		editor.commit();
            	}
        }
    }
    
    // Method to hide the keyboard
    public void hideSoftKeyboard(EditText myEditText) {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
    }
    
    // Hides the keyboard
    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(editPictureSetting);
                    editPictureSetting.clearFocus();
                    hideSoftKeyboard(editVideoSettingSpeed);
                    editVideoSettingSpeed.clearFocus();
                    hideSoftKeyboard(editVideoSettingMin);
                    editVideoSettingMin.clearFocus();
                    hideSoftKeyboard(editVideoSettingSec);
                    editVideoSettingSec.clearFocus();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }
    
    public void setSchemeWhite() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_white);
		drivercambtn.setBackgroundResource(R.drawable.unselected_white);
		settingsbtn.setBackgroundResource(R.drawable.selected_white);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_white);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_white);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_white);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_white);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_white);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_white);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_white);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_white);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_white);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.white));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.white));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.white));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.light_gray));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.light_gray));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.light_gray));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.light_gray));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.light_gray));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.light_gray));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.light_gray));
		staticColorScheme.setTextColor(getResources().getColor(R.color.light_gray));
		staticEverythingText.setTextColor(getResources().getColor(R.color.light_gray));
		staticFontText.setTextColor(getResources().getColor(R.color.light_gray));
    }
    
    public void setSchemeRed() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_red);
		drivercambtn.setBackgroundResource(R.drawable.unselected_red);
		settingsbtn.setBackgroundResource(R.drawable.selected_red);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_red);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_red);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_red);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_red);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_red);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_red);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_red);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_red);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_red);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.red));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.red));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.red));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.peach));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.peach));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.peach));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.peach));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.peach));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.peach));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.peach));
		staticColorScheme.setTextColor(getResources().getColor(R.color.peach));
		staticEverythingText.setTextColor(getResources().getColor(R.color.peach));
		staticFontText.setTextColor(getResources().getColor(R.color.peach));
    }
    
    public void setSchemeOrange() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_orange);
		drivercambtn.setBackgroundResource(R.drawable.unselected_orange);
		settingsbtn.setBackgroundResource(R.drawable.selected_orange);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_orange);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_orange);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_orange);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_orange);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_orange);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_orange);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_orange);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_orange);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_orange);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.orange));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.orange));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.orange));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.light_orange));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.light_orange));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.light_orange));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.light_orange));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.light_orange));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.light_orange));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.light_orange));
		staticColorScheme.setTextColor(getResources().getColor(R.color.light_orange));
		staticEverythingText.setTextColor(getResources().getColor(R.color.light_orange));
		staticFontText.setTextColor(getResources().getColor(R.color.light_orange));
    }
    
    public void setSchemeYellow() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_yellow);
		drivercambtn.setBackgroundResource(R.drawable.unselected_yellow);
		settingsbtn.setBackgroundResource(R.drawable.selected_yellow);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_yellow);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_yellow);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_yellow);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_yellow);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_yellow);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_yellow);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_yellow);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_yellow);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_yellow);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.yellow));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.yellow));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.yellow));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.light_yellow));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.light_yellow));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.light_yellow));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.light_yellow));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.light_yellow));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.light_yellow));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.light_yellow));
		staticColorScheme.setTextColor(getResources().getColor(R.color.light_yellow));
		staticEverythingText.setTextColor(getResources().getColor(R.color.light_yellow));
		staticFontText.setTextColor(getResources().getColor(R.color.light_yellow));
    }
    
    public void setSchemeGreen() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_green);
		drivercambtn.setBackgroundResource(R.drawable.unselected_green);
		settingsbtn.setBackgroundResource(R.drawable.selected_green);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_green);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_green);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_green);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_green);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_green);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_green);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_green);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_green);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_green);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.green));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.green));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.green));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.light_green));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.light_green));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.light_green));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.light_green));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.light_green));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.light_green));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.light_green));
		staticColorScheme.setTextColor(getResources().getColor(R.color.light_green));
		staticEverythingText.setTextColor(getResources().getColor(R.color.light_green));
		staticFontText.setTextColor(getResources().getColor(R.color.light_green));
    }
    
    public void setSchemeBlue() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_blue);
		drivercambtn.setBackgroundResource(R.drawable.unselected_blue);
		settingsbtn.setBackgroundResource(R.drawable.selected_blue);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_blue);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_blue);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_blue);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_blue);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_blue);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_blue);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_blue);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_blue);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_blue);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.blue));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.blue));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.blue));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.light_blue));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.light_blue));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.light_blue));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.light_blue));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.light_blue));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.light_blue));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.light_blue));
		staticColorScheme.setTextColor(getResources().getColor(R.color.light_blue));
		staticEverythingText.setTextColor(getResources().getColor(R.color.light_blue));
		staticFontText.setTextColor(getResources().getColor(R.color.light_blue));
    }
    
    public void setSchemePurple() {
    	maindashbtn.setBackgroundResource(R.drawable.unselected_purple);
		drivercambtn.setBackgroundResource(R.drawable.unselected_purple);
		settingsbtn.setBackgroundResource(R.drawable.selected_purple);
		restoreDefaultBtn.setBackgroundResource(R.drawable.button_selector_purple);
		speed_measure_spinner.setBackgroundResource(R.drawable.spinner_selector_purple);
		editPictureSetting.setBackgroundResource(R.drawable.edit_text_selector_purple);
		editVideoSettingSpeed.setBackgroundResource(R.drawable.edit_text_selector_purple);
		editVideoSettingMin.setBackgroundResource(R.drawable.edit_text_selector_purple);
		editVideoSettingSec.setBackgroundResource(R.drawable.edit_text_selector_purple);
		checkPictureSetting.setButtonDrawable(R.drawable.check_box_selector_purple);
		checkVideoSetting.setButtonDrawable(R.drawable.check_box_selector_purple);
		checkEverythingAEROfont.setButtonDrawable(R.drawable.check_box_selector_purple);
		staticSettingsMainDash.setTextColor(getResources().getColor(R.color.purple));
		staticSettingsDriverCam.setTextColor(getResources().getColor(R.color.purple));
		staticSettingsMisc.setTextColor(getResources().getColor(R.color.purple));
		staticSpeedMeasure.setTextColor(getResources().getColor(R.color.light_purple));
		staticPictureSetting.setTextColor(getResources().getColor(R.color.light_purple));
		txtPictureUnit.setTextColor(getResources().getColor(R.color.light_purple));
		staticVideoSetting.setTextColor(getResources().getColor(R.color.light_purple));
		txtVideoUnit.setTextColor(getResources().getColor(R.color.light_purple));
		staticVideoSettingMin.setTextColor(getResources().getColor(R.color.light_purple));
		staticVideoSettingSec.setTextColor(getResources().getColor(R.color.light_purple));
		staticColorScheme.setTextColor(getResources().getColor(R.color.light_purple));
		staticEverythingText.setTextColor(getResources().getColor(R.color.light_purple));
		staticFontText.setTextColor(getResources().getColor(R.color.light_purple));
    }
    
    public void setTypefaceSquealer() {
    	maindashbtn.setTypeface(squealer);
		drivercambtn.setTypeface(squealer);
		settingsbtn.setTypeface(squealer);
		restoreDefaultBtn.setTypeface(squealer);
		editPictureSetting.setTypeface(squealer);
		editVideoSettingSpeed.setTypeface(squealer);
		editVideoSettingMin.setTypeface(squealer);
		editVideoSettingSec.setTypeface(squealer);
		staticSettingsMainDash.setTypeface(squealer);
		staticSettingsDriverCam.setTypeface(squealer);
		staticSettingsMisc.setTypeface(squealer);
		staticSpeedMeasure.setTypeface(squealer);
		staticPictureSetting.setTypeface(squealer);
		txtPictureUnit.setTypeface(squealer);
		staticVideoSetting.setTypeface(squealer);
		txtVideoUnit.setTypeface(squealer);
		staticVideoSettingMin.setTypeface(squealer);
		staticVideoSettingSec.setTypeface(squealer);
		staticColorScheme.setTypeface(squealer);
		staticEverythingText.setTypeface(squealer);
		staticFontText.setTypeface(squealer);
    }
    
    public void setTypefaceDefault() {
    	maindashbtn.setTypeface(Typeface.DEFAULT);
		drivercambtn.setTypeface(Typeface.DEFAULT);
		settingsbtn.setTypeface(Typeface.DEFAULT);
		restoreDefaultBtn.setTypeface(Typeface.DEFAULT);
		editPictureSetting.setTypeface(Typeface.DEFAULT);
		editVideoSettingSpeed.setTypeface(Typeface.DEFAULT);
		editVideoSettingMin.setTypeface(Typeface.DEFAULT);
		editVideoSettingSec.setTypeface(Typeface.DEFAULT);
		staticSettingsMainDash.setTypeface(Typeface.DEFAULT);
		staticSettingsDriverCam.setTypeface(Typeface.DEFAULT);
		staticSettingsMisc.setTypeface(Typeface.DEFAULT);
		staticSpeedMeasure.setTypeface(Typeface.DEFAULT);
		staticPictureSetting.setTypeface(Typeface.DEFAULT);
		txtPictureUnit.setTypeface(Typeface.DEFAULT);
		staticVideoSetting.setTypeface(Typeface.DEFAULT);
		txtVideoUnit.setTypeface(Typeface.DEFAULT);
		staticVideoSettingMin.setTypeface(Typeface.DEFAULT);
		staticVideoSettingSec.setTypeface(Typeface.DEFAULT);
		staticColorScheme.setTypeface(Typeface.DEFAULT);
		staticEverythingText.setTypeface(Typeface.DEFAULT);
		staticFontText.setTypeface(Typeface.DEFAULT);
    }
    
}

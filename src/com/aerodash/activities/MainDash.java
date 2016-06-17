package com.aerodash.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;

import com.aerodash.R;
import com.aerodash.interfaces.Constants;
import com.aerodash.interfaces.GPSCallback;
import com.aerodash.managers.GPSManager;

public class MainDash extends Activity implements SurfaceHolder.Callback, GPSCallback{
	public static final String TAG = "MainDash";
	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	private int measurement_index, seconds, minutes, hours, oldSeconds, oldMinutes, oldHours, updateCount;
	private GPSManager gpsManager = null;
	private double newLat, newLong, oldLat, oldLong, oldSpeed, avgSpeed, distanceSum;
    private double speed = 0.0;
    private double[] avgSpeedResults = new double[1];
    private double[] distResults = new double[2];
    String strSpeedUnit, strDistanceUnit, speedString;
    TextView speedtext, textMaxSpeed, textAvgSpeed, textDistance, textElapsedTime, textCamNotification, textVideoTimer;
    Chronometer videoTimer, stopWatch;
    private long videoStartTime, startTime;
    private int drivercam_index, drivercam_picture_speed, drivercam_video_speed, drivercam_video_time, videoTimerSec, videoTimerMin;
    Camera mCamera;
    SurfaceView mPreview;
	MediaRecorder mMediaRecorder;
	int numberOfCameras, defaultCameraId;	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private boolean isRecording = false;
	private boolean reset_drivercam;
	AnimationSet fadeAnimation, fadeAnimationVideoTimer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Lock the android in landscape mode
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// Prevents the window from falling asleep
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Set the view as the main dash layout
		setContentView(R.layout.activity_main_dash);
		// Load preferences
		sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
		
		// Import layout buttons/text fields/etc.
		Button maindashbtn = (Button)findViewById(R.id.maindashbtn);
		Button drivercambtn = (Button)findViewById(R.id.drivercambtn);
		Button settingsbtn = (Button)findViewById(R.id.settingsbtn);
		Button resetbtn = (Button) findViewById(R.id.resetbtn);
        resetbtn.bringToFront();   // Bring the reset button to the front so that it hides the dummy surface view showing the camera preview
		speedtext = (TextView)findViewById(R.id.speedometer);
		TextView speedunit = (TextView)findViewById(R.id.speedunit);
		TextView staticMaxSpeed = (TextView)findViewById(R.id.maxspeed);
		TextView staticAvgSpeed = (TextView)findViewById(R.id.averagespeed);
		TextView staticDistance = (TextView)findViewById(R.id.dist_traveled);
		TextView staticElapsedTime = (TextView)findViewById(R.id.elapsedtime);
		textMaxSpeed = (TextView)findViewById(R.id.maxspeedoutput);
        textAvgSpeed = (TextView)findViewById(R.id.averagespeedoutput);
        textDistance = (TextView)findViewById(R.id.dist_traveled_output);
        textElapsedTime = (TextView)findViewById(R.id.elapsedtimeoutput);
        textCamNotification = (TextView)findViewById(R.id.drivercam_status);
        textVideoTimer = (TextView) findViewById(R.id.text_main_video_timer);
		
        // Set color scheme according to the set setting
		if (sharedPreferences.getString("colorSchemeActive", "blue").equals("white")) {
			maindashbtn.setBackgroundResource(R.drawable.selected_white);
			drivercambtn.setBackgroundResource(R.drawable.unselected_white);
			settingsbtn.setBackgroundResource(R.drawable.unselected_white);
			resetbtn.setBackgroundResource(R.drawable.button_selector_white);
			speedtext.setTextColor(getResources().getColor(R.color.white));
			speedunit.setTextColor(getResources().getColor(R.color.white));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.white));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.white));
			staticDistance.setTextColor(getResources().getColor(R.color.white));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.white));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.light_gray));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.light_gray));
			textDistance.setTextColor(getResources().getColor(R.color.light_gray));
			textElapsedTime.setTextColor(getResources().getColor(R.color.light_gray));
			textCamNotification.setTextColor(getResources().getColor(R.color.light_gray));
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_gray));
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("red")) {
			maindashbtn.setBackgroundResource(R.drawable.selected_red);
			drivercambtn.setBackgroundResource(R.drawable.unselected_red);
			settingsbtn.setBackgroundResource(R.drawable.unselected_red);
			resetbtn.setBackgroundResource(R.drawable.button_selector_red);
			speedtext.setTextColor(getResources().getColor(R.color.red));
			speedunit.setTextColor(getResources().getColor(R.color.red));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.red));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.red));
			staticDistance.setTextColor(getResources().getColor(R.color.red));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.red));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.peach));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.peach));
			textDistance.setTextColor(getResources().getColor(R.color.peach));
			textElapsedTime.setTextColor(getResources().getColor(R.color.peach));
			textCamNotification.setTextColor(getResources().getColor(R.color.peach));
			textVideoTimer.setTextColor(getResources().getColor(R.color.peach));
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("orange")) {
			maindashbtn.setBackgroundResource(R.drawable.selected_orange);
			drivercambtn.setBackgroundResource(R.drawable.unselected_orange);
			settingsbtn.setBackgroundResource(R.drawable.unselected_orange);
			resetbtn.setBackgroundResource(R.drawable.button_selector_orange);
			speedtext.setTextColor(getResources().getColor(R.color.orange));
			speedunit.setTextColor(getResources().getColor(R.color.orange));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.orange));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.orange));
			staticDistance.setTextColor(getResources().getColor(R.color.orange));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.orange));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.light_orange));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.light_orange));
			textDistance.setTextColor(getResources().getColor(R.color.light_orange));
			textElapsedTime.setTextColor(getResources().getColor(R.color.light_orange));
			textCamNotification.setTextColor(getResources().getColor(R.color.light_orange));
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_orange));
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("yellow")) {
			maindashbtn.setBackgroundResource(R.drawable.selected_yellow);
			drivercambtn.setBackgroundResource(R.drawable.unselected_yellow);
			settingsbtn.setBackgroundResource(R.drawable.unselected_yellow);
			resetbtn.setBackgroundResource(R.drawable.button_selector_yellow);
			speedtext.setTextColor(getResources().getColor(R.color.yellow));
			speedunit.setTextColor(getResources().getColor(R.color.yellow));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.yellow));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.yellow));
			staticDistance.setTextColor(getResources().getColor(R.color.yellow));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.yellow));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.light_yellow));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.light_yellow));
			textDistance.setTextColor(getResources().getColor(R.color.light_yellow));
			textElapsedTime.setTextColor(getResources().getColor(R.color.light_yellow));
			textCamNotification.setTextColor(getResources().getColor(R.color.light_yellow));
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_yellow));
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("green")) {
			maindashbtn.setBackgroundResource(R.drawable.selected_green);
			drivercambtn.setBackgroundResource(R.drawable.unselected_green);
			settingsbtn.setBackgroundResource(R.drawable.unselected_green);
			resetbtn.setBackgroundResource(R.drawable.button_selector_green);
			speedtext.setTextColor(getResources().getColor(R.color.green));
			speedunit.setTextColor(getResources().getColor(R.color.green));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.green));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.green));
			staticDistance.setTextColor(getResources().getColor(R.color.green));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.green));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.light_green));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.light_green));
			textDistance.setTextColor(getResources().getColor(R.color.light_green));
			textElapsedTime.setTextColor(getResources().getColor(R.color.light_green));
			textCamNotification.setTextColor(getResources().getColor(R.color.light_green));
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_green));
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("blue")) {
			maindashbtn.setBackgroundResource(R.drawable.selected_blue);
			drivercambtn.setBackgroundResource(R.drawable.unselected_blue);
			settingsbtn.setBackgroundResource(R.drawable.unselected_blue);
			resetbtn.setBackgroundResource(R.drawable.button_selector_blue);
			speedtext.setTextColor(getResources().getColor(R.color.blue));
			speedunit.setTextColor(getResources().getColor(R.color.blue));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.blue));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.blue));
			staticDistance.setTextColor(getResources().getColor(R.color.blue));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.blue));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.light_blue));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.light_blue));
			textDistance.setTextColor(getResources().getColor(R.color.light_blue));
			textElapsedTime.setTextColor(getResources().getColor(R.color.light_blue));
			textCamNotification.setTextColor(getResources().getColor(R.color.light_blue));
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_blue));
		}
		else {
			maindashbtn.setBackgroundResource(R.drawable.selected_purple);
			drivercambtn.setBackgroundResource(R.drawable.unselected_purple);
			settingsbtn.setBackgroundResource(R.drawable.unselected_purple);
			resetbtn.setBackgroundResource(R.drawable.button_selector_purple);
			speedtext.setTextColor(getResources().getColor(R.color.purple));
			speedunit.setTextColor(getResources().getColor(R.color.purple));
			staticMaxSpeed.setTextColor(getResources().getColor(R.color.purple));
			staticAvgSpeed.setTextColor(getResources().getColor(R.color.purple));
			staticDistance.setTextColor(getResources().getColor(R.color.purple));
			staticElapsedTime.setTextColor(getResources().getColor(R.color.purple));
			textMaxSpeed.setTextColor(getResources().getColor(R.color.light_purple));
			textAvgSpeed.setTextColor(getResources().getColor(R.color.light_purple));
			textDistance.setTextColor(getResources().getColor(R.color.light_purple));
			textElapsedTime.setTextColor(getResources().getColor(R.color.light_purple));
			textCamNotification.setTextColor(getResources().getColor(R.color.light_purple));
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_purple));
		}
		
		// If Everything AERO font is checked
		if(sharedPreferences.getBoolean("checkEverythingAEROfont", false) == true) {
			Typeface squealer = Typeface.createFromAsset(getAssets(),"fonts/squealer.ttf");
			maindashbtn.setTypeface(squealer);
			drivercambtn.setTypeface(squealer);
			settingsbtn.setTypeface(squealer);
			resetbtn.setTypeface(squealer);
			speedtext.setTypeface(squealer);
			speedunit.setTypeface(squealer);
			staticMaxSpeed.setTypeface(squealer);
			staticAvgSpeed.setTypeface(squealer);
			staticDistance.setTypeface(squealer);
			staticElapsedTime.setTypeface(squealer);
			textMaxSpeed.setTypeface(squealer);
			textAvgSpeed.setTypeface(squealer);
			textDistance.setTypeface(squealer);
			textElapsedTime.setTypeface(squealer);
			textCamNotification.setTypeface(squealer);
			textVideoTimer.setTypeface(squealer);
		}
		
		// Get the update count
		updateCount = sharedPreferences.getInt("updateCount", 0);
		
		// Check if the driver cam is active
		if (sharedPreferences.getBoolean("checkPictureSetting", false) == true) {
        	drivercam_index = 1;
        	drivercam_picture_speed = sharedPreferences.getInt("editPictureSetting", 0);
        }
        else if (sharedPreferences.getBoolean("checkVideoSetting", false) == true) {
        	drivercam_index = 2;
        	drivercam_video_speed = sharedPreferences.getInt("editVideoSettingSpeed", 0);
        	drivercam_video_time = sharedPreferences.getInt("editVideoSettingMin", 0) * 60 + sharedPreferences.getInt("editVideoSettingSec", 0);
        }
        else {
        	// Driver cam is inactive
        	drivercam_index = 0;
        }
		
		// Initialize or import values depending on update count
		if(updateCount == 0){
			oldLat = Double.NaN;
			oldLong = Double.NaN;
			oldSpeed = 0.0;
			avgSpeed = 0.0;
			distanceSum = 0.0;
			reset_drivercam = true;
		}
		else{
	        oldLat = Double.parseDouble(sharedPreferences.getString("strOldLat", "0.0"));
	        oldLong = Double.parseDouble(sharedPreferences.getString("strOldLong", "0.0"));
	        oldSpeed = Double.parseDouble(sharedPreferences.getString("strOldSpeed", "0.0"));
	        avgSpeed = Double.parseDouble(sharedPreferences.getString("strAvgSpeed", "0.0"));
	        distanceSum = Double.parseDouble(sharedPreferences.getString("strDistanceSum", "0.0"));
	        if (drivercam_index == 1) {
	        	if(double2Int(convertSpeed(oldSpeed)) <= drivercam_picture_speed) {
	        		reset_drivercam = true;
	        	}
	        	else {
	        		reset_drivercam = false;
	        	}
	        }
	        else if (drivercam_index == 2) {
	        	if(double2Int(convertSpeed(oldSpeed)) <= drivercam_video_speed) {
	        		reset_drivercam = true;
	        	}
	        	else {
	        		reset_drivercam = false;
	        	}
	        }
		}
		
		// Set the unit strings
		strSpeedUnit = sharedPreferences.getString("SpeedUnit", "mph");
		strDistanceUnit = sharedPreferences.getString("DistanceUnit", "mi");
		// Set the measurement index
		if(strSpeedUnit.equals("mph")){
			measurement_index = 1;
		}
		else {
			measurement_index = 0;
		}
		
		speedunit.setText(strSpeedUnit);		
		
		// Sets OnTouchListener so that button executes as soon as the button is touched
		drivercambtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					// Starts the new activity
					startActivity(new Intent(getApplicationContext(),DriverCam.class));
					// Transition between activities
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
					// If the driver cam is active send all taken photos/videos to the android gallery app
					if (drivercam_index != 0) {
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
					}
				}
				return false;
			}
		});
		
		settingsbtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					startActivity(new Intent(getApplicationContext(),SettingsTab.class));
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
					if (drivercam_index != 0) {
						sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
					}
				}
				return false;
			}
		});
		
		// Start the GPS Manager
		gpsManager = new GPSManager();
        
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);        
        
        // Define the chronometer
        stopWatch = (Chronometer) findViewById(R.id.chrono_main);
        
        // Create a TextChangedListener to detect when the speedometer text changes and start the timer
        speedtext.addTextChangedListener(new TextWatcher() {
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) {
        		// Do nothing
        	}
        	
        	@Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
        		// Do nothing
            }
        	
            @Override
            public void afterTextChanged(Editable s) {
            	// After the speedometer's text changes, start or freeze the timer based on whether you're moving
            	// Timer freezes if you're still and counts when you're moving
            	if(speedString.equals("0")){
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
                		textElapsedTime.setText("" + "0" + hours + ":0" + minutes + ":0" + seconds);
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
            			setTimerText(oldSeconds, oldMinutes, oldHours);
            		}
            	}
            	else{
            		if(updateCount == 0) {
            			startTime = SystemClock.elapsedRealtime();
            			oldSeconds = 0;
                		oldMinutes = 0;
                		oldHours = 0;
            			editor = sharedPreferences.edit();
            			editor.putLong("startTime", startTime);
            			editor.putInt("oldSeconds", oldSeconds);
            			editor.putInt("oldMinutes", oldMinutes);
            			editor.putInt("oldHours", oldHours);
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
            				setTimerText(seconds, minutes, hours);
            			}
            		});
            		stopWatch.start();
            	}
            }
        });
        
        videoTimer = (Chronometer) findViewById(R.id.chrono_main_video_timer);
        
        // Initialize the camera
        numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
     	for (int i = 0; i < numberOfCameras; i++) {
     		Camera.getCameraInfo(i, cameraInfo);
     		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
     			defaultCameraId = i;
     		}
     	}
        
     	// Create the dummy surface
     	mPreview = (SurfaceView)findViewById(R.id.cam_preview);
	    mPreview.getHolder().addCallback(this);
     	
     	mCamera = null;
	    try {
	    	mCamera = Camera.open(defaultCameraId); // Attempt to get a camera instance
	    }
	    catch (Exception e) {
	    	// Camera is not available, in use, or does not exist
	    }
                
        textVideoTimer.addTextChangedListener(new TextWatcher() {
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) {
        		// Do nothing
        	}
        	
        	@Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
        		// Do nothing
            }
        	
            @Override
            public void afterTextChanged(Editable s) {
            	if ((videoTimerMin * 60 + videoTimerSec) >= drivercam_video_time) {
            		mMediaRecorder.stop();
	                releaseMediaRecorder();
	                mCamera.lock();
	                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
	                isRecording = false;
	                videoTimer.stop();    
            	}
            }
        });
        
        // Create animations for the driver cam notifications
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);

        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        
        final Animation fadeOutVideoTimer = new AlphaAnimation(1.0f, 0.0f);
        fadeOutVideoTimer.setDuration(500);

        fadeAnimation = new AnimationSet(true);
        fadeAnimation.addAnimation(fadeIn);
        fadeOut.setStartOffset(3000);
        fadeAnimation.addAnimation(fadeOut);
        
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            	textCamNotification.setText("Picture Taken");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	textCamNotification.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            	// Do nothing
            }
        });
        
        fadeAnimationVideoTimer = new AnimationSet(true);
        fadeAnimationVideoTimer.addAnimation(fadeIn);
        fadeOutVideoTimer.setStartOffset(drivercam_video_time * 1000 + 2000);
        fadeAnimationVideoTimer.addAnimation(fadeOutVideoTimer);
        
        fadeAnimationVideoTimer.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            	textCamNotification.setText("Recording Video");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	videoTimerSec = 0;
            	videoTimerMin = 0;
            	textCamNotification.setText("");
            	textVideoTimer.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            	// Do nothing
            }
        });
        
        // Reset button
        resetbtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if(updateCount != 0) {
        			updateCount = 0;
        			oldSpeed = 0.0;
        			textMaxSpeed.setText("0 " + strSpeedUnit);
        			avgSpeed = 0.0;
        			textAvgSpeed.setText("0 " + strSpeedUnit);
        			distanceSum = 0.0;
        			textDistance.setText(distanceSum + " " + strDistanceUnit);
        			seconds = 0;
        			minutes = 0;
        			hours = 0;
        			textElapsedTime.setText("" + "0" + hours + ":0" + minutes + ":0" + seconds);
        		}
        	}
        });
	}
	
	
	@Override
	public void onBackPressed() {
	    // This block of code ensures you'll get the same transition when the back button is pressed.
		super.onBackPressed();
	    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	@Override
    public void onGPSUpdate(Location location) {
		editor = sharedPreferences.edit();
		
		newLat = location.getLatitude();
        newLong = location.getLongitude();
        // Returns the value for speed in m/s
        speed = location.getSpeed();
          
        // convertSpeed converts the speed into the appropriate unit and "" + (speed value) converts from double to string
        speedString = "" + double2Int(convertSpeed(speed));
        // Display the converted speed on the speedometer textview
        speedtext.setText(speedString);
            
        // Redefine old values to be used in next GPS update
        oldSpeed = maxSpeedFunction(speed,oldSpeed);
        avgSpeedResults = averageSpeedFunction(speed,updateCount,avgSpeed);
        avgSpeed = avgSpeedResults[0];
        updateCount = (int) avgSpeedResults[1];
        distResults = approxDistanceTraveled(oldLat,oldLong,newLat,newLong,distanceSum);
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
        
        if (drivercam_index == 0) {
        	// Do nothing because the driver cam is inactive
        }
        else if (drivercam_index == 1) {
            if (reset_drivercam == false) {
            	if ((drivercam_picture_speed - Constants.reset_speed[measurement_index]) > 0) {
            		if (double2Int(convertSpeed(speed)) < (drivercam_picture_speed - Constants.reset_speed[measurement_index])) {
            			reset_drivercam = true;
            		}
            	}
            	else {
            		if (double2Int(convertSpeed(speed)) == 0) {
            			reset_drivercam = true;
            		}
            	}
            }
        	if (drivercam_picture_speed == 0) {
        		// Do nothing
        	}
        	else if (double2Int(convertSpeed(speed)) >= drivercam_picture_speed && reset_drivercam == true) {
        		mCamera.takePicture(null, null, mPicture);
        		textCamNotification.startAnimation(fadeAnimation);
        		reset_drivercam = false;
        	}
        }
        else {
            if (reset_drivercam == false && isRecording == false) {
            	if ((drivercam_video_speed - Constants.reset_speed[measurement_index]) > 0) {
            		if (double2Int(convertSpeed(speed)) < (drivercam_video_speed - Constants.reset_speed[measurement_index])) {
            			reset_drivercam = true;
            		}
            	}
            	else {
            		if (double2Int(convertSpeed(speed)) == 0) {
            			reset_drivercam = true;
            		}
            	}
            }
        	if (drivercam_video_speed == 0 || drivercam_video_time == 0) {
        		// Do nothing
        	}
        	else if (double2Int(convertSpeed(speed)) >= drivercam_video_speed && reset_drivercam == true && isRecording == false) {
        		if (prepareVideoRecorder() == true) {
	               	isRecording = true;
	               	mMediaRecorder.start();
	                videoStartTime = SystemClock.elapsedRealtime();
	                videoTimer.setOnChronometerTickListener(new OnChronometerTickListener(){
	           			@Override
	           			public void onChronometerTick(Chronometer arg0) {
            				videoTimerSec = (((int) (SystemClock.elapsedRealtime() - videoStartTime)) / 1000);
	            			videoTimerMin = videoTimerSec / 60;
	            			videoTimerSec = videoTimerSec % 60;
	            			setVideoTimerText(videoTimerSec, videoTimerMin);
	            		}
	                });
	                videoTimer.start();
	                textCamNotification.startAnimation(fadeAnimationVideoTimer);
                    textVideoTimer.startAnimation(fadeAnimationVideoTimer);
	                reset_drivercam = false;
	            }
	            else {
	                releaseMediaRecorder();
	            }
        	}
        }
    }
	
	@Override
    public void onPause() {
        super.onPause();
        mCamera.stopPreview();
        if (isRecording == true) {
        	mMediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));

            isRecording = false;
            reset_drivercam = false;
            videoTimer.stop();
            videoTimerSec = 0;
            videoTimerMin = 0;
            textVideoTimer.setText("" + "0" + videoTimerMin + ":0" + videoTimerSec);
        }
        else {
        	releaseMediaRecorder();
        }
        releaseCamera();
    }
	
	@Override
	public void onResume() {
		super.onResume();
		if (mCamera == null) {
			try {
		    	mCamera = Camera.open(defaultCameraId); // Attempt to get a camera instance
		    }
		    catch (Exception e) {
		    	// Camera is not available, in use, or does not exist
		    }
			mMediaRecorder = new MediaRecorder();
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording == true) {
        	mMediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));

            isRecording = false;
            reset_drivercam = false;
            videoTimer.stop();
            videoTimerSec = 0;
            videoTimerMin = 0;
            textVideoTimer.setText("" + "0" + videoTimerMin + ":0" + videoTimerSec);
        }
        else {
        	releaseMediaRecorder();
        }
        releaseCamera();
        
        // When the activity closes the GPS Manager is stopped
    	gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
            
        gpsManager = null;
        
        stopWatch.stop();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_dash, menu);
		return true;
	}
	
	// Function to convert the speed into the appropriate unit based on the measurement index
	public double convertSpeed(double speed){
        return ((speed * Constants.hour_multiplier) * Constants.unit_multipliers[measurement_index]); 
	}
	
	// Converts distance from meters to kilometers or miles depending on measurement index
	public double convertDistance(double distance){
		return ((distance * Constants.unit_multipliers[measurement_index]));
	}
	
	// Function that rounds a double to its appropriate integer
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
	
	// Rounds a double to a specified decimal place
	public double roundDecimal(double value, final int decimalPlace)
    {
            BigDecimal bd = new BigDecimal(value);
            
            bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
            value = bd.doubleValue();
            
            return value;
    }
	
	// Compares new updated speed with the last found speed to properly set the max speed textview and returns the max speed value
	public double maxSpeedFunction(double newSpeed, double oldSpeed){
		double returnSpeed;
		if(newSpeed>oldSpeed){
			String maxSpeedString = "" + double2Int(convertSpeed(newSpeed)) + " " + strSpeedUnit;
			textMaxSpeed.setText(maxSpeedString);
			returnSpeed = newSpeed;
			return returnSpeed;
		}
		else{
			String maxSpeedString = "" + double2Int(convertSpeed(oldSpeed)) + " " + strSpeedUnit;
			textMaxSpeed.setText(maxSpeedString);
			returnSpeed = oldSpeed;
			return returnSpeed;
		}
	}
	
	// Calculates the average speed but excludes all speeds that are 0
	public double[] averageSpeedFunction(double newSpeed, int count, double avgSpeed){
		double returnSpeed;
		if(speedString.equals("0")){
			double newCount = (double) count;
			returnSpeed = avgSpeed;
			String avgSpeedString = "" + double2Int(convertSpeed(returnSpeed)) + " " + strSpeedUnit;
			textAvgSpeed.setText(avgSpeedString);
			double[] results = {returnSpeed, newCount};
			return results;
		}
		else{
			count++;
			double newCount = (double) count;
			returnSpeed = (newSpeed + (avgSpeed * (newCount - 1))) / newCount;
			String avgSpeedString = "" + double2Int(convertSpeed(returnSpeed)) + " " + strSpeedUnit;
			textAvgSpeed.setText(avgSpeedString);
			double[] results = {returnSpeed, newCount};
			return results;
		}
	}
	
	// Uses a linear approximation to estimate the total distance traveled
	public double[] approxDistanceTraveled(double startLatitude, double startLongitude, double endLatitude, double endLongitude, double distanceSum){
		if(Double.isNaN(startLatitude)==true||Double.isNaN(startLongitude)==true){
			String distanceString = "" + roundDecimal(convertDistance(distanceSum),1) + " " + strDistanceUnit;
			textDistance.setText(distanceString);
			
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
		
			// Method distanceTo returns a distance between two point in meters
			double distance = startLocation.distanceTo(endLocation);
			distanceSum = distanceSum + distance;
			String distanceString = "" + roundDecimal(convertDistance(distanceSum),1) + " " + strDistanceUnit;
			textDistance.setText(distanceString);
		
			double oldLat, oldLong;
			oldLat = endLatitude;
			oldLong = endLongitude;
		
			double[] results = {oldLat, oldLong, distanceSum};
			return results;
		}
	}
	
	public void setTimerText(int seconds, int minutes, int hours) {
		if (hours < 10) {
			if(minutes < 10) {
				if(seconds < 10) {
					textElapsedTime.setText("" + "0" + hours + ":0" + minutes + ":0" + seconds);
				}
				else {
					textElapsedTime.setText("" + "0" + hours + ":0" + minutes + ":" + seconds);
				}
			}
			else {
				if(seconds < 10) {
					textElapsedTime.setText("" + "0" + hours + ":" + minutes + ":0" + seconds);
				}
				else {
					textElapsedTime.setText("" + "0" + hours + ":" + minutes + ":" + seconds);
				}
			}
		} 
		else {
			if(minutes < 10) {
				if(seconds < 10) {
					textElapsedTime.setText("" + hours + ":0" + minutes + ":0" + seconds);
				}
				else {
					textElapsedTime.setText("" + hours + ":0" + minutes + ":" + seconds);
				}
			}
			else {
				if(seconds < 10) {
					textElapsedTime.setText("" + hours + ":" + minutes + ":0" + seconds);
				}
				else {
					textElapsedTime.setText("" + hours + ":" + minutes + ":" + seconds);
				}
			}
		}
	}
	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        params.setPreviewSize(selected.width,selected.height);
        mCamera.setParameters(params);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("PREVIEW","surfaceDestroyed");
        releaseCamera();
    }
	
	private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera mCamera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            mCamera.startPreview();
        }
    };
	
    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "AERO Dash");
        
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    private void releaseCamera() {
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }
    
    private boolean prepareVideoRecorder(){
    	
	    mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    
    public void setVideoTimerText(int seconds, int minutes) {
    	if(minutes < 10) {
			if(seconds < 10) {
				textVideoTimer.setText("" + "0" + minutes + ":0" + seconds);
			}
			else {
				textVideoTimer.setText("" + "0" + minutes + ":" + seconds);
			}
		}
    	else {
    		if(seconds < 10) {
    			textVideoTimer.setText("" + minutes + ":0" + seconds);
			}
			else {
				textVideoTimer.setText("" + minutes + ":" + seconds);
			}
		}
    }
}

package com.aerodash.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.aerodash.R;
import com.aerodash.interfaces.Constants;
import com.aerodash.interfaces.GPSCallback;
import com.aerodash.managers.GPSManager;

public class DriverCam extends Activity implements SurfaceHolder.Callback, GPSCallback {
	public static final String TAG = "DriverCam";
	private GPSManager gpsManager = null;
	private double newLat, newLong, oldLat, oldLong, oldSpeed, avgSpeed, distanceSum;
    private double speed = 0.0;
    private int updateCount, measurement_index, roundedSpeed, seconds, minutes, hours, oldSeconds, oldMinutes, oldHours;
    private double[] avgSpeedResults = new double[1];
    private double[] distResults = new double[2];
    SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	String cam_index;
	Button takepicturebtn;
	SurfaceView mPreview;
	Camera mCamera;
	MediaRecorder mMediaRecorder;
	int numberOfCameras, defaultCameraId;	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private boolean isRecording = false;
	TextView textVideoTimer;
	Chronometer videoTimer, stopWatch;
    private long videoStartTime, startTime;
    private int videoTimerSec, videoTimerMin;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_driver_cam);
		
		sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
		
		Button maindashbtn = (Button)findViewById(R.id.maindashbtn);
		Button drivercambtn = (Button)findViewById(R.id.drivercambtn);
		Button settingsbtn = (Button)findViewById(R.id.settingsbtn);
		Switch camera_toggle = (Switch)findViewById(R.id.camera_toggle);
	    takepicturebtn = (Button)findViewById(R.id.takepicturebtn);
	    textVideoTimer = (TextView) findViewById(R.id.video_timer);
		
		if (sharedPreferences.getString("colorSchemeActive", "blue").equals("white")) {
			maindashbtn.setBackgroundResource(R.drawable.unselected_white);
			drivercambtn.setBackgroundResource(R.drawable.selected_white);
			settingsbtn.setBackgroundResource(R.drawable.unselected_white);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_white);
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_gray));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearanceWhite);
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("red")) {
			maindashbtn.setBackgroundResource(R.drawable.unselected_red);
			drivercambtn.setBackgroundResource(R.drawable.selected_red);
			settingsbtn.setBackgroundResource(R.drawable.unselected_red);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_red);
			textVideoTimer.setTextColor(getResources().getColor(R.color.peach));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearanceRed);
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("orange")) {
			maindashbtn.setBackgroundResource(R.drawable.unselected_orange);
			drivercambtn.setBackgroundResource(R.drawable.selected_orange);
			settingsbtn.setBackgroundResource(R.drawable.unselected_orange);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_orange);
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_orange));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearanceOrange);
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("yellow")) {
			maindashbtn.setBackgroundResource(R.drawable.unselected_yellow);
			drivercambtn.setBackgroundResource(R.drawable.selected_yellow);
			settingsbtn.setBackgroundResource(R.drawable.unselected_yellow);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_yellow);
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_yellow));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearanceYellow);
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("green")) {
			maindashbtn.setBackgroundResource(R.drawable.unselected_green);
			drivercambtn.setBackgroundResource(R.drawable.selected_green);
			settingsbtn.setBackgroundResource(R.drawable.unselected_green);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_green);
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_green));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearanceGreen);
		}
		else if (sharedPreferences.getString("colorSchemeActive", "blue").equals("blue")) {
			maindashbtn.setBackgroundResource(R.drawable.unselected_blue);
			drivercambtn.setBackgroundResource(R.drawable.selected_blue);
			settingsbtn.setBackgroundResource(R.drawable.unselected_blue);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_blue);
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_blue));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearanceBlue);
		}
		else {
			maindashbtn.setBackgroundResource(R.drawable.unselected_purple);
			drivercambtn.setBackgroundResource(R.drawable.selected_purple);
			settingsbtn.setBackgroundResource(R.drawable.unselected_purple);
			takepicturebtn.setBackgroundResource(R.drawable.button_selector_purple);
			textVideoTimer.setTextColor(getResources().getColor(R.color.light_purple));
			camera_toggle.setSwitchTextAppearance(this, R.style.SwitchTextAppearancePurple);
		}
		
		if(sharedPreferences.getBoolean("checkEverythingAEROfont", false) == true) {
			Typeface squealer = Typeface.createFromAsset(getAssets(),"fonts/squealer.ttf");
			maindashbtn.setTypeface(squealer);
			drivercambtn.setTypeface(squealer);
			settingsbtn.setTypeface(squealer);
			takepicturebtn.setTypeface(squealer);
			textVideoTimer.setTypeface(squealer);
			camera_toggle.setSwitchTypeface(squealer);
		}
		
		String strSpeedUnit = sharedPreferences.getString("SpeedUnit", "mph");
		if(strSpeedUnit.equals("mph")){
			measurement_index = 1;
		}
		else {
			measurement_index = 0;
		}
		
		cam_index = "Take Picture";
	    takepicturebtn.setText(cam_index);
	    textVideoTimer.setVisibility(View.INVISIBLE);
	    
		// Sets a switch listener that will check if the photo/video toggle switch gets changed
		camera_toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					cam_index = "Take Video";
					takepicturebtn.setText(cam_index);
					textVideoTimer.setVisibility(View.VISIBLE);
					videoTimerSec = 0;
					videoTimerMin = 0;
					textVideoTimer.setText("" + "0" + videoTimerMin + ":0" + videoTimerSec);
				}
				else {
					cam_index = "Take Picture";
					takepicturebtn.setText(cam_index);
					textVideoTimer.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		maindashbtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					startActivity(new Intent(getApplicationContext(),MainDash.class));
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
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
	                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
				}
				return false;
			}
		});
		
		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();
		
		//Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
					defaultCameraId = i;
				}
			}
			
		mPreview = (SurfaceView)findViewById(R.id.cam_preview);
	    mPreview.getHolder().addCallback(this);
	    
	    mCamera = null;
	    try {
	    	mCamera = Camera.open(defaultCameraId); // Attempt to get a camera instance
	    }
	    catch (Exception e) {
	    	// Camera is not available, in use, or does not exist
	    }
		
	    stopWatch = (Chronometer) findViewById(R.id.chrono_drivercam_main);
	    videoTimer = (Chronometer) findViewById(R.id.chrono_drivercam_video);
	    
		takepicturebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(cam_index.equals("Take Picture")) {
					mCamera.takePicture(null, null, mPicture);
				}
				else {
		            if (isRecording == true) {
		                // stop recording and release camera
		                mMediaRecorder.stop();  // stop the recording
		                releaseMediaRecorder(); // release the MediaRecorder object
		                mCamera.lock();         // take camera access back from MediaRecorder
		                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));

		                // inform the user that recording has stopped
		                takepicturebtn.setText("Take Video");
		                isRecording = false;
		                videoTimer.stop();
		                videoTimerSec = 0;
		                videoTimerMin = 0;
		                textVideoTimer.setText("" + "0" + videoTimerMin + ":0" + videoTimerSec);
		            }
		            else {
		                // initialize video camera
		                if (prepareVideoRecorder() == true) {
		                    // Camera is available and unlocked, MediaRecorder is prepared,
		                    // now you can start recording
		                    mMediaRecorder.start();

		                    // inform the user that recording has started
		                    takepicturebtn.setText("Stop Video");
		                    isRecording = true;
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
		                }
		                else {
		                    // prepare didn't work, release the camera
		                    releaseMediaRecorder();
		                    // inform user
		                }
		            }
				}
			}
		});
		
		gpsManager = new GPSManager();
        
        gpsManager.startListening(getApplicationContext());
        gpsManager.setGPSCallback(this);
        
        updateCount = sharedPreferences.getInt("updateCount", 0);
        oldLat = Double.parseDouble(sharedPreferences.getString("strOldLat", "0.0"));
        oldLong = Double.parseDouble(sharedPreferences.getString("strOldLong", "0.0"));
        oldSpeed = Double.parseDouble(sharedPreferences.getString("strOldSpeed", "0.0"));
        avgSpeed = Double.parseDouble(sharedPreferences.getString("strAvgSpeed", "0.0"));
        distanceSum = Double.parseDouble(sharedPreferences.getString("strDistanceSum", "0.0"));

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
    public void onPause() {
        super.onPause();
        mCamera.stopPreview();
        if (isRecording == true) {
        	takepicturebtn.performClick();
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
    public void onDestroy() {
        super.onDestroy();
        if (isRecording == true) {
        	takepicturebtn.performClick();
        }
        else {
        	releaseMediaRecorder();
        }
        releaseCamera();
        
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
            
        gpsManager = null;
        
        stopWatch.stop();
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

    // Create a File for saving an image or video
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "AERO Dash");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
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
    
    public double convertSpeed(double speed){
        return ((speed * Constants.hour_multiplier) * Constants.unit_multipliers[measurement_index]); 
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
    
}

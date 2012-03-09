package me.ideia.cameraoverlay;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.WindowManager;

public class SplashActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		try {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory()))); 
		} catch (Exception e) {
		}
		
        new Handler().postDelayed(new Thread() {
        	@Override
        	public void run() {
				Intent activity = new Intent(SplashActivity.this, CameraOverlayActivity.class);
				startActivity(activity);
				SplashActivity.this.finish();
        	}
        }, 5000);
	}

}
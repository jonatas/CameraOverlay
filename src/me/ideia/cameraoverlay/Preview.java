package me.ideia.cameraoverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private int w, h;

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	protected void onWindowVisibilityChanged(int visibility) {
		try {
			super.onWindowVisibilityChanged(visibility);
		} catch (Exception e) {
		}
		
	};
	
	ShutterCallback shutter = new ShutterCallback() {

		@Override
		public void onShutter() {
		}

	};
	PictureCallback raw = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
		}

	};

	public String file = "/sdcard/CameraOverlay/image-1.jpg";
	PictureCallback jpeg = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(file);
				outStream.write(data);
				outStream.flush();
				outStream.close();
			} catch (Exception e) {
		//		Log.d("Camera", e.getMessage());
				((CameraOverlayActivity)getContext()).toast("Exception! " + e.getMessage());
			}

			File f = new File(file);
			try {
			    if (f.exists()) {
			    	((CameraOverlayActivity)getContext()).toast(getContext().getString(R.string.successsaved));
			    } else {
			    	((CameraOverlayActivity)getContext()).toast(getContext().getString(R.string.dontsaved));
			    }
			} catch (Exception e) {
			}
		}

	};

	public void takePicture(String basefile) {
		if (basefile != null) {
			file = basefile;
		}
		
		Pattern pattern = Pattern.compile("(.*)-([0-9]+)(\\..{3,4})$");
		Matcher matcher = pattern.matcher(basefile);
		try {
			File f;
			String auxFile;
			if (matcher.matches()) {
				if (matcher.groupCount() == 3) {
					int auxOrder = 0;
					try {
						auxOrder = Integer.parseInt(matcher.group(2));
					} catch (Exception e) {
					}
					do {
						auxFile = matcher.group(1) + "-" + (++auxOrder) + matcher.group(3);
						f = new File(auxFile);
					} while (f.exists());
					file = auxFile;
				} else {
					throw new Exception();
				}
			} else {
				pattern = Pattern.compile("(.*)(\\..{3,4})$");
				matcher = pattern.matcher(basefile);
				if (matcher.matches()) {
					if (matcher.groupCount() == 2) {
						int auxOrder = 0;
						do {
							auxFile = matcher.group(1) + "-" + (++auxOrder) + matcher.group(3);
							f = new File(auxFile);
						} while (f.exists());
						file = auxFile;
					} else {
						throw new Exception();
					}
				}
			}
		} catch (Exception e) {
			file = CameraOverlayActivity.basefile;
			File f = new File(file);
			int auxOrder = 0;
			if (!f.exists()) {
				do {
					file = "/sdcard/CameraOverlay/image-" + (++auxOrder) + ".jpg";
					f = new File(file);
				} while (f.exists());
			}
		}
		mCamera.takePicture(shutter, raw, jpeg);
		try {
			getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory()))); 
		} catch (Exception e) {
		}
	}

	public void startCamera() {
		if (mCamera == null) {
			mCamera = Camera.open();
			try {
				mCamera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				Builder cameraPreviewError = new AlertDialog.Builder(getContext());
				cameraPreviewError.setMessage(R.string.error_get_camera_preview); 
				cameraPreviewError.setIcon(R.drawable.error);
				cameraPreviewError.setCancelable(true);
				cameraPreviewError.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						System.exit(0);
						
					}
				});
				cameraPreviewError.setItems(R.string.retry, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						try {
							mCamera.setPreviewDisplay(mHolder);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(0);
						}
					}
				});
				
			}
		}
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(w, h);
		try {
			
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// strange stuff happnens on a unknown model. 
			// fixing the first reported error by @googleplay
		}
		mCamera.startPreview();
		new Handler().post(new Thread() {
			@Override
			public void run() {
				final Button takePicture = (Button) findViewById(R.id.takepicture);
				if (takePicture != null) {
					takePicture.setVisibility(View.VISIBLE);
					Button takeNewPicture = (Button) findViewById(R.id.takenewpicture);
					takeNewPicture.setVisibility(View.GONE);
				}
			}
		});
	}
	
	public void stopCamera() {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		new Handler().post(new Thread() {
			@Override
			public void run() {
				final Button takePicture = (Button) findViewById(R.id.takepicture);
				if (takePicture != null) {
					takePicture.setVisibility(View.GONE);
					Button takeNewPicture = (Button) findViewById(R.id.takenewpicture);
					takeNewPicture.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		stopCamera();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		this.w = w;
		this.h = h;
		startCamera();
	}

}
package me.ideia.cameraoverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
				// Log.d("Camera", e.getMessage());
				((CameraOverlayActivity)getContext()).toast("Exception! " + e.getMessage());
			}

			File f = new File(file);
			try {
				if (f.exists()) {
					((CameraOverlayActivity)getContext()).toast(getContext().getString(R.string.successsaved));
					if (((CameraOverlayActivity)getContext()).photoBase.withoutPicture()) {
						((CameraOverlayActivity)getContext()).takeNewPicture();
					}
				} else {
					((CameraOverlayActivity)getContext()).toast(getContext().getString(R.string.dontsaved));
				}
			} catch (Exception e) {
				e.printStackTrace();
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
						auxFile = file;
						f = new File(auxFile);
						while (f.exists()) {
							auxFile = matcher.group(1) + "-" + (++auxOrder) + matcher.group(2);
							f = new File(auxFile);
						}
						file = auxFile;
					} else {
						throw new Exception();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			file = CameraOverlayActivity.basefile;
			File f = new File(file);
			int auxOrder = 0;
			while (f.exists()) {
				file = "/sdcard/CameraOverlay/image-" + (++auxOrder) + ".jpg";
				f = new File(file);
			}
		}
		mCamera.takePicture(shutter, raw, jpeg);
		try {
			getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
		} catch (Exception e) {
		}
	}

	public void startCamera() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
				mCamera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				Toast.makeText(getContext(), "Não foi possível carregar a câmera: " + e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		Camera.Parameters parameters = mCamera.getParameters();
		List<Size> pictureSizes = parameters.getSupportedPictureSizes();
		List<Size> previewSizes = parameters.getSupportedPreviewSizes();
		Size maxPictureSize = pictureSizes.get(pictureSizes.size() - 1);
		float screenRatio = (float)maxPictureSize.height / (float)maxPictureSize.width;
		Size maxPreviewSize = previewSizes.get(0).height < previewSizes.get(previewSizes.size() - 1).height ? previewSizes.get(0) : previewSizes.get(previewSizes.size() - 1);
		float difference = Math.abs(((float)maxPreviewSize.height / (float)maxPreviewSize.width) - screenRatio);
		boolean found = false;
		float adjust = 0;
		do {
			for (Size previewSize : previewSizes) {
				float previewRatio = (float)previewSize.height / (float)previewSize.width;
				float diff = Math.abs(previewRatio - screenRatio);
				if (difference + adjust > diff && maxPreviewSize.height < previewSize.height) {
					maxPreviewSize = previewSize;
					difference = diff;
					found = true;
				}
			}
			adjust += 0.01;
		} while (found == false);
		parameters.setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
		
		float ratio = (float)maxPreviewSize.height / (float)maxPreviewSize.width;

		int pictureHeight = (int)(maxPictureSize.width * ratio);
		int pictureWidth = maxPictureSize.width;
		if (pictureHeight > maxPictureSize.height) {
			pictureHeight = maxPictureSize.height;
			pictureWidth = (int)(maxPictureSize.height / ratio);
		}
		try {
			try {
				parameters.setPictureSize(pictureWidth, pictureHeight);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} catch (RuntimeException e) {
				parameters.setPictureSize(maxPictureSize.width, maxPictureSize.height);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}
		} catch (RuntimeException e) {
			// strange stuff happnens on a unknown model.
			// fixing the first reported error by @googleplay
			Toast.makeText(getContext(), "Não foi possível carregar parâmetros e iniciar o preview da câmera.", Toast.LENGTH_LONG).show();
		}
	}

	public void stopCamera() {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		new Handler().post(new Thread() {
			@Override
			public void run() {
				final Button takePicture = (Button)findViewById(R.id.takepicture);
				if (takePicture != null) {
					takePicture.setVisibility(View.GONE);
					Button takeNewPicture = (Button)findViewById(R.id.takenewpicture);
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
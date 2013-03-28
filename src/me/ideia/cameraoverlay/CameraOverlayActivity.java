package me.ideia.cameraoverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kaloer.filepicker.FilePickerActivity;

// TODO: Long click take screen shot -> big long click salva foto com o efeito atual sem foto base
// TODO: Configurações da camera 
// TODO: Gravar ultima imagem e recarregar na abertura da aplicação
// TODO: Gravar configuraçoes e permitir carregar retornando ao estado "preferido"
// TODO: Redimensionamento da imagem base na view está distorcida -> nao casa com a camera
// TODO: (bloqueia escurecimento da tela) - ok
// TODO: Camera preview nao resgata a camera -> ok
// TODO: Menu flutuante para seleção de efeitos -> ok
// TODO: Controle de brilho da tela -> ok


public class CameraOverlayActivity extends Activity {

	Preview preview;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	LayoutInflater controlInflater = null;
	PhotoEffects photoBase;
	View effects;
	View viewControl;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    static String basefile;

    private int seekEffect = 0;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    boolean running = false;

    public void applyEffect(final View v) {
        // Handle item selection
    	if (!running) {
			running = true;
			final Button switchEffect = (Button)findViewById(R.id.switchEffect);
			final ProgressBar loadingSE = (ProgressBar)findViewById(R.id.loadingSE);
			final ProgressBar loadingSSE = (ProgressBar)findViewById(R.id.loadingSSE);
			final Button switchSeekEffect = (Button) findViewById(R.id.switchSeekEffect);
			switch (v.getId()) {
	            case R.id.alpha1: case R.id.alpha2: case R.id.alpha3: case R.id.alpha4:
	            case R.id.alpha5: case R.id.gray_scale: case R.id.highcontrast:
	            case R.id.contrastbw: case R.id.hue1: case R.id.hue2:
	            case R.id.edgedetect: case R.id.edgedetecttransparent:
	            	loadingSE.setVisibility(View.VISIBLE);
	            	switchEffect.setVisibility(View.INVISIBLE);
	            	break;
	        }

			new Handler().postDelayed(new Thread() {
				@Override
				public void run() {
			        switch (v.getId()) {
			            
			            case R.id.invert:
							photoBase.invert();
							if (photoBase.isInverted()) {
								v.setBackgroundResource(R.drawable.icon64);
				            	switchEffect.setBackgroundResource(R.drawable.invert);
							} else {
								v.setBackgroundResource(R.drawable.invert);
				            	switchEffect.setBackgroundResource(R.drawable.icon64);
							}
			                break;
			            case R.id.alpha1:
			            	photoBase.alpha1();
			            	switchEffect.setBackgroundResource(R.drawable.alpha1);
			            	break;
			            case R.id.alpha2:
			            	photoBase.alpha2();
			            	switchEffect.setBackgroundResource(R.drawable.alpha2);
			            	break;
			            case R.id.alpha3:
			            	photoBase.alpha3();
			            	switchEffect.setBackgroundResource(R.drawable.alpha3);
			            	break;
			            case R.id.alpha4:
			            	photoBase.alpha4();
			            	switchEffect.setBackgroundResource(R.drawable.alpha4);
			            	break;
			            case R.id.alpha5:
			            	photoBase.alpha5();
			            	switchEffect.setBackgroundResource(R.drawable.alpha5);
			            	break;
			            case R.id.gray_scale:
			            	photoBase.grayScale();
			            	switchEffect.setBackgroundResource(R.drawable.grayscale);
			            	break;
			            case R.id.highcontrast:
			            	photoBase.highContrast();
			            	switchEffect.setBackgroundResource(R.drawable.highcontrast);
			            	break;
			            case R.id.contrastbw:
			            	photoBase.contrastBW();
			            	switchEffect.setBackgroundResource(R.drawable.contrastbw);
			            	break;
			            case R.id.hue1:
			            	photoBase.hue1();
			            	switchEffect.setBackgroundResource(R.drawable.hue1);
			                break;
			            case R.id.hue2:
			            	photoBase.hue2();
			            	switchEffect.setBackgroundResource(R.drawable.hue2);
			                break;
			            case R.id.edgedetect:
			            	photoBase.edgeDetect();
			            	switchEffect.setBackgroundResource(R.drawable.edgedetect);
			                break;
			            case R.id.edgedetecttransparent:
			            	photoBase.edgeDetectTransparent();
			            	switchEffect.setBackgroundResource(R.drawable.edgedetect);
			                break;
			            case R.id.original:
			            	photoBase.resetEffect();
			            	switchEffect.setBackgroundResource(R.drawable.icon64);
			                break;
			        }
			        running = false;
	            	loadingSE.setVisibility(View.INVISIBLE);
	            	loadingSSE.setVisibility(View.INVISIBLE);
	            	switchEffect.setVisibility(View.VISIBLE);
	            	switchSeekEffect.setVisibility(View.VISIBLE);
				}
			}, 10);
    	}
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub
    	switch (item.getItemId()){
    	case R.id.aboutitem:
    		Intent activity = new Intent(CameraOverlayActivity.this, AboutActivity.class);
    		startActivity(activity);
    		break;
    	case R.id.preferences:
    		Intent preferences = new Intent(CameraOverlayActivity.this, PreferencesActivity.class);
    		startActivity(preferences);
    		break;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    public String picFileName(String format){
    	return getSdcardCameraOverlay()+"/"+
		 (new SimpleDateFormat("yyyyMMddHHmm").format(new Date())) +format;
    }
    public FileOutputStream newPicFile(String format) throws FileNotFoundException {
    	 return new FileOutputStream( new File(picFileName(format)) );
    }
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		setFullBrightness();
		
		try {
			File dir = new File(getSdcardCameraOverlay());
			if (!dir.exists()) dir.mkdir();
		} catch (Exception e) {
		}

		preview = new Preview(this);
		this.setContentView(preview);

		photoBase = new PhotoEffects(this);

		controlInflater = LayoutInflater.from(getBaseContext());
		viewControl = controlInflater.inflate(R.layout.main, null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(photoBase, layoutParamsControl);

		this.addContentView(viewControl, layoutParamsControl);
		
		effects = controlInflater.inflate(R.layout.effects, null);
		effects.setVisibility(View.GONE);
		LayoutParams layoutParamsEffects = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		this.addContentView(effects, layoutParamsEffects);

		final Button takeScreenshot = (Button) findViewById(R.id.takescreenshot);

		final Button takePicture = (Button) findViewById(R.id.takepicture);
		takePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (basefile == null) {
					basefile = picFileName(".jpg");
				}
				preview.takePicture(basefile);
				showTakeNewPicture(false);
				toast(getString(R.string.savingAs) + preview.file);
			}
		});
		takeScreenshot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                Toast waiting = getToast("Aguarde...", Toast.LENGTH_SHORT);
                while (! new File(preview.file).exists()) {
                	waiting.show();
                }
                //preview.stopCamera();
                
				Bitmap b = BitmapFactory.decodeFile(preview.file);
				b = photoBase.resizeBitmap(b);

			    Bitmap bitmap = Bitmap.createBitmap(b);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawBitmap(photoBase.getBmp(), 0, 0, photoBase.getPaint());
			
				if (bitmap == null) {
					toast("bitmap null");
				}
	            
	            try {
	            	FileOutputStream fos = newPicFile(".png");
	                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
	                fos.close();
	            } catch (FileNotFoundException e) {
	            	toast("Arquivo não encontrado ");
	            } catch (IOException e) {
	            	toast("IO Error");
	            }

				toast(getString(R.string.savingLayeredPicAs) + preview.file);
			}
		});
		final Button takeNewPicture = (Button) findViewById(R.id.takenewpicture);
		takeNewPicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				takeNewPicture();
			}
		});
		
		Button loadImage = (Button) findViewById(R.id.loadimage);
		loadImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					preview.stopCamera();
				} catch (Exception e) {
				}

				Intent i = new Intent(CameraOverlayActivity.this, FilePickerActivity.class);
				i.putExtra(FilePickerActivity.EXTRA_FILE_PATH, "/sdcard/CameraOverlay/");
				String[] filter = new String[] { "jpg", "jpeg", "png", "bmp", "gif" };
				i.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, filter);
				startActivityForResult(i, 1);
				showTakeNewPicture(true);
			}
		});


		final Button switchSeekEffect = (Button) findViewById(R.id.switchSeekEffect);
		final SeekBar alphaValue = (SeekBar) findViewById(R.id.alphaValue);
		alphaValue.setOnTouchListener(new View.OnTouchListener() {
			private boolean running = false;
			@Override
			public boolean onTouch(final View v, MotionEvent event) {
				final ProgressBar loadingSSE = (ProgressBar) findViewById(R.id.loadingSSE);

            	loadingSSE.setVisibility(View.VISIBLE);
            	switchSeekEffect.setVisibility(View.INVISIBLE);
				new Handler().postDelayed(new Thread() {
					@Override
					public void run() {
						if (!running) {
							running = true;
							int progress = ((SeekBar)v).getProgress();
							switch (seekEffect) {
							case 0:
								photoBase.setAlpha(progress);	
								break;
							case 1:
								photoBase.grid(progress * 100 / 255);	
								break;
							case 2:
								photoBase.horizontal(progress * 100 / 255);	
								break;
							case 3:
								photoBase.vertical(progress * 100 / 255);	
								break;
							default:
								break;
							}

			            	loadingSSE.setVisibility(View.INVISIBLE);
			            	switchSeekEffect.setVisibility(View.VISIBLE);
							running = false;
						}
					}
				}, 10);
				return false;
			}
		});

		switchSeekEffect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final TextView switchSeekEffectLabel = (TextView) findViewById(R.id.switchSeekEffectLabel);

				final ProgressBar loadingSSE = (ProgressBar) findViewById(R.id.loadingSSE);
            	loadingSSE.setVisibility(View.VISIBLE);
            	switchSeekEffect.setVisibility(View.INVISIBLE);
				new Handler().postDelayed(new Thread() {
					@Override
					public void run() {
						seekEffect++;
						if (seekEffect > 3)
							seekEffect = 0;
						switch (seekEffect) {
							case 0:
								alphaValue.setProgress(photoBase.getAlpha());
								switchSeekEffect.setBackgroundResource(R.drawable.alpha);
								switchSeekEffectLabel.setText(getString(R.string.alpha));
								break;
							case 1:
								alphaValue.setProgress(photoBase.getGrid() * 255 / 100);
								switchSeekEffect.setBackgroundResource(R.drawable.grid);
								switchSeekEffectLabel.setText(getString(R.string.grid));
								break;
							case 2:
								alphaValue.setProgress(photoBase.getHorizontal() * 255 / 100);
								switchSeekEffect.setBackgroundResource(R.drawable.horizontal);
								switchSeekEffectLabel.setText(getString(R.string.horizontal));
								break;
							case 3:
								alphaValue.setProgress(photoBase.getVertical() * 255 / 100);
								switchSeekEffect.setBackgroundResource(R.drawable.vertical);
								switchSeekEffectLabel.setText(getString(R.string.vertical));
								break;
							default:
								break;
						}

		            	loadingSSE.setVisibility(View.INVISIBLE);
		            	switchSeekEffect.setVisibility(View.VISIBLE);
					}
					
				}, 10);
			}
		});

	}

	public void takeNewPicture() {
		if (photoBase.withoutPicture()) {
			basefile = preview.file;
			try {
				loadBaseImage(getBitmapFromString(basefile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		showTakeNewPicture(true);
		try {
			preview.stopCamera();
		} catch (Exception e) {
			e.printStackTrace();
		}
		preview.startCamera();
	}

	public void showTakeNewPicture(boolean open) {
		final Button takePicture = (Button) findViewById(R.id.takepicture);
		final LinearLayout takeNewPictureWrapper = (LinearLayout) findViewById(R.id.takenewpicturewrapper);
		final LinearLayout takeScreenshotWrapper = (LinearLayout ) findViewById(R.id.screenshotwrapper);
		
		takePicture.setVisibility(open ? View.VISIBLE : View.GONE);
		takeNewPictureWrapper.setVisibility(open ? View.GONE : View.VISIBLE);
		takeScreenshotWrapper.setVisibility(open ? View.GONE : View.VISIBLE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			File selectedImage = new File(data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
			basefile = preview.file;
			try {
				loadBaseImage(getBitmapFromString(selectedImage.getPath()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			findViewById(R.id.invert).setBackgroundResource(R.drawable.invert);
			findViewById(R.id.switchEffect).setBackgroundResource(R.drawable.icon64);
		}
		try {
			preview.startCamera();
		} catch (Exception e) {
			toast("Ops!" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadBaseImage(Bitmap bmp ) {
		try {
			photoBase.setBitmap(bmp);
			photoBase.selectedPicture();
		} catch (Exception e) {
			toast("Ops!" + e.getMessage());
			e.printStackTrace();
		}
	}

	public Bitmap getBitmapFromString(String src)
			throws FileNotFoundException {
		Uri uri = Uri.fromFile(new File(src));
		return getBitmapFromURI(uri);
	}
		public Bitmap getBitmapFromURI(Uri uri )
		throws FileNotFoundException {
		InputStream imageStream = getContentResolver().openInputStream(uri);
		Bitmap bmp = BitmapFactory.decodeStream(imageStream);
		return bmp;
	}
		
	public Toast getToast(String dados, int duration) {
		return Toast.makeText(this, dados, duration);
	}

	public void toast(String dados) {
		final Toast t = getToast(dados, Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	private void setFullBrightness() {  
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = 1F;
		getWindow().setAttributes(layout);
	}
	
	public void toggleEffects(View v) {
		if (v.getId() == R.id.switchEffect) {
			if (effects.getVisibility() == View.GONE)
				effects.setVisibility(View.VISIBLE);
			else
				effects.setVisibility(View.GONE);
		}
	}

	private String getSdcardCameraOverlay() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("camera_overlay_directory", "/sdcard/CameraOverlay/");
	}
}
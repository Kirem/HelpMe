package pwr.project.getrawdata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GetRawData extends Activity implements OnClickListener, SensorEventListener, LocationListener{

	Button getData;
	TextView tvWysokosc;
	TextView tvSzerokosc;
	TextView tvDlugosc;
	TextView tvSzerGEO;
	TextView tvDlGEO;
	private Sensor accelerometer;
	private Srednia sr;

	private SensorManager mSensorManager;
	private LocationManager mLocationManager;

	private float[] mGravity = new float[3];
	private float[] mAccel = new float[3] ;
	private long mLastUpdate, mLastUpdateGPS;
	
	private final float mAlpha = 0.8f;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		

		sr = new Srednia();
		tvDlugosc = (TextView) findViewById(R.id.tvAccelerometer1);
		tvSzerokosc = (TextView) findViewById(R.id.tvAccelerometer2);
		tvWysokosc = (TextView) findViewById(R.id.tvAccelerometer3);
		tvSzerGEO = (TextView) findViewById(R.id.tvGPSLONG);
		tvDlGEO = (TextView) findViewById(R.id.TVGPSLATIT);
		
		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(accelerometer == null){
			Toast.makeText(this, "No accelerometer available", Toast.LENGTH_SHORT).show();
		}
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLastUpdate = System.currentTimeMillis();
		mLastUpdateGPS = System.currentTimeMillis();

	}

@Override
	public void onClick(View v) {
		if(v == getData){
		}
	}



	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);

	}





	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SensorManager.SENSOR_DELAY_UI, 0, this);


	}




	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				long actualTime = System.currentTimeMillis();
				if (actualTime - mLastUpdate > 10) {
					mLastUpdate = actualTime;
					
					float rawX = event.values[0];
					float rawY = event.values[1];
					float rawZ = event.values[2];
				
					mGravity[0] = lowPass(rawX, mGravity[0]);
					mGravity[1] = lowPass(rawY, mGravity[1]);
					mGravity[2] = lowPass(rawZ, mGravity[2]);
					
					mAccel[0] = highPass(rawX, mGravity[0]);
					mAccel[1] = highPass(rawY, mGravity[1]);
					mAccel[2] = highPass(rawZ, mGravity[2]);
					
					//tvDlugosc.setText(""+mAccel[0]);
					//tvSzerokosc.setText(""+mAccel[1]);
					//tvWysokosc.setText(""+mAccel[2]);
					try {
						 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("data.txt", Context.MODE_APPEND));
					        outputStreamWriter.write(rawX + " " + rawY + " " + rawZ + "\n");
					        outputStreamWriter.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					sr.pobieranieDanychACC(rawX, rawY, rawZ);
					if(sr.czyPelnaACC){
						//Log.i(getClass().getSimpleName(), sr.getSrednia()+"");
					try {
						 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("dataBlad.txt", Context.MODE_APPEND));
					        outputStreamWriter.write(sr.bladACC.get(sr.ileDanych-1).x + " " + sr.bladACC.get(sr.ileDanych-1).y + " " + sr.bladACC.get(sr.ileDanych-1).z + "\n");
					        outputStreamWriter.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("dataSredWlas.txt", Context.MODE_APPEND));
					        outputStreamWriter.write(sr.srednieWlasciweACC.get(sr.ileDanych-1).x + " " + sr.srednieWlasciweACC.get(sr.ileDanych-1).y + " " + sr.srednieWlasciweACC.get(sr.ileDanych-1).z + "\n");
					        outputStreamWriter.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				try {
					 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("dataSredSred.txt", Context.MODE_APPEND));
				        outputStreamWriter.write(sr.srednieZeSredniejACC.get(sr.ileDanych-1).x + " " + sr.srednieZeSredniejACC.get(sr.ileDanych-1).y + " " + sr.srednieZeSredniejACC.get(sr.ileDanych-1).z + "\n");
				        outputStreamWriter.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				
				}
		}
				}
		}
		
	}
	private float highPass(float current, float gravity) {

		return current - gravity;

	}
	private float lowPass(float current, float gravity){
		return gravity * mAlpha + current * (1 - mAlpha);
	}


	@Override
	public void onLocationChanged(Location location) {
		long actualTime = System.currentTimeMillis();
		if (actualTime - mLastUpdateGPS > 500) {
			mLastUpdateGPS = actualTime;

			
			tvDlGEO.setText(""+location.getLongitude());
			tvSzerGEO.setText(""+location.getLatitude());
			try {
				 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("dataGPS.txt", Context.MODE_APPEND));
			        outputStreamWriter.write(location.getLongitude() + " " + location.getLatitude() + "\n");
			        outputStreamWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}


	@Override
	public void onProviderEnabled(String provider) {
	}


	@Override
	public void onProviderDisabled(String provider) {	
	}

}

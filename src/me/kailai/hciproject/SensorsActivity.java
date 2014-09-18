package me.kailai.hciproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import me.kailai.hciproject.R;

import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;

public class SensorsActivity extends FragmentActivity implements
		SensorEventListener, OnClickListener, OnCheckedChangeListener {
	private static final String ORIENTATION_DIALOG = "orientation_dialog";
	private SensorManager sm;
	private Sensor lastSensor;
	private SensorView sensorView = null;
	private int lastDelay = SensorManager.SENSOR_DELAY_UI;
	private TextView txtStatus;
	private TextView txtFPS;
	private FPSCounter fpsCounter = new FPSCounter(20);

	private static final String FPS_FORMAT = "FPS = %2.2f";
	
	private String lastPlayingCommand = "Play";
	private String lastVolumeCommand = "None";
	private boolean init = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		prepareObjects();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateOrientation();
		startSensor();
		Log.e("Hci", "Command: Start"); 
		lastPlayingCommand = "Play";
		new HttpAsyncTask().execute("http://192.168.200.167:5000/start");
	}

	public void updateOrientation() {
		TextView tv = (TextView) findViewById(R.id.txt_orient);
		int orient = getRequestedOrientation();
		tv.setText(getText(R.string.orientation) + ": "
				+ DataHelpers.getNameForOrientation(orient));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.w(getClass().getCanonicalName(), "onConfigurationChanged");
	}

	@Override
	protected void onPause() {
		stopSensors();
		super.onPause();
		Log.e("Hci", "Command: Stop");
		lastPlayingCommand = "Stop";
		new HttpAsyncTask().execute("http://192.168.200.167:5000/stop");
	}

	private void prepareObjects() {
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		txtStatus = (TextView) findViewById(R.id.txt_status);
		txtFPS = (TextView) findViewById(R.id.txt_fps);
		sensorView = (SensorView) findViewById(R.id.sensor_view);
		CheckBox cbScale = (CheckBox) findViewById(R.id.cb_same_scale);
		cbScale.setOnCheckedChangeListener(this);
		findViewById(R.id.btn_info).setOnClickListener(this);
		findViewById(R.id.btn_screen_orien).setOnClickListener(this);
		prepareSensorsSpinner();
		prepareDelaySpinner();
	}

	private void prepareDelaySpinner() {
		Spinner spinSpeed = (Spinner) findViewById(R.id.spinner_delay);
		spinSpeed.setAdapter(new ArrayAdapter<DataHelpers.NamedValue>(this,
				android.R.layout.simple_spinner_item, DataHelpers.delays()));
		spinSpeed.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				DataHelpers.NamedValue sdl = (DataHelpers.NamedValue) parent
						.getItemAtPosition(position);
				delaySelected(sdl);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void prepareSensorsSpinner() {
		List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
		List<SensorLabel> labels = new ArrayList<SensorLabel>(sensors.size());
		Log.i(getClass().getCanonicalName(), "Sensors: " + sensors.size());
		for (Sensor s : sensors) {
			if(s.getName().equals("Gravity")) {
				Log.i(getClass().getCanonicalName(), "Sensor: " + s.getName());
				labels.add(new SensorLabel(s));
			}
		}
		Spinner spinSensor = (Spinner) findViewById(R.id.spinner_sensor);
		spinSensor.setAdapter(new ArrayAdapter<SensorLabel>(this,
				android.R.layout.simple_spinner_item, labels));
		spinSensor.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				SensorLabel label = (SensorLabel) parent
						.getItemAtPosition(position);
				sensorSelected(label.sensor);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				sensorSelected(null);
			}
		});

	}

	private void startSensor() {
		if (lastSensor != null) {
			sm.registerListener(this, lastSensor, lastDelay);
		}
	}

	void sensorSelected(Sensor sensor) {
		if (lastSensor != null) {
			sm.unregisterListener(this, lastSensor);
		}
		sensorView.reset();
		fpsCounter.clear();
		txtStatus.setText(R.string.waiting_for_reading);
		lastSensor = sensor;
		if (lastSensor != null) {
			sm.registerListener(this, lastSensor, lastDelay);
		}
	}

	void delaySelected(DataHelpers.NamedValue sdl) {
		lastDelay = sdl.value;
		if (lastSensor != null) {
			sm.unregisterListener(this, lastSensor);
			sm.registerListener(this, lastSensor, lastDelay);
		}
	}

	protected void stopSensors() {
		if (sm != null) {
			sm.unregisterListener(this);
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!event.sensor.equals(lastSensor)) {
			return;
		}
//		if(init == true) {
//			init = false;
//			Log.e("Hci", "Command: Start"); 
//			lastPlayingCommand = "Play";
//			new HttpAsyncTask().execute("http://192.168.200.167:5000/start");
//		}
		float fps = fpsCounter.update();
		sensorView.updateSensor(event.values);
		if(event.values[2] >= 7 && lastPlayingCommand == "Pause") {
			Log.e("Hci", "Command: Play"); 
			lastPlayingCommand = "Play";
			new HttpAsyncTask().execute("http://192.168.200.167:5000/play");
		}
		else if(event.values[2] <= -7 && lastPlayingCommand == "Play") {
			Log.e("Hci", "Command: Pause");
			lastPlayingCommand = "Pause";
			new HttpAsyncTask().execute("http://192.168.200.167:5000/pause");
		}
		if(event.values[0] >= 5) {
			Log.e("Hci", "Command: Volume-20"); 
			lastVolumeCommand = "-20";
			new HttpAsyncTask().execute("http://192.168.200.167:5000/vdown");
		}
		else if(event.values[0] <= -5) {
			Log.e("Hci", "Command: Volume+20");
			lastVolumeCommand = "+20";
			new HttpAsyncTask().execute("http://192.168.200.167:5000/vup");
		}
		String accur = DataHelpers.accuracyString(event.accuracy);
		txtStatus.setText(getText(R.string.accuracy) + ": " + accur);
		txtFPS.setText(String.format(FPS_FORMAT, fps));
	}
	
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
 
        return result;
    }
    
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }

	private void showSensorInfo() {
		FragmentManager fm = getSupportFragmentManager();
		SensorDialogFragment sdf = new SensorDialogFragment();
		sdf.setSensor(lastSensor);
		sdf.show(fm, "sensor_dialog");
	}

	private void showOrientationDialog() {
		OrientationDialog od = new OrientationDialog();
		od.show(getSupportFragmentManager(), ORIENTATION_DIALOG);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_info) {
			showSensorInfo();
		} else if (v.getId() == R.id.btn_screen_orien) {
			showOrientationDialog();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.cb_same_scale) {
			sensorView.setCommonScales(isChecked);
		}
	}

	private static class SensorLabel {
		private final Sensor sensor;

		public SensorLabel(Sensor sensor) {
			super();
			this.sensor = sensor;
		}

		@Override
		public String toString() {
			return sensor.getName();
		}
	}
	
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
 
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
       }
    }
}
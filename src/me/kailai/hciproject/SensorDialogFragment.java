package me.kailai.hciproject;

import java.lang.reflect.Method;

import me.kailai.hciproject.R;

import android.hardware.Sensor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SensorDialogFragment extends DialogFragment implements
		OnClickListener {

	private LinearLayout senLayout;
	private Sensor sensor;

	public SensorDialogFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sensor_dialog_layout, container);
		View ok = view.findViewById(R.id.btn_ok);
		ok.setOnClickListener(this);
		senLayout = (LinearLayout) view.findViewById(R.id.layout_sensor);
		showSensor();
		return view;
	}

	private void showSensor() {
		if (getActivity() != null && sensor != null) {
			getDialog().setTitle(sensor.getName());
			senLayout.removeAllViews();
			Method[] methods = Sensor.class.getMethods();
			for (Method m : methods) {
				String name = m.getName();
				if (name.startsWith("get") || name.startsWith("is")) {
					try {
						Object v = m.invoke(sensor, (Object[]) null);
						TextView tv = new TextView(getActivity());
						tv.setText(name + " = " + String.valueOf(v));
						senLayout.addView(tv);
					} catch (Exception e) {
						Log.e(getClass().getCanonicalName(), e.getMessage());
					}
				}
			}
		}
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
		showSensor();
	}

	@Override
	public void onClick(View v) {
		this.dismiss();
	}
}

package me.kailai.hciproject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.util.Log;

public class DataHelpers {

	private static final int CUT_SCREEN_ORIENTATION = 19;
	private static final int CUT_SENSOR_DELAY = 7;

	private static final String[] ORIENTATIONS = { "SCREEN_ORIENTATION_BEHIND",
			"SCREEN_ORIENTATION_FULL_SENSOR", "SCREEN_ORIENTATION_LANDSCAPE",
			"SCREEN_ORIENTATION_NOSENSOR", "SCREEN_ORIENTATION_PORTRAIT",
			"SCREEN_ORIENTATION_REVERSE_LANDSCAPE",
			"SCREEN_ORIENTATION_REVERSE_PORTRAIT", "SCREEN_ORIENTATION_SENSOR",
			"SCREEN_ORIENTATION_SENSOR_LANDSCAPE",
			"SCREEN_ORIENTATION_SENSOR_PORTRAIT",
			"SCREEN_ORIENTATION_UNSPECIFIED", "SCREEN_ORIENTATION_USER" };
	private static final String[] SENSOR_DELAYS = { "SENSOR_DELAY_NORMAL",
			"SENSOR_DELAY_UI", "SENSOR_DELAY_GAME", "SENSOR_DELAY_FASTEST" };

	private static NamedValue[] ORIENTATION_VALUES;
	private static NamedValue[] SENSOR_DELAY_VALUES;

	public static String accuracyString(int v) {
		switch (v) {
		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
			return "HIGH";
		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
			return "MEDIUM";
		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
			return "LOW";
		case SensorManager.SENSOR_STATUS_UNRELIABLE:
			return "UNRELIABLE";
		default:
			return "??? [" + v + "]";
		}
	}

	public static NamedValue[] delays() {
		if (SENSOR_DELAY_VALUES == null) {
			SENSOR_DELAY_VALUES = getValuesForFields(SENSOR_DELAYS,
					SensorManager.class, CUT_SENSOR_DELAY);
		}
		return SENSOR_DELAY_VALUES;
	}

	public static NamedValue[] orientations() {
		if (ORIENTATION_VALUES == null) {
			ORIENTATION_VALUES = getValuesForFields(ORIENTATIONS,
					ActivityInfo.class, CUT_SCREEN_ORIENTATION);
		}
		return ORIENTATION_VALUES;
	}

	public static String getNameForDelay(int val) {
		return getNameForValue(val, delays());
	}

	public static String getNameForOrientation(int val) {
		return getNameForValue(val, orientations());
	}

	public static String getNameForValue(int val, NamedValue[] values) {
		for (NamedValue nv : values) {
			if (val == nv.value) {
				return nv.name;
			}
		}
		return "[???]";
	}

	public static NamedValue[] getValuesForFields(String[] fields,
			Class<?> clazz, int cut) {
		List<NamedValue> values = new ArrayList<NamedValue>(fields.length);
		for (String name : fields) {
			Integer i = getFieldValue(name, clazz);
			if (i != null) {
				values.add(new NamedValue(name.substring(cut), i.intValue()));
			}
		}
		return values.toArray(new NamedValue[values.size()]);
	}

	private static Integer getFieldValue(String name, Class<?> clazz) {
		try {
			Field field = clazz.getField(name);
			return Integer.valueOf(field.getInt(null));
		} catch (Exception e) {
			Log.i("DataHelper",
					"cannot access field: " + name + " - " + e.getMessage());
			return null;
		}
	}

	public static class NamedValue {
		public final String name;
		public final int value;

		public NamedValue(String name, int value) {
			super();
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}

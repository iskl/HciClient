package me.kailai.hciproject;

import me.kailai.hciproject.DataHelpers.NamedValue;

import me.kailai.hciproject.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class OrientationDialog extends DialogFragment {

	private NamedValue[] values;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.screen_orientation);
		values = DataHelpers.orientations();
		builder.setSingleChoiceItems(new OrientationAdapter(getActivity(),
				values), findSelected(), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				orientationSelected(values[which].value);
			}
		});

		return builder.create();
	}

	private int findSelected() {
		int or = getActivity().getRequestedOrientation();
		for (int i = 0; i < values.length; i++) {
			if (values[i].value == or) {
				return i;
			}
		}
		return 0;
	}

	protected void orientationSelected(int value) {
		SensorsActivity sa = (SensorsActivity) getActivity();
		sa.setRequestedOrientation(value);
		sa.updateOrientation();
	}

	private class OrientationAdapter extends ArrayAdapter<NamedValue> {

		public OrientationAdapter(Context context, NamedValue[] objects) {
			super(context, android.R.layout.select_dialog_singlechoice, objects);
		}

		private TextView createTextView(ViewGroup parent) {
			LayoutInflater li = getLayoutInflater(null);
			return (TextView) li.inflate(
					android.R.layout.select_dialog_singlechoice, parent, false);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (convertView == null ? createTextView(parent)
					: (TextView) convertView);
			NamedValue bi = getItem(position);
			tv.setText(bi.name);
			return tv;
		}

	}
}

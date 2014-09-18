package me.kailai.hciproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SensorView extends View {

	private static final int COLOR_TEXT = Color.BLACK;
	private static final int COLOR_MIDDLE = Color.BLUE;
	private static final int COLOR_BACKGROUND = Color.WHITE;
	private static final int COLOR_BORDER_TOP = Color.RED;
	private static final int COLOR_BORDER_BOTTOM = Color.GREEN;
	private static final int COLOR_POINTS = Color.RED;

	private static final float RESCALE_FACTOR = 1.2f;

	private static final String FLOAT_FORMAT = "%2.2f";

	private Paint paint;
	// private float[][] data;
	private DataRow[] data;
	private int counter;
	private int size;
	private int lastDrawn;

	private Canvas backCanvas;
	private Bitmap backBmp;

	private boolean commonScales;

	public SensorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SensorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SensorView(Context context) {
		super(context);
		init();
	}

	public void setCommonScales(boolean commonScales) {
		this.commonScales = commonScales;
		if (commonScales) {
			rescaleAll();
		}
	}

	public boolean isCommonScales() {
		return commonScales;
	}

	private void init() {
		paint = new Paint();
		paint.setColor(Color.RED);

	}

	/**
	 * Resets view, so it will be prepared for data from new sensor
	 */
	public void reset() {
		// this.sensor = sensor;
		data = null;
		counter = 0;
		lastDrawn = 0;
		size = getWidth();
		data = null;
		redoBackground();
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		reset();
	}

	private void redoBackground() {
		if (backBmp != null) {
			backBmp.recycle();
		}
		backBmp = null;
		backCanvas = null;
		int w = getWidth();
		int h = getHeight();
		if (w < 1 || h < 1) {
			return;
		}
		prepareBackground();
	}

	private void prepareBackground() {
		backCanvas = new Canvas();
		backBmp = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.RGB_565);
		backBmp.eraseColor(Color.WHITE);
		backCanvas.setBitmap(backBmp);
	}

	public void updateSensor(float[] data) {
		if (size < 1) {
			Log.w(getClass().getCanonicalName(), "ojoj: no size");
			return;

		}
		if (this.data == null) {
			initData(data);
		}
		for (int i = 0; i < data.length; i++) {
			this.data[i].update(counter, data[i]);
		}
		checkRescaling();
		counter++;
		if (counter >= size) {
			counter = 0;
			setForRepaint();
		}
		invalidate();
	}

	private void checkRescaling() {
		if (commonScales) {
			for (DataRow dr : data) {
				if (dr.mustRescale) {
					rescaleAll();
					return;
				}
			}
		}
	}

	private void setForRepaint() {
		lastDrawn = 0;
		for (DataRow dr : data) {
			dr.repaintPlot = true;
		}
	}

	private void initData(float[] d) {
		int len = d.length;
		this.data = new DataRow[len];
		float h = getHeight();
		float textH = Math.abs(paint.ascent());
		float rowH = h / len;
		float plotH = rowH - textH;
		for (int i = 0; i < len; i++) {
			DataRow dr = new DataRow(size);
			dr.topPixel = i * rowH;
			dr.pixelDelta = plotH / 2;
			dr.middlePixel = dr.topPixel + plotH / 2;
			dr.bottomPixel = dr.topPixel + plotH;
			dr.textPixel = rowH * (i + 1);
			data[i] = dr;
		}
	}

	private boolean canDraw() {
		if (backBmp == null || backCanvas == null) {
			return false;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!canDraw()) {
			super.onDraw(canvas);
			return;
		}
		if (lastDrawn == counter) {
			canvas.drawBitmap(backBmp, 0, 0, paint);
			return;
		}
		for (DataRow dr : data) {
			drawRow(dr, backCanvas);
		}
		canvas.drawBitmap(backBmp, 0, 0, paint);
		lastDrawn = counter;
	}

	private void drawRow(DataRow dr, Canvas c) {
		int start = lastDrawn;
		int end = counter;
		boolean needRescale = dr.mustRescale;
		if (needRescale) {
			clearRow(dr, c);
			dr.mustRescale = false;
			start = 0;
			end = size;
		} else if (dr.repaintPlot) {
			end = size;
		}
		updateRow(dr, c, start, end);
		if (dr.repaintPlot || needRescale) {
			drawCoordinates(c, dr);
		}
		markEnd(dr, counter, c);
	}

	private void clearRow(DataRow dr, Canvas c) {
		c.save();
		c.clipRect(0, dr.topPixel, getWidth(), dr.textPixel);
		c.drawColor(COLOR_BACKGROUND);
		c.restore();
	}

	private void updateRow(DataRow dr, Canvas c, int start, int end) {
		paint.setColor(COLOR_POINTS);
		if (start == 0 && end > 0) {
			drawClearPoint(dr, c, start);
			start = 1;
		}
		for (int i = start; i < end; i++) {
			drawClearLine(dr, c, i);
		}
	}

	private void drawClearPoint(DataRow dr, Canvas c, int i) {
		float y = getYForValue(dr, i);
		clearLine(dr, i, c);
		paint.setColor(COLOR_POINTS);
		c.drawPoint(i, y, paint);
	}

	private void drawClearLine(DataRow dr, Canvas c, int i) {
		float y = getYForValue(dr, i);
		float ly = getYForValue(dr, i - 1);
		clearLine(dr, i, c);
		paint.setColor(COLOR_POINTS);
		c.drawLine(i - 1, ly, i, y, paint);
	}

	private void clearLine(DataRow dr, int i, Canvas c) {
		paint.setColor(COLOR_BACKGROUND);
		c.drawLine(i, dr.topPixel, i, dr.bottomPixel, paint);
		paint.setColor(COLOR_BORDER_TOP);
		c.drawPoint(i, dr.topPixel, paint);
		paint.setColor(COLOR_MIDDLE);
		c.drawPoint(i, dr.middlePixel, paint);
		paint.setColor(COLOR_BORDER_BOTTOM);
		c.drawPoint(i, dr.bottomPixel, paint);
	}

	private void markEnd(DataRow dr, int x, Canvas c) {
		paint.setColor(Color.BLACK);
		c.drawLine(x, dr.topPixel, x, dr.bottomPixel, paint);
	}

	private float getYForValue(DataRow dr, int i) {
		float v = dr.data[i];
		float d = (v - dr.midValue) / dr.delta;
		float h = dr.middlePixel - (d * dr.pixelDelta);
		return h;
	}

	private void drawCoordinates(Canvas c, DataRow dr) {
		drawHorizontalLine(c, dr.middlePixel, COLOR_MIDDLE);
		drawHorizontalLine(c, dr.topPixel, COLOR_BORDER_TOP);
		drawHorizontalLine(c, dr.bottomPixel, COLOR_BORDER_BOTTOM);

		drawValueText(c, dr.topPixel + Math.abs(paint.ascent()), dr.maxValue,
				COLOR_TEXT);
		drawValueText(c, dr.middlePixel, dr.midValue, COLOR_TEXT);
		drawValueText(c, dr.bottomPixel, dr.minValue, COLOR_TEXT);
		dr.repaintPlot = false;
	}

	private void drawValueText(Canvas c, float height, float value, int color) {
		String str = String.format(FLOAT_FORMAT, value);
		Rect r = new Rect();
		paint.getTextBounds(str, 0, str.length(), r);
		paint.setColor(color);
		c.drawText(str, getWidth() - r.width() - 10, height, paint);
	}

	private void drawHorizontalLine(Canvas c, float height, int color) {
		paint.setColor(color);
		c.drawLine(0, height, getWidth(), height, paint);
	}

	private void rescaleAll() {
		if (data == null) {
			return;
		}
		float max = 1;
		float min = -1;
		for (DataRow dr : data) {
			max = Math.max(max, dr.maxValue);
			min = Math.min(min, dr.minValue);
		}
		for (DataRow dr : data) {
			dr.setMinMax(min, max);
		}
	}

	private static class DataRow {
		float topPixel;
		float middlePixel;
		float bottomPixel;
		float pixelDelta;
		float textPixel;
		float maxValue = 1;
		float midValue = 0;
		float minValue = -1;
		float delta = 1;
		float[] data;
		boolean mustRescale = true;
		boolean repaintPlot = false;

		public DataRow(int size) {
			data = new float[size];
		}

		void update(int i, float v) {
			data[i] = v;
			if (v > maxValue) {
				maxValue = (v - midValue) * RESCALE_FACTOR + midValue;
				mustRescale = true;
			} else if (v < minValue) {
				minValue = (v - midValue) * RESCALE_FACTOR + midValue;
				mustRescale = true;
			}
			if (mustRescale) {
				midValue = (maxValue + minValue) / 2;
				delta = maxValue - midValue;
			}
		}

		void setMinMax(float min, float max) {
			mustRescale = true;
			minValue = min;
			maxValue = max;
			midValue = (maxValue + minValue) / 2;
			delta = maxValue - midValue;
		}
	}
}

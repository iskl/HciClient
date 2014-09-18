package me.kailai.hciproject;

import java.util.Arrays;

public class FPSCounter {

	private final int size;
	private final long[] history;

	private long sum;
	private int counter;
	private long lastTimestamp;
	private boolean filled;

	public FPSCounter(int size) {
		super();
		this.size = size;
		this.history = new long[size];
	}

	public void clear() {
		Arrays.fill(history, 0);
		counter = 0;
		sum = 0;
		filled = false;
		lastTimestamp = System.currentTimeMillis();
	}

	public float update() {
		long now = System.currentTimeMillis();
		long diff = now - lastTimestamp;
		lastTimestamp = now;
		sum -= history[counter];
		sum += diff;
		history[counter] = diff;
		counter++;
		if (counter >= size) {
			counter = 0;
			filled = true;
		}
		return getAverage();
	}

	// Average
	public float getAverage() {
		if (!filled) {
			return 0;
		}
		if (sum == 0) {
			return Float.POSITIVE_INFINITY;
		}
		float s = sum;
		float v = (size * 1000f) / s;
		return v;
	}
}

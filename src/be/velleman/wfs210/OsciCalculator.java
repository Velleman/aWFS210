package be.velleman.wfs210;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import android.util.Log;

public class OsciCalculator {
	Boolean isRunning = false;
	WFS210 scope;
	MyGLRenderer renderer;
	Timer timer;
	float Vdc1 = 0;
	float Vdc2 = 0;
	float RMS1 = 0;

	float RMS2 = 0;
	float TRMS1 = 0;
	float TRMS2 = 0;
	float Vmax1 = 0;
	float Vmax2 = 0;
	float Vmin1 = 0;
	float Vmin2 = 0;
	float Vpkpk1 = 0;
	float Vpkpk2 = 0;
	float dbM1 = 0;
	float dbM2 = 0;
	float dbGain = 0;
	float W1rms2 = 0;
	float W1rms4 = 0;
	float W1rms8 = 0;
	float W1rms16 = 0;
	float W1rms32 = 0;
	float W2rms2 = 0;
	float W2rms4 = 0;
	float W2rms8 = 0;
	float W2rms16 = 0;
	float W2rms32 = 0;
	float dV1 = 0;
	float dV2 = 0;
	float dt = 0;
	float Freq = 0;
	Boolean validSignals = false;

	public OsciCalculator(WFS210 scope, MyGLRenderer rend) {
		this.scope = scope;
		this.renderer = rend;
	}

	/**
	 * @param d
	 *            Value that needs to be formatted into a string
	 * @return returns the formated string
	 */
	public static String fmt(float d) {
		if (d == (int) d)
			return String.format("%.2f", d);
		else
			return String.format("%.2f", d);
	}

	/**
	 * @param ch
	 *            The channel which Vdc needs to be calculated
	 * @return the DC voltage of the given channel
	 */
	public float calculateVdc(Channel ch) {
		float Vdc = 0;
		byte[] data = new byte[4096];
		System.arraycopy(ch.getSamples(), 0, data, 0, 4096);
		for (int i = 0; i < 4096; i++) {
			Vdc += (255 - unsignedToBytes(data[i]))
					- (255 - ch.getVerticalPosition());
		}
		Vdc /= 4096;
		Vdc *= (scope.getFloatFromVoltageDiv(ch) / 25);
		return Vdc * (ch.getIsX10() ? 10 : 1);
	}

	/**
	 * @param Vrms
	 *            The Vrms that is needed for the ratio calculating between Vrms
	 *            and 0dBm(0.775V)
	 * @return Retuns the calculated ratio
	 */
	public float calculateDb(Channel channel) {
		float Vrms = calculateRms(channel);
		float db = 0;
		db = (float) (20 * Math.log10((Vrms / 0.775)));
		return db;
	}

	/**
	 * @param ch
	 *            The channel which Vmin needs to be calculated
	 * @return Returns the minimum voltage of the channel
	 */
	public float calculateVMin(Channel ch) {

		float volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data = new byte[4096];
		int[] idata;
		System.arraycopy(ch.getSamples(), 0, data, 0, 4096);
		float result;
		if (data.length != 0) {
			idata = new int[data.length];
			for (int i = 0; i < data.length; i++) {
				idata[i] = unsignedToBytes(data[i]) - ch.getVerticalPosition();
			}
			Arrays.sort(idata);
			float Vmin = idata[0];
			Vmin = Vmin * (volt2 / 25);
			result = Vmin * (ch.getIsX10() ? 10 : 1);
		} else
			result = 0;

		return result;
	}

	/**
	 * Returns the Vmax of the specified channel
	 * 
	 * @param ch
	 *            The channel which Vmax needs to be calculated
	 * @return the maximum voltage of the channel
	 */
	public float calculateVMax(Channel ch) {
		float Vmax = 0;
		float volt2 = 1;
		volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data = new byte[4096];
		int[] idata;
		float result;
		System.arraycopy(ch.getSamples(), 0, data, 0, 4096);
		if (data.length != 0) {
			idata = new int[data.length];
			for (int i = 0; i < data.length; i++) {
				idata[i] = 255 - unsignedToBytes(data[i])
						- ch.getVerticalPosition();
			}
			Arrays.sort(idata);
			Vmax = idata[idata.length - 1];
			Vmax = Vmax * (volt2 / 25);
			result = Vmax * (ch.getIsX10() ? 10 : 1);
		} else
			result = 0;
		return result;
	}

	/**
	 * Returns the RMS of the specified channel
	 * 
	 * @param ch
	 *            The channel where the RMS needs to be calculated
	 * @return Calculated Rms value from the given channel
	 */
	public float calculateRms(Channel ch) {
		float RMS = 0;
		float volt2 = 0;
		volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data2 = new byte[4096];
		System.arraycopy(ch.getSamples(), 0, data2, 0, 4096);
		float average2 = 0;
		float buf3 = 25;
		float voltpersample2 = volt2 / buf3;
		float buf4 = 0;
		for (int i = 0; i <= data2.length - 1; i++) {
			buf4 = unsignedToBytes(data2[i]);
			average2 += (buf4);
		}
		average2 = (average2 / data2.length);
		for (int i = 0; i <= data2.length - 1; i++) {
			RMS += Math.pow(((unsignedToBytes(data2[i])) - average2), 2);
		}
		RMS = RMS / (4096);
		RMS = (float) Math.sqrt(RMS);
		RMS = RMS * voltpersample2;

		return RMS * (ch.getIsX10() ? 10 : 1);
	}

	/**
	 * Returns the TRMS of the specified channel
	 * 
	 * @param ch
	 *            The channel where the TRMS needs to be calculated
	 * @return Calculated TRms value from the given channel
	 */
	public float calculateTRms(Channel ch) {
		float RMS = 0;
		float volt2 = 0;
		volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data2 = new byte[4096];
		System.arraycopy(ch.getSamples(), 0, data2, 0, 4096);
		float buf3 = 25;
		float voltpersample2 = volt2 / buf3;
		for (int i = 0; i <= data2.length - 1; i++) {
			RMS += Math.pow(
					(unsignedToBytes(data2[i]) - ch.getVerticalPosition()), 2);
		}
		RMS = RMS / 4096;
		RMS = (float) Math.sqrt(RMS);
		RMS = RMS * voltpersample2;

		return RMS * (ch.getIsX10() ? 10 : 1);
	}

	/**
	 * Converts signed byte to unsigned
	 * 
	 * @param b
	 *            the byte that needs to be converted
	 * @return a byte that is unsigned
	 */
	private static int unsignedToBytes(byte b) {
		int result = b & 0xFF;
		return result;
	}

	private float calculateDV(float y1, float y2, Channel channel) {
		float result;
		float difference;
		float volt = (channel.getIsX10() ? 10 : 1)
				* scope.getFloatFromVoltageDiv(channel);
		difference = Math.abs(y1 - y2);
		result = (volt / 25.5f) * difference;
		return result;
	}

	/**
	 * Calculates the time between the 2 XMarkers and sends it to the activity
	 * 
	 * @param difference
	 *            value between 2 markers
	 */
	public void calculateTime(float x1, float x2) {
		calculateDT(x1, x2);
		Freq = 1 / dt;
	}

	/**
	 * Calculates the time between the 2 XMarkers and sends it to the activity
	 * 
	 * @param difference
	 *            value between 2 markers
	 */
	public void calculateDT(float x1, float x2) {
		float fTimebase = scope.getFloatFromTimeBase();
		float totaltime = fTimebase * scope.totalDivisions;
		float timePerSample = totaltime / scope.totalSamples;
		dt = timePerSample * Math.abs(x2 - x1);
	}

	public void setScope(WFS210 newScope) {
		scope = newScope;
	}

	public String getVdc(Channel channel) {
		return fmt(calculateVdc(channel)) + "V";
	}

	public String getRMS(Channel channel) {

		return fmt(calculateRms(channel)) + "V";

	}

	public String getTRMS(Channel channel) {

		return fmt(calculateTRms(channel)) + "V";

	}

	public String getVmax(Channel channel) {

		return fmt(calculateVMax(channel)) + "V";

	}

	public String getVmin(Channel channel) {

		return fmt(calculateVMin(channel)) + "V";

	}

	public String getVpkpk(Channel channel) {

		float Vptp = Math.abs(calculateVMax(channel))
				+ Math.abs(calculateVMin(channel));
		return fmt(Vptp) + "V";

	}

	public String getDbM(Channel channel) {

		return fmt(calculateDb(channel)) + "dB";

	}

	public String getDbGain(Channel channel1, Channel channel2) {

		return fmt(calculateDb(channel2) - calculateDb(channel1)) + "dB";

	}

	public String getWrms(Channel channel, int watt) {

		float rms = calculateRms(channel);
		return fmt(rms * (rms / watt)) + "W";

	}

	public String getdV(float y1, float y2, Channel channel) {
		float dv = calculateDV(y1, y2, channel);
		if (dv >= 1) {
			String format = String.format("%.2f", dv);
			return " " + format + "V";
		} else {
			return fmt(dv * 1000) + "mV";
		}
	}

	public String getdt() {
		try {
			calculateDT(renderer.xMarker1.getPosition().x,
					renderer.xMarker2.getPosition().x);
		} catch (Exception e) {

		}
		if (dt != 0) {
			if (dt >= 1) {
				return String.format("%.2f", dt) + " s";
			} else {
				if (dt > 0.001 && dt <= 0.99) {
					return String.format("%.2f", dt * 1000) + " ms";
				} else {
					if (dt > 0.000001 && dt <= 0.000999) {
						return String.format("%.2f", dt * 1000000) + " µs";
					}
				}
			}
		}
		else
			return "0s";
		return "???";

	}

	public String getFreq() {
		calculateDT(renderer.xMarker1.getPosition().x,
				renderer.xMarker2.getPosition().x);
		float freq = 1 / dt;
		if (freq <= 1000000) {
			if (freq <= 1000)
				return String.format("%.1f", freq) + " Hz";
			else
				return String.format("%.1f", freq / 1000) + " KHz";
		} else
			return String.format("%.1f", freq / 1000000) + " MHz";

	}

}

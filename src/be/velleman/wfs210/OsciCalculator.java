package be.velleman.wfs210;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.R.bool;

public class OsciCalculator
{
	Boolean isRunning = false;
	WFS210 scope;
	MyGLRenderer renderer;
	Map<String, String> measurements = new HashMap<String, String>();
	private List<WFS210MeasurementsListener> measurementsListeners = new ArrayList<WFS210MeasurementsListener>();
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

	public OsciCalculator(WFS210 scope2, MyGLRenderer rend)
	{
		scope = scope2;
		renderer = rend;
	}

	public void addMeasurementsListener(WFS210MeasurementsListener wml)
	{
		measurementsListeners.add(wml);
	}

	public void notifyUpdatedMeasurements(Map<String, String> measurements)
	{
		for (WFS210MeasurementsListener wml : measurementsListeners)
		{
			wml.updatedMeasurements();
		}
	}

	/**
	 * Calculates every measurement every second and sending it to the specified
	 * handler
	 * 
	 * @param h
	 *            the handler which is used to send the measurements to
	 */
	public void startCalculating()
	{
		if (!isRunning)
		{

			isRunning = true;
			timer = new Timer("Calc-Timer");

			timer.scheduleAtFixedRate(new TimerTask()
			{

				@Override
				public void run()
				{
					// Your database code here

					if (ValidateSignals())
					{
						validSignals = true;
						Vdc1 = calculateVdc(scope.getChannel1());
						Vdc2 = calculateVdc(scope.getChannel2());
						RMS1 = calculateRms(scope.getChannel1());
						RMS2 = calculateRms(scope.getChannel2());
						Vmax1 = calculateVMax(scope.getChannel1());
						Vmax2 = calculateVMax(scope.getChannel2());
						Vmin1 = calculateVMin(scope.getChannel1());
						Vmin2 = calculateVMin(scope.getChannel2());
						Vpkpk1 = Math.abs(Vmax1) + Math.abs(Vmin1);
						Vpkpk2 = Math.abs(Vmax2) + Math.abs(Vmin2);
						TRMS1 = calculateTRms(scope.getChannel1());
						TRMS2 = calculateTRms(scope.getChannel2());
						dbM1 = calculateDb(RMS1);
						dbM2 = calculateDb(RMS2);
						W1rms2 = RMS1 * (RMS1 / 2);
						W1rms4 = RMS1 * (RMS1 / 4);
						W1rms8 = RMS1 * (RMS1 / 8);
						W1rms16 = RMS1 * (RMS1 / 16);
						W1rms32 = RMS1 * (RMS1 / 32);
						W2rms2 = RMS1 * (RMS1 / 2);
						W2rms4 = RMS1 * (RMS1 / 4);
						W2rms8 = RMS1 * (RMS1 / 8);
						W2rms16 = RMS1 * (RMS1 / 16);
						W2rms32 = RMS1 * (RMS1 / 32);
						dbGain = dbM2 - dbM1;
					} else
						validSignals = false;
					if (renderer != null && renderer.yMarker1 != null)
					{
						calculateDV(renderer.yMarker1.getPosition().y, renderer.yMarker2
								.getPosition().y);
						calculateTime(renderer.xMarker1.getPosition().x, renderer.xMarker2
								.getPosition().x);
					}
					notifyUpdatedMeasurements(measurements);

				}
			}, 500, 500);
		}

	}

	/**
	 * @param d
	 *            Value that needs to be formatted into a string
	 * @return returns the formated string
	 */
	public static String fmt(float d)
	{
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
	public float calculateVdc(Channel ch)
	{
		float Vdc = 0;
		byte[] data = new byte[4096];
		System.arraycopy(ch.getSamples(), 0, data, 0, 4096);
		for (int i = 0; i < 4096; i++)
		{
			Vdc += unsignedToBytes(data[i]) - ch.getVerticalPosition();
		}
		Vdc /= 4096;
		//Vdc -= ch.getVerticalPosition();
		Vdc *= (scope.getFloatFromVoltageDiv(ch) / 25);
		return Vdc * (ch.getIsX10() ? 10 : 1);
	}

	/**
	 * @param Vrms
	 *            The Vrms that is needed for the ratio calculating between Vrms
	 *            and 0dBm(0.775V)
	 * @return Retuns the calculated ratio
	 */
	public float calculateDb(float Vrms)
	{
		float db = 0;
		db = (float) (20 * Math.log10((Vrms / 0.775)));
		return db;
	}

	/**
	 * @param ch
	 *            The channel which Vmin needs to be calculated
	 * @return Returns the minimum voltage of the channel
	 */
	public float calculateVMin(Channel ch)
	{

		float volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data = new byte[4096];
		int[] idata;
		System.arraycopy(ch.getSamples(), 0, data, 0, 4096);
		float result;
		if (data.length != 0)
		{
			idata = new int[data.length];
			for (int i = 0; i < data.length; i++)
			{
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
	public float calculateVMax(Channel ch)
	{
		float Vmax = 0;
		float volt2 = 1;
		volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data = new byte[4096];
		int[] idata;
		float result;
		System.arraycopy(ch.getSamples(), 0, data, 0, 4096);
		if (data.length != 0)
		{
			idata = new int[data.length];
			for (int i = 0; i < data.length; i++)
			{
				idata[i] = unsignedToBytes(data[i]) - ch.getVerticalPosition();
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
	public float calculateRms(Channel ch)
	{
		float RMS = 0;
		float volt2 = 0;
		volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data2 = new byte[4096];
		System.arraycopy(ch.getSamples(), 0, data2, 0, 4096);		
		float average2 = 0;
		float buf3 = 25;
		float voltpersample2 = volt2 / buf3;
		float buf4 = 0;
		for (int i = 0; i <= data2.length - 1; i++)
		{
			buf4 = unsignedToBytes(data2[i]);
			average2 += (buf4);
		}
		average2 = (average2 / data2.length);
		for (int i = 0; i <= data2.length - 1; i++)
		{
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
	public float calculateTRms(Channel ch)
	{
		float RMS = 0;
		float volt2 = 0;
		volt2 = scope.getFloatFromVoltageDiv(ch);
		byte[] data2 = new byte[4096];
		System.arraycopy(ch.getSamples(), 0, data2, 0, 4096);
		float buf3 = 25;
		float voltpersample2 = volt2 / buf3;
		for (int i = 0; i <= data2.length - 1; i++)
		{
			RMS += Math.pow((unsignedToBytes(data2[i]) - ch
					.getVerticalPosition()), 2);
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
	private static int unsignedToBytes(byte b)
	{
		int result = b & 0xFF;
		return result;
	}

	public void calculateDV(float y1, float y2)
	{

		float difference;
		float volt2 = (scope.getChannel2().getIsX10() ? 10 : 1) * scope
				.getFloatFromVoltageDiv(scope.getChannel2());
		float volt1 = (scope.getChannel1().getIsX10() ? 10 : 1) * scope
				.getFloatFromVoltageDiv(scope.getChannel1());
		difference = Math.abs(y1 - y2);
		float difference1 = (scope.getFloatFromVoltageDiv(scope.getChannel1())/25.5f) * difference;
		float difference2 = (scope.getFloatFromVoltageDiv(scope.getChannel2())/25.5f) * difference;
		dV1 = difference1;

		dV2 = difference2;

	}

	/**
	 * Calculates the time between the 2 XMarkers and sends it to the activity
	 * 
	 * @param difference
	 *            value between 2 markers
	 */
	public void calculateTime(float x1, float x2)
	{
		float fTimebase = scope.getFloatFromTimeBase();
		float totaltime = fTimebase * scope.totalDivisions;
		float timePerSample = totaltime / scope.totalSamples;
		dt = timePerSample * Math.abs(x2 - x1);
		Freq = 1 / dt;
	}

	public void stopCalculating()
	{
		if (timer != null)
		{

		}
	}

	public void setScope(WFS210 newScope)
	{
		scope = newScope;
	}

	public void clearListners(WFS210MeasurementsListener wml)
	{
		measurementsListeners.remove(wml);
	}

	private Boolean ValidateSignals()
	{
		Boolean status = true;
		for (byte b : scope.getChannel1().getSamples())
		{
			int i = unsignedToBytes(b);
			if (i <= 3)
				status = false;
			if (i >= 252)
				status = false;
		}
		for (byte b : scope.getChannel2().getSamples())
		{
			int i = unsignedToBytes(b);
			if (i <= 3)
				status = false;
			if (i >= 252)
				status = false;
		}

		return status;
	}

	public String getVdc1()
	{
		if (validSignals)
		{
			return fmt(Vdc1) + "V";
		}
		return "???";

	}
	
	public String getVdc2()
	{
		if (validSignals)
		{
			return fmt(Vdc2) + "V";
		}
		return "???";

	}
	
	public String getRMS1()
	{
		if (validSignals)
		{
			return fmt(RMS1) + "V";
		}
		return "???";

	}

	public String getRMS2()
	{
		if (validSignals)
		{
			return fmt(RMS2) + "V";
		}
		return "???";
	}

	public String getTRMS1()
	{
		if (validSignals)
		{
			return fmt(TRMS1) + "V";
		}
		return "???";
	}

	public String getTRMS2()
	{
		if (validSignals)
		{
			return fmt(TRMS2) + "V";
		}
		return "???";
	}

	public String getVmax1()
	{
		if (validSignals)
		{
			return fmt(Vmax1) + "V";
		}
		return "???";
	}

	public String getVmax2()
	{
		if (validSignals)
		{
			return fmt(Vmax2) + "V";
		}
		return "???";
	}

	public String getVmin1()
	{
		if (validSignals)
		{
			return fmt(Vmin1) + "V";
		}
		return "???";
	}

	public String getVmin2()
	{
		if (validSignals)
		{
			return fmt(Vmin2) + "V";
		}
		return "???";
	}

	public String getVpkpk1()
	{
		if (validSignals)
		{
			return fmt(Vpkpk1) + "V";
		}
		return "???";
	}

	public String getVpkpk2()
	{
		if (validSignals)
		{
			return fmt(Vpkpk2)+"V";
		}
		return "???";
	}

	public String getDbM1()
	{
		if (validSignals)
		{
			return fmt(dbM1) + "dB";
		}
		return "???";
	}

	public String getDbM2()
	{
		if (validSignals)
		{
			return fmt(dbM2) + "dB";
		}
		return "???";
	}

	public String getDbGain()
	{
		if (validSignals)
		{
			return fmt(dbGain) + "dB";
		}
		return "???";
	}

	public String getW1rms2()
	{
		if (validSignals)
		{
			return fmt(W1rms2) + "V";
		}
		return "???";
	}

	public String getW1rms4()
	{
		if (validSignals)
		{
			return fmt(W1rms4) + "V";
		}
		return "???";
	}

	public String getW1rms8()
	{
		if (validSignals)
		{
			return fmt(W1rms8) + "V";
		}
		return "???";
	}

	public String getW1rms16()
	{
		if (validSignals)
		{
			return fmt(W2rms16) + "V";
		}
		return "???";
	}

	public String getW1rms32()
	{
		if (validSignals)
		{
			return fmt(W1rms32) + "V";
		}
		return "???";
	}

	public String getW2rms2()
	{
		if (validSignals)
		{
			return fmt(W2rms2) + "V";
		}
		return "???";
	}

	public String getW2rms4()
	{
		if (validSignals)
		{
			return fmt(W2rms4) + "V";
		}
		return "???";
	}

	public String getW2rms8()
	{
		if (validSignals)
		{
			return fmt(W2rms8) + "V";
		} 
		return "???";
	}

	public String getW2rms16()
	{
		if (validSignals)
		{
			return fmt(W2rms16) + "V";
		}
		return "???";
	}

	public String getW2rms32()
	{
		if (validSignals)
		{
			return fmt(W2rms32) + "V";
		}
		return "???";
	}

	public String getdV1()
	{
		if(dV1 >= 1)
		{
			String format = String.format("%.2f",dV1);
			return " "+format+"V";
		}
		else
		{
			return fmt(dV1*1000)+"mV";
		}
		

	}

	public String getdV2()
	{
		if(dV2 >= 1)
		{
			String format = String.format("%.2f",dV2);
			return " "+format+"V";
		}
		else
		{
			return fmt(dV2*1000)+"mV";
		}

	}

	public String getdt()
	{
		if (dt >= 1)
		{
			return String.format("%.2f", dt) + " s";
		} else
		{
			if (dt > 0.001 && dt <= 0.99)
			{
				return String.format("%.2f", dt * 1000) + " ms";
			} else
			{
				if (dt > 0.000001 && dt <= 0.000999)
				{
					return String.format("%.2f", dt * 1000000) + " μs";
				}
			}
		}
		return "???";

	}

	public String getFreq()
	{

		float freq = 1 / dt;
		if (freq <= 1000000)
		{
			if (freq <= 1000)
				return String.format("%.1f", freq) + " Hz";
			else
				return String.format("%.1f", freq / 1000) + " KHz";
		} else
			return String.format("%.1f", freq / 1000000) + " MHz";

	}

}

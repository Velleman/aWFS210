package be.velleman.wfs210;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import be.velleman.wfs210.Channel.InputCoupling;
import be.velleman.wfs210.Trigger.ManualTriggering;
import be.velleman.wfs210.Trigger.RestartTriggering;
import be.velleman.wfs210.Trigger.TriggerChannel;
import be.velleman.wfs210.Trigger.TriggerMode;
import be.velleman.wfs210.Trigger.TriggerSlope;

public class FakeWFS210 extends WFS210
{

	public FakeWFS210(Connector fakeConnector)
	{
		isFakeData = true;
		connector = fakeConnector;
	}

	@Override
	Boolean sendSettings()
	{
		// TODO Auto-generated method stub
		updateSettings();
		return true;
	}

	@Override
	Boolean requestSettings()
	{
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Generates fake signals and stores it in the channels buffer This will run
	 * until there is a connection
	 */
	@Override
	public void generateFakeSignals()
	{

		Thread fakesignal = new Thread("Fake Signals")
		{
			public void run()
			{
				Random rand = new Random(1337);
				while (true)
				{
					
					if (!triggerSettings.getRun_Hold())
					{
						byte[] fakeSquare = new byte[(int) 4096];
						float frequency = calculateFrequency(TimeBase.HDIV_1mS);
						float amplitude1 = calculateAmplitude(channel1, VoltageDiv.VDIV_200mV);
						float amplitude2 = calculateAmplitude(channel2, VoltageDiv.VDIV_200mV);
						float max, min;
						max = 0;
						min = 0;
						byte[] fakeSinus = new byte[4096];
						for (int i = 0; i < 4096 - 1; i++)
						{
							float data = (float) (amplitude2 * (Math
									.sin((double) (i * ((2 * Math.PI * frequency) / (totalSamples))))));

							data = (data * 128) + channel2
									.getVerticalPosition();

							//i = i + rand.nextInt(4);
							//Log.i("Fake",Float.toString(data));

							data += rand.nextInt(2);
							if (data > 256)
							{
								data = 255;
							}
							if (data < 0)
								data = 0;
							if (data > max)
							{
								max = data;
							}
							if (data < min)
								min = data;
							fakeSinus[i] = (byte) data;
						}
						channel2.setSampleData(0, fakeSinus);
						max = 0;
						min = 0;
						for (int i = 0; i < totalSamples - 1; i++)
						{
							float data = (float) (amplitude1 * (Math
									.sin((double) (i * ((2 * Math.PI * frequency) / (totalSamples))))));
							if (data > max)
								max = data;
							if (data < min)
								min = data;
						}
						for (int i = 0; i < 4096 - 1; i++)
						{
							float data = (float) (amplitude1 * (Math
									.sin((double) (2 * i * ((2 * Math.PI * frequency) / (totalSamples))))));

							data = Math.signum(data);
							if (data == 1.0f)
							{
								data = (max * 100) + channel1
										.getVerticalPosition();
								data += rand.nextInt(2);
								if (data > 256)
								{
									data = 255;
								}
							} else
							{
								data = (min * 100) + channel1
										.getVerticalPosition();
								data += rand.nextInt(2);
								if (data < 0)
									data = 0;
							}
							fakeSquare[i] = (byte) data;
						}
						channel1.setSampleData(0, fakeSquare);
						isNewData = true;
						newDataFrame();
					}
					try {
						synchronized(this)
						{
							this.wait(200);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		};
		fakesignal.start();
	}

	private float calculateAmplitude(Channel ch, VoltageDiv vdivref)
	{
		int diff = ch.getVerticalDiv().ordinal() - vdivref.ordinal();
		float factor = 0;
		Boolean isChanged = false;
		if (diff > 0)//positive number
		{

			for (int i = vdivref.ordinal(); i < ch.getVerticalDiv().ordinal(); i++)
			{
				factor += vdivArray[i - 1];
				isChanged = true;
			}
		} else
		//Negative number
		{
			diff = Math.abs(diff);
			float factor2 = 1;
			for (int i = vdivref.ordinal(); i > ch.getVerticalDiv().ordinal(); i--)
			{
				factor2 /= (float) vdivArray[i];
				isChanged = true;
			}
			factor = factor2;
		}
		if (isChanged)
			return factor;
		else
			return 1;
	}

	private float calculateFrequency(TimeBase timeRef)
	{
		int diff = timeBase.ordinal() - timeRef.ordinal();
		float factor = 0;
		Boolean isChanged = false;
		if (diff > 0)//positive number
		{

			for (int i = timeRef.ordinal(); i < timeBase.ordinal(); i++)
			{
				factor += timeArray[i - 1];
				isChanged = true;
			}
		} else
		//Negative number
		{
			diff = Math.abs(diff);
			float factor2 = 1;
			for (int i = timeRef.ordinal(); i > timeBase.ordinal(); i--)
			{
				factor2 /= (float) timeArray[i];
				isChanged = true;
			}
			factor = factor2;
		}
		if (isChanged)
			return factor;
		else
			return 1;
	}

	@Override
	Boolean requestCalibrate()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	Boolean sendWifiSettings(String currentWifiName, String currentWifiChannel)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	Trigger getTriggerSettings()
	{
		// TODO Auto-generated method stub
		return triggerSettings;
	}

	@Override
	void setTriggerSettings(Trigger t)
	{
		// TODO Auto-generated method stub

	}

	@Override
	Boolean requestWifiSettings()
	{
		// TODO Auto-generated method stub
		Map<String,String> wifiSettings = new HashMap<String,String>();
		wifiSettings.put("WIFICHANNEL", "DEMO");
		wifiSettings.put("WIFINAME", "DEMO");
		wifiSettings.put("WIFIPASSWORD", "DEMO");
		wifiSettings.put("WIFIVERSION", "DEMO");
		wifiSettings.put("SCOPEVERSION", "DEMO");
		notifyUpdatedWifiSettings(wifiSettings);
		return true;
	}
}

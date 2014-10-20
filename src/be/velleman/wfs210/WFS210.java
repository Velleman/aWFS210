package be.velleman.wfs210;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.velleman.wfs210.Channel.InputCoupling;
import be.velleman.wfs210.Trigger.ManualTriggering;
import be.velleman.wfs210.Trigger.RestartTriggering;
import be.velleman.wfs210.Trigger.TriggerChannel;
import be.velleman.wfs210.Trigger.TriggerMode;
import be.velleman.wfs210.Trigger.TriggerSlope;

public abstract class WFS210
{
	protected Channel channel1;
	protected Channel channel2;
	public boolean isNewData;
	protected TimeBase timeBase;
	protected Trigger triggerSettings;
	protected Connector connector;
	public Channel selectedChannel;
	public int totalSamples, totalDivisions;
	protected List<ScopeDataChangedListener> scopeDataChangedListeners = new ArrayList<ScopeDataChangedListener>();
	protected Map<String, String> settings = new HashMap<String, String>();
	protected Boolean isFakeData = true;
	float[] vdivArray = new float[]
	{ 0, 2, 2.5f, 2, 2, 2, 2.5f, 2, 2, 2, 2.5f, 2 };
	float[] timeArray = new float[]
	{ 2, 2.5f, 2, 2, 2.5f, 2, 2, 2.5f, 2, 2, 2.5f, 2, 2, 2.5f, 2, 2, 2.5f, 2 };
	List<NewDataFrameListener> newDataFrameListeners = new ArrayList<NewDataFrameListener>();
	Boolean hasSettings = false;

	public Boolean getHasSettings()
	{
		return hasSettings;
	}

	public void setHasSettings(Boolean hasSettings)
	{
		this.hasSettings = hasSettings;
	}

	public void addNewDataFrameListener(NewDataFrameListener nfl)
	{
		newDataFrameListeners.add(nfl);
	}

	public void removeNewDataFrameListener(NewDataFrameListener nfl)
	{
		newDataFrameListeners.remove(nfl);
	}

	public void newDataFrame()
	{
		for (NewDataFrameListener ndfl : newDataFrameListeners)
		{
			ndfl.newDataFrame();
		}
	}

	public Channel getChannel1()
	{
		return this.channel1;
	}

	public Channel getChannel2()
	{
		return this.channel2;
	}

	public TimeBase getTimeBase()
	{
		return timeBase;
	}

	public void setTimeBase(TimeBase timeBase)
	{
		this.timeBase = timeBase;
	}

	public void addScopeDataChangedListener(ScopeDataChangedListener sdcl)
	{
		scopeDataChangedListeners.add(sdcl);
	}

	public void notifyUpdatedSettings(Map<String, String> settings)
	{
		for(int i = 0;i < scopeDataChangedListeners.size();i++)
		{
			scopeDataChangedListeners.get(i).updatedSettings(settings);
		}
		
	}

	public void notifyUpdatedWifiSettings(Map<String, String> settings)
	{
		for(int i = 0;i < scopeDataChangedListeners.size();i++)
		{
			scopeDataChangedListeners.get(i).updatedWifiSettings(settings);
		}
	}

	abstract Boolean sendSettings();

	abstract Boolean requestSettings();

	abstract Boolean requestCalibrate();

	abstract Boolean sendWifiSettings(String currentWifiName,
			String currentWifiChannel);

	abstract Trigger getTriggerSettings();

	abstract void setTriggerSettings(Trigger t);

	abstract void generateFakeSignals();

	abstract Boolean requestWifiSettings();

	/**
	 * This will convert the timebase setting from enum to a Float value
	 * 
	 * @return Returns the float value from the current timebase setting
	 */
	public float getFloatFromTimeBase()
	{
		float fTimebase = 0f;
		if (timeBase != null)
		{
			switch (timeBase.ordinal())
			{
			case 0:
				fTimebase = 0.000001f;
				break;
			case 1:
				fTimebase = 0.000002f;
				break;
			case 2:
				fTimebase = 0.000005f;
				break;
			case 3:
				fTimebase = 0.00001f;
				break;
			case 4:
				fTimebase = 0.00002f;
				break;
			case 5:
				fTimebase = 0.00005f;
				break;
			case 6:
				fTimebase = 0.0001f;
				break;
			case 7:
				fTimebase = 0.0002f;
				break;
			case 8:
				fTimebase = 0.0005f;
				break;
			case 9:
				fTimebase = 0.001f;
				break;
			case 10:
				fTimebase = 0.002f;
				break;
			case 11:
				fTimebase = 0.005f;
				break;
			case 12:
				fTimebase = 0.01f;
				break;
			case 13:
				fTimebase = 0.02f;
				break;
			case 14:
				fTimebase = 0.05f;
				break;
			case 15:
				fTimebase = 0.1f;
				break;
			case 16:
				fTimebase = 0.2f;
				break;
			case 17:
				fTimebase = 0.5f;
				break;
			case 18:
				fTimebase = 1f;
				break;
			default:
				break;

			}
		}
		return fTimebase;
	}

	public float getFloatFromVoltageDiv(Channel ch)
	{
		float volt1 = 0;
		if (ch.getVerticalDiv() != null)
		{
			switch (ch.getVerticalDiv().ordinal())
			{
			case 1:
				volt1 = 20;
				break;
			case 2:
				volt1 = 10;
				break;
			case 3:
				volt1 = 5;
				break;
			case 4:
				volt1 = 2;
				break;
			case 5:
				volt1 = 1;
				break;
			case 6:
				volt1 = 0.5f;
				break;
			case 7:
				volt1 = 0.2f;
				break;
			case 8:
				volt1 = 0.1f;
				break;
			case 9:
				volt1 = 0.05f;
				break;
			case 10:
				volt1 = 0.025f;
				break;
			case 11:
				volt1 = 0.01f;
				break;
			case 12:
				volt1 = 0.005f;
				break;
			default:
				break;
			}
		}
		return volt1;
	}

	public void updateScaleData(Boolean isScaling, Boolean isTime,
			Boolean isLeft)
	{
		if (isScaling)
		{

			settings.put("VDIV1", getStringFromVDiv(channel1.getVerticalDiv(), channel1));
			settings.put("VDIV2", getStringFromVDiv(channel2.getVerticalDiv(), channel2));
			settings.put("TIMEBASE", getStringFromTimebase(timeBase));
			settings.put("ENDSCALE", "FALSE");
			settings.put("XSCALE", isTime ? "TRUE" : "FALSE");
			settings.put("LEFT", isLeft ? "TRUE" : "FALSE");
			notifyUpdatedSettings(settings);

		} else
		{

			settings.put("ENDSCALE", "TRUE");
			notifyUpdatedSettings(settings);
		}
	}

	protected String getStringFromTimebase(TimeBase tb)
	{
		String timebasereturn = "";
		if (tb != null)
		{
			switch (tb.ordinal())
			{
			case 0:
				timebasereturn = "1us";
				break;
			case 1:
				timebasereturn = "2us";
				break;
			case 2:
				timebasereturn = "5us";
				break;
			case 3:
				timebasereturn = "10us";
				break;
			case 4:
				timebasereturn = "20us";
				break;
			case 5:
				timebasereturn = "50us";
				break;
			case 6:
				timebasereturn = "100us";
				break;
			case 7:
				timebasereturn = "200us";
				break;
			case 8:
				timebasereturn = "500us";
				break;
			case 9:
				timebasereturn = "1ms";
				break;
			case 10:
				timebasereturn = "2ms";
				break;
			case 11:
				timebasereturn = "5ms";
				break;
			case 12:
				timebasereturn = "10ms";
				break;
			case 13:
				timebasereturn = "20ms";
				break;
			case 14:
				timebasereturn = "50ms";
				break;
			case 15:
				timebasereturn = "100ms";
				break;
			case 16:
				timebasereturn = "200ms";
				break;
			case 17:
				timebasereturn = "500ms";
				break;
			case 18:
				timebasereturn = "1s";
				break;
			default:
				break;
			}
		}
		return timebasereturn;
	}

	/**
	 * Converts the VDIV settings from the specified channel to a String
	 * 
	 * @param ch
	 *            a VDIV enum
	 * @return the VDIV in string format
	 */
	protected String getStringFromVDiv(VoltageDiv vdiv, Channel ch)
	{
		String value = "";
		if (vdiv != null && ch != null)
		{
			switch (vdiv.ordinal())
			{
			case 0:
				value = "off";
				break;
			case 1:
				value = ch.getIsX10() ? "200V" : "20V";
				break;
			case 2:
				value = ch.getIsX10() ? "100V" : "10V";
				break;
			case 3:
				value = ch.getIsX10() ? "40V" : "4V";
				break;
			case 4:
				value = ch.getIsX10() ? "20V" : "2V";
				break;
			case 5:
				value = ch.getIsX10() ? "10V" : "1V";
				break;
			case 6:
				value = ch.getIsX10() ? "5V" : "500mV";
				break;
			case 7:
				value = ch.getIsX10() ? "2V" : "200mV";
				break;
			case 8:
				value = ch.getIsX10() ? "1V" : "100mV";
				break;
			case 9:
				value = ch.getIsX10() ? "500mV" : "50mV";
				break;
			case 10:
				value = ch.getIsX10() ? "250mV" : "25mV";
				break;
			case 11:
				value = ch.getIsX10() ? "100mV" : "10mV";
				break;
			case 12:
				value = ch.getIsX10() ? "50mV" : "5mV";
				break;
			default:
				break;
			}
		}
		return value;
	}

	protected byte generateTriggerSettings(Trigger triggerData)
	{
		byte result = 0x00;
		switch (triggerData.getTrigger_Mode())
		{
		case NORMAL:
			break;
		case AUTO:
			result = (byte) (result | 0x01);
			break;
		case ONCE:
			result = (byte) (result | 0x02);
			break;

		default:
			break;
		}
		if (triggerData.getTrigger_Slope().ordinal() == 1)
		{
			result = (byte) (result | 4);
		}
		if (triggerData.getTrigger_Channel().ordinal() == 1)
		{
			result = (byte) (result | 8);
		}
		if (triggerData.getRun_Hold())
		{
			result = (byte) (result | 16);
		}
		if (triggerData.getRestart_Triggering().ordinal() == 1)
		{
			result = (byte) (result | 32);
		}
		if (triggerData.getManual_Triggering().ordinal() == 1)
		{
			result = (byte) (result | 64);
		}
		if (triggerData.getAutorange())
		{
			result = (byte) (result | 128);
		}
		return result;
	}

	public void updateSettings()
	{

		if (channel1.getInputCoupling() == InputCoupling.AC)
		{
			settings.put("IC1", "AC");
		}
		if (channel1.getInputCoupling() == InputCoupling.DC)
		{
			settings.put("IC1", "DC");
		}
		if (channel1.getInputCoupling() == InputCoupling.GND)
		{
			settings.put("IC1", "GND");
		}

		settings.put("VDIV1", getStringFromVDiv(channel1.getVerticalDiv(), channel1));

		settings.put("VPOS1", Integer.toString(channel1.getVerticalPosition()));

		if (channel2.getInputCoupling() == InputCoupling.AC)
		{
			settings.put("IC2", "AC");
		}
		if (channel2.getInputCoupling() == InputCoupling.DC)
		{
			settings.put("IC2", "DC");
		}
		if (channel2.getInputCoupling() == InputCoupling.GND)
		{
			settings.put("IC2", "GND");
		}

		settings.put("VDIV2", getStringFromVDiv(channel2.getVerticalDiv(), channel2));

		settings.put("VPOS2", Integer.toString(channel2.getVerticalPosition()));

		settings.put("TIMEBASE", getStringFromTimebase(timeBase));

		settings.put("TRIGGERLEVEL", Integer.toString(triggerSettings
				.getTriggerLevel()));

		if (triggerSettings.getTrigger_Channel() == TriggerChannel.CH1)
		{
			settings.put("TRIGGERCHANNEL", "CH1");
		} else
		{
			settings.put("TRIGGERCHANNEL", "CH2");
		}
		switch (triggerSettings.getTrigger_Mode().ordinal())
		{
		case 0:
			settings.put("TRIGGERMODE", "Normal");
			break;
		case 1:
			settings.put("TRIGGERMODE", "Auto");
			break;
		case 2:
			settings.put("TRIGGERMODE", "Once");
			break;
		case 3:
			settings.put("TRIGGERMODE", "Roll");
			break;
		}
		if (triggerSettings.getTrigger_Slope().ordinal() == TriggerSlope.FALLING_EDGE
				.ordinal())
		{
			settings.put("TRIGGERSLOPE", "FALLING");
		} else
		{
			settings.put("TRIGGERSLOPE", "RISING");
		}
		if (triggerSettings.getRun_Hold())
		{
			settings.put("HOLD", "TRUE");
		} else
		{
			settings.put("HOLD", "FALSE");
		}
		notifyUpdatedSettings(settings);
	}

	public void setSettingByMap(Map<String, String> map)
	{

		if (map.get("TIMEBASE").equals("1us"))
			timeBase = TimeBase.HDIV_1uS;
		if (map.get("TIMEBASE").equals("2us"))
			timeBase = TimeBase.HDIV_2uS;
		if (map.get("TIMEBASE").equals("5us"))
			timeBase = TimeBase.HDIV_5uS;
		if (map.get("TIMEBASE").equals("10us"))
			timeBase = TimeBase.HDIV_10uS;
		if (map.get("TIMEBASE").equals("20us"))
			timeBase = TimeBase.HDIV_20uS;
		if (map.get("TIMEBASE").equals("50us"))
			timeBase = TimeBase.HDIV_50uS;
		if (map.get("TIMEBASE").equals("100us"))
			timeBase = TimeBase.HDIV_100uS;
		if (map.get("TIMEBASE").equals("200us"))
			timeBase = TimeBase.HDIV_200uS;
		if (map.get("TIMEBASE").equals("500us"))
			timeBase = TimeBase.HDIV_500uS;
		if (map.get("TIMEBASE").equals("1ms"))
			timeBase = TimeBase.HDIV_1mS;
		if (map.get("TIMEBASE").equals("2ms"))
			timeBase = TimeBase.HDIV_2mS;
		if (map.get("TIMEBASE").equals("5ms"))
			timeBase = TimeBase.HDIV_5mS;
		if (map.get("TIMEBASE").equals("10ms"))
			timeBase = TimeBase.HDIV_10mS;
		if (map.get("TIMEBASE").equals("20ms"))
			timeBase = TimeBase.HDIV_20mS;
		if (map.get("TIMEBASE").equals("50ms"))
			timeBase = TimeBase.HDIV_50mS;
		if (map.get("TIMEBASE").equals("100ms"))
			timeBase = TimeBase.HDIV_100mS;
		if (map.get("TIMEBASE").equals("200ms"))
			timeBase = TimeBase.HDIV_200mS;
		if (map.get("TIMEBASE").equals("500ms"))
			timeBase = TimeBase.HDIV_500mS;
		if (map.get("TIMEBASE").equals("1s"))
			timeBase = TimeBase.HDIV_1S;

		setVoltageDivByString(map.get("VDIV1"), channel1);
		setVoltageDivByString(map.get("VDIV2"), channel2);

		if (map.get("HOLD") == "TRUE")
		{
			triggerSettings.setRun_Hold(true);
		} else
		{
			triggerSettings.setRun_Hold(false);
		}
		if (map.get("IC1").equals("AC"))
		{
			channel1.setInputCoupling(InputCoupling.AC);
		}
		if (map.get("IC1").equals("DC"))
		{
			channel1.setInputCoupling(InputCoupling.DC);
		}
		if (map.get("IC1").equals("GND"))
		{
			channel1.setInputCoupling(InputCoupling.GND);
		}
		if (map.get("IC2").equals("AC"))
		{
			channel2.setInputCoupling(InputCoupling.AC);
		}
		if (map.get("IC2").equals("DC"))
		{
			channel2.setInputCoupling(InputCoupling.DC);
		}
		if (map.get("IC2").equals("GND"))
		{
			channel2.setInputCoupling(InputCoupling.GND);
		}
		if (map.get("TRIGGERMODE").equals("Auto"))
		{
			triggerSettings.setTrigger_Mode(TriggerMode.AUTO);
		}
		if (map.get("TRIGGERMODE").equals("Normal"))
		{
			triggerSettings.setTrigger_Mode(TriggerMode.NORMAL);
		}
		if (map.get("TRIGGERMODE").equals("Once"))
		{
			triggerSettings.setTrigger_Mode(TriggerMode.ONCE);
		}
		if (map.get("TRIGGERCHANNEL").equals("CH1"))
		{
			triggerSettings.setTrigger_Channel(TriggerChannel.CH1);
		} else
		{
			triggerSettings.setTrigger_Channel(TriggerChannel.CH2);
		}
		if (map.get("TRIGGERSLOPE").equals("RISING"))
		{
			triggerSettings.setTrigger_Slope(TriggerSlope.RISING_EDGE);
		} else
		{
			triggerSettings.setTrigger_Slope(TriggerSlope.FALLING_EDGE);
		}
		if (map.get("AUTORANGE").equals("TRUE"))
		{
			triggerSettings.setAutorange(true);
		} else
		{
			triggerSettings.setAutorange(false);
		}
		String vpos1 = map.get("VPOS1");
		int ivpos1 = Integer.parseInt(vpos1);
		channel1.setVerticalPosition(ivpos1);
		channel2.setVerticalPosition(Integer.parseInt(map.get("VPOS2")));
		triggerSettings.setTriggerLevel(Integer.parseInt(map
				.get("TRIGGERLEVEL")));
		hasSettings = true;
	}

	private void setVoltageDivByString(String data, Channel ch)
	{
		if (data.equals("off"))
			ch.setVerticalDiv(VoltageDiv.off);
		if (data.equals("20V"))
			ch.setVerticalDiv(VoltageDiv.VDIV_20V);
		if (data.equals("10V"))
			ch.setVerticalDiv(VoltageDiv.VDIV_10V);
		if (data.equals("4V"))
			ch.setVerticalDiv(VoltageDiv.VDIV_4V);
		if (data.equals("2V"))
			ch.setVerticalDiv(VoltageDiv.VDIV_2V);
		if (data.equals("1V"))
			ch.setVerticalDiv(VoltageDiv.VDIV_1);
		if (data.equals("500mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_500mV);
		if (data.equals("200mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_200mV);
		if (data.equals("100mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_100mV);
		if (data.equals("50mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_50mV);
		if (data.equals("25mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_25mV);
		if (data.equals("10mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_10mV);
		if (data.equals("5mV"))
			ch.setVerticalDiv(VoltageDiv.VDIV_5mV);

	}
	
	public void LoadDefaultSettings()
	{
		channel1 = new Channel("CH1");
		channel2 = new Channel("CH2");
		triggerSettings = new Trigger();
		triggerSettings.setAutorange(false);
		triggerSettings
				.setManual_Triggering(ManualTriggering.NO_MANUAL_TRIGGERING);
		triggerSettings
				.setRestart_Triggering(RestartTriggering.NO_RESTART);
		triggerSettings.setRun_Hold(false);
		triggerSettings.setTrigger_Channel(TriggerChannel.CH1);
		triggerSettings.setTrigger_Mode(TriggerMode.AUTO);
		triggerSettings.setTrigger_Slope(TriggerSlope.FALLING_EDGE);
		triggerSettings.setTriggerLevel(64);
		channel1.setInputCoupling(InputCoupling.AC);
		channel1.setIsX10(false);
		channel1.setVerticalDiv(VoltageDiv.VDIV_200mV);
		channel1.setVerticalPosition(128);
		channel2.setInputCoupling(InputCoupling.AC);
		channel2.setIsX10(false);
		channel2.setVerticalDiv(VoltageDiv.VDIV_200mV);
		channel2.setVerticalPosition(128);
		timeBase = TimeBase.HDIV_1mS;
		selectedChannel = channel1;
		hasSettings = true;
	}

}

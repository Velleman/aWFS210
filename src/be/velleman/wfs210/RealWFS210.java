package be.velleman.wfs210;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import be.velleman.wfs210.Channel.InputCoupling;
import be.velleman.wfs210.Trigger.ManualTriggering;
import be.velleman.wfs210.Trigger.RestartTriggering;
import be.velleman.wfs210.Trigger.TriggerChannel;
import be.velleman.wfs210.Trigger.TriggerMode;
import be.velleman.wfs210.Trigger.TriggerSlope;

/**
 * @author bn
 * 
 */

public class RealWFS210 extends WFS210 implements ConnectionListener
{
	private Map<String, String> wifiSettings = new HashMap<String, String>();
	
	@Override
	public Trigger getTriggerSettings()
	{
		return triggerSettings;
	}

	@Override
	public void setTriggerSettings(Trigger triggerSettings)
	{
		this.triggerSettings = triggerSettings;
	}

	/**
	 * @param con
	 *            The context of this application
	 * @param connector
	 *            The connector object that the wfs210 needs to use
	 * @param mHandler
	 *            The handler for sending message to the activity
	 */
	public RealWFS210(Connector connector)
	{

		channel1 = new Channel("CH1");
		channel2 = new Channel("CH2");
		triggerSettings = new Trigger();
		this.connector = connector;
		selectedChannel = channel2;

		connector.addConnectionListener(this);
	}

	/**
	 * This will send the current settings to the scope If settings are not
	 * filled it will try requesting it
	 */
	@Override
	public Boolean sendSettings()
	{
		try
		{
			Packet SettingsPacket = new Packet(18);
			SettingsPacket.setCommand(Commands.SEND_SCOPE_SETTINGS);
			SettingsPacket.setData(2, channel1.getInputCoupling().ordinal());
			SettingsPacket.setData(3, channel1.getVerticalDiv().ordinal());
			SettingsPacket.setData(4, channel1.getVerticalPosition());
			SettingsPacket.setData(5, channel2.getInputCoupling().ordinal());
			SettingsPacket.setData(6, channel2.getVerticalDiv().ordinal());
			SettingsPacket.setData(7, channel2.getVerticalPosition());
			SettingsPacket.setData(8, timeBase.ordinal());
			SettingsPacket.setData(9, (byte) triggerSettings.getTriggerLevel());
			SettingsPacket.setData(10, generateTriggerSettings(triggerSettings));
			SettingsPacket.finalize();
			connector.send(SettingsPacket);
			return true;
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * This function will request the settings from the scope
	 * 
	 * @return Returns true if successful else false;
	 */
	@Override
	public Boolean requestSettings()
	{
		Packet requestSettings = new Packet(8);
		requestSettings.setCommand(Commands.REQUEST_STATUS);
		requestSettings.finalize();

		try
		{
			connector.send(requestSettings);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	/**
	 * This will request the wifi settings from the scope
	 * 
	 * @return Returns true if successful else false;
	 */
	public Boolean requestWifiSettings()
	{
		Packet requestSettings = new Packet(8);
		requestSettings.setCommand(Commands.REQUEST_WIFI_SETTINGS);
		requestSettings.finalize();

		try
		{
			connector.send(requestSettings);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}

	private Trigger AnalyzeTriggerData(byte data)
	{
		Trigger newTriggerSettings = new Trigger();

		byte triggerMode = 0;
		byte triggerSlope = 0;
		byte triggerChannel = 0;
		byte runHold = 0;
		byte restart = 0;
		byte manual = 0;
		byte autoRange = 0;

		triggerMode = (byte) (data & 3);
		switch (triggerMode)
		{
		case 0:
			newTriggerSettings.setTrigger_Mode(TriggerMode.NORMAL);
			break;
		case 1:
			newTriggerSettings.setTrigger_Mode(TriggerMode.AUTO);
			break;
		case 2:
			newTriggerSettings.setTrigger_Mode(TriggerMode.ONCE);
			break;
		default:
			break;
		}
		data = (byte) (data >> 2);
		triggerSlope = (byte) (data & 1);
		if (triggerSlope == 1)
			newTriggerSettings.setTrigger_Slope(TriggerSlope.FALLING_EDGE);
		else
			newTriggerSettings.setTrigger_Slope(TriggerSlope.RISING_EDGE);

		data = (byte) (data >> 1);
		triggerChannel = (byte) (data & 1);
		if (triggerChannel == 1)
			newTriggerSettings.setTrigger_Channel(TriggerChannel.CH2);
		else
			newTriggerSettings.setTrigger_Channel(TriggerChannel.CH1);

		data = (byte) (data >> 1);
		runHold = (byte) (data & 1);
		if (runHold == 1)
			newTriggerSettings.setRun_Hold(true);
		else
			newTriggerSettings.setRun_Hold(false);

		data = (byte) (data >> 1);
		restart = (byte) (data & 1);
		if (restart == 1)
			newTriggerSettings
					.setRestart_Triggering(RestartTriggering.RESTART_TRIGGERING);
		else
			newTriggerSettings
					.setRestart_Triggering(RestartTriggering.NO_RESTART);

		data = (byte) (data >> 1);
		manual = (byte) (data & 1);
		if (manual == 1)
			newTriggerSettings
					.setManual_Triggering(ManualTriggering.FORCE_MANUAL_TRIGGERING);
		else
			newTriggerSettings
					.setManual_Triggering(ManualTriggering.NO_MANUAL_TRIGGERING);

		data = (byte) (data >> 1);
		autoRange = (byte) (data & 1);
		if (autoRange == 1)
			newTriggerSettings.setAutorange(true);
		else
			newTriggerSettings.setAutorange(false);

		return newTriggerSettings;

	}

	static int i = 0;
	private Boolean isCalibrating = false;
	private byte previousstatus = 10;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.velleman.wfs210.packetListener#newPacket(be.velleman.wfs210.Packet)
	 */
	@Override
	public void newPacketFound(Packet p)
	{
		// TODO Auto-generated method stub
		//Log.i(TAG,Integer.toString(i));
		//i++;
		Packet currentPacket = p;

		if (currentPacket.getCommand() == Commands.RECEIVE_SCOPE_SETTINGS
				.getValue())
		{

			channel1.setInputCoupling(InputCoupling.values()[currentPacket
					.getData(2)]);
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
			channel1.setVerticalDiv(VoltageDiv.values()[currentPacket
					.getData(3)]);
			settings.put("VDIV1", getStringFromVDiv(channel1.getVerticalDiv(), channel1));
			channel1.setVerticalPosition(unsignedToBytes(currentPacket
					.getData(4)));
			settings.put("VPOS1", Integer.toString(channel1
					.getVerticalPosition()));
			channel2.setInputCoupling(InputCoupling.values()[currentPacket
					.getData(5)]);
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
			channel2.setVerticalDiv(VoltageDiv.values()[currentPacket
					.getData(6)]);
			settings.put("VDIV2", getStringFromVDiv(channel2.getVerticalDiv(), channel2));
			channel2.setVerticalPosition(unsignedToBytes(currentPacket
					.getData(7)));

			settings.put("VPOS2", Integer.toString(channel2
					.getVerticalPosition()));
			timeBase = TimeBase.fromOrdinal(currentPacket.getData(8));
			switch (timeBase.ordinal())
			{
			case 0:
				settings.put("TIMEBASE", "1us");
				break;
			case 1:
				settings.put("TIMEBASE", "2us");
				break;
			case 2:
				settings.put("TIMEBASE", "5us");
				break;
			case 3:
				settings.put("TIMEBASE", "10us");
				break;
			case 4:
				settings.put("TIMEBASE", "20us");
				break;
			case 5:
				settings.put("TIMEBASE", "50us");
				break;
			case 6:
				settings.put("TIMEBASE", "100us");
				break;
			case 7:
				settings.put("TIMEBASE", "200us");
				break;
			case 8:
				settings.put("TIMEBASE", "500us");
				break;
			case 9:
				settings.put("TIMEBASE", "1ms");
				break;
			case 10:
				settings.put("TIMEBASE", "2ms");
				break;
			case 11:
				settings.put("TIMEBASE", "5ms");
				break;
			case 12:
				settings.put("TIMEBASE", "10ms");
				break;
			case 13:
				settings.put("TIMEBASE", "20ms");
				break;
			case 14:
				settings.put("TIMEBASE", "50ms");
				break;
			case 15:
				settings.put("TIMEBASE", "100ms");
				break;
			case 16:
				settings.put("TIMEBASE", "200ms");
				break;
			case 17:
				settings.put("TIMEBASE", "500ms");
				break;
			case 18:
				settings.put("TIMEBASE", "1s");
				break;
			default:
				break;

			}

			triggerSettings = AnalyzeTriggerData(currentPacket.getData(10));
			int triggerLevel = (unsignedToBytes(currentPacket.getData(9)));
			triggerSettings.setTriggerLevel(triggerLevel);
			settings.put("TRIGGERLEVEL", Integer
					.toString(unsignedToBytes(currentPacket.getData(9))));
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
			}
			if (triggerSettings.getTrigger_Slope().ordinal() == TriggerSlope.FALLING_EDGE
					.ordinal())
			{
				settings.put("TRIGGERSLOPE", "FALLING");
			} else
			{
				settings.put("TRIGGERSLOPE", "RISING");
			}
			byte modulestatus = p.getData(11);
			byte calibrating = (byte) (modulestatus >> 4);
			calibrating = (byte) (calibrating & 0x01);
			if (calibrating == 1)
			{
				if (!isCalibrating)
				{
					isCalibrating = true;

					settings.put("CALIBRATING", isCalibrating ? "TRUE" : "FALSE");
					notifyUpdatedSettings(settings);
				}

			} else
			{
				if (isCalibrating)
				{
					isCalibrating = false;
					settings.put("CALIBRATING", isCalibrating ? "TRUE" : "FALSE");
					notifyUpdatedSettings(settings);
				} else
				{

				}
			}
			settings.put("HOLD", triggerSettings.getRun_Hold() ? "TRUE" : "FALSE");
			settings.put("AUTORANGE", triggerSettings.getAutorange() ? "TRUE" : "FALSE");
			notifyUpdatedSettings(settings);
			if (!hasSettings)
				hasSettings = true;
		}

		//samples
		
		if (currentPacket.getCommand() == Commands.RECEIVE_SAMPLES_FROM_SCOPE
				.getValue())
		{
			int samplesSize = (1042 - 18) / 2;
			byte[] sampleCH1 = new byte[samplesSize];
			byte[] sampleCH2 = new byte[samplesSize];
			byte offSetHigh, offSetLow;
			int offSet;
			offSetLow = currentPacket.getData(0);
			offSetHigh = currentPacket.getData(1);
			offSet = (offSetHigh << 8) | offSetLow;
			offSet /= 2;
			int ch1Count = 0, ch2Count = 0;
			int length = currentPacket.getSize();

			triggerSettings.setTriggerLevel(unsignedToBytes(currentPacket
					.getData(9)));
			byte modulestatus = currentPacket.getData(11);
			byte buf = (byte) (modulestatus & 7);
			byte buf2 = (byte) (modulestatus & 32);

			Boolean isChanged = false;
			if (buf == 4 && previousstatus != 0)
			{
				settings.put("BATTERY", "CHARGING");
				previousstatus = 0;
				isChanged = true;
			}
			if (buf == 2 && previousstatus != 1)
			{
				settings.put("BATTERY", "FULL");
				previousstatus = 1;
				isChanged = true;
			}
			if (buf2 == 32 && previousstatus != 2)
			{
				settings.put("BATTERY", "LOW");
				previousstatus = 2;
				isChanged = true;
			}
			if (buf != 2 && buf != 4 && buf2 != 32 && previousstatus != 3)
			{
				settings.put("BATTERY", "NO");
				previousstatus = 3;
				isChanged = true;
			}
			if (isChanged)
			{
				notifyUpdatedSettings(settings);
				isChanged = false;
			}
			byte calibrating = (byte) (modulestatus >> 4);
			calibrating = (byte) (calibrating & 0x01);
			if (calibrating == 1)
			{
				if (!isCalibrating)
				{
					isCalibrating = true;

					settings.put("CALIBRATING", isCalibrating ? "TRUE" : "FALSE");
					notifyUpdatedSettings(settings);
				}

			} else
			{
				if (isCalibrating)
				{
					isCalibrating = false;
					settings.put("CALIBRATING", isCalibrating ? "TRUE" : "FALSE");
					notifyUpdatedSettings(settings);
				} else
				{

				}
			}

			for (int i = 12; i < length - 6; i++)
			{
				if (i % 2 != 0)
				{

					sampleCH2[ch2Count] = currentPacket.getData(i);

					ch2Count++;
				} else
				{
					sampleCH1[ch1Count] = currentPacket.getData(i);
					ch1Count++;
				}

			}

			System.arraycopy(sampleCH1, 0, channel1.getSamples(), offSet, (length - 18) / 2);
			System.arraycopy(sampleCH2, 0, channel2.getSamples(), offSet, (length - 18) / 2);
			if (offSet == 2048)
			{
				channel1.isNewData = true;
				channel2.isNewData = true;
				isNewData = true;
				newDataFrame();
				
			}
			if (isFakeData)
			{
				isFakeData = false;
				requestSettings();
			}

		}

		if (currentPacket.getCommand() == Commands.RECEIVE_WIFI_SETTINGS
				.getValue())
		{
			byte wifiChannelHigh, wifiChannelLow;
			int wifiChannel;

			wifiChannelLow = currentPacket.getData(2);
			wifiChannelHigh = currentPacket.getData(3);
			wifiChannel = (wifiChannelHigh << 8) | wifiChannelLow;
			wifiSettings.put("WIFICHANNEL", Integer.toString(wifiChannel));
			wifiSettings.put("WIFINAME", getWifiName(currentPacket));
			wifiSettings.put("WIFIPASSWORD", getWifiPassword(currentPacket));
			wifiSettings.put("WIFIVERSION", getWifiBuildNumber(currentPacket));
			wifiSettings
					.put("SCOPEVERSION", getScopeBuildNumber(currentPacket));
			notifyUpdatedWifiSettings(wifiSettings);

		}

	}

	/**
	 * Requests
	 * 
	 * @return is <code>true</code> if the request is succesfully sended else
	 *         <code>false</code>
	 */
	@Override
	public Boolean requestCalibrate()
	{
		Packet requestSettings = new Packet(8);
		requestSettings.setCommand(Commands.START_CALIBRATE_REQUEST);
		requestSettings.finalize();
		try
		{
			connector.send(requestSettings);
		} catch (IOException e)
		{
			return false;
		}
		return true;
	}

	@Override
	public Boolean sendWifiSettings(String wifiName, String wifiChannel)
	{

		Packet WifiSettings = new Packet(74);
		WifiSettings.setCommand(Commands.SEND_WIFI_SETTINGS);
		WifiSettings.setData(2, 0);
		char[] data = wifiName.toCharArray();
		int i = 4;
		for (char c : data)
		{
			WifiSettings.setData(i, c);
			i++;
		}

		data = " ".toCharArray();
		i = 36;
		for (char c : data)
		{
			WifiSettings.setData(i, c);
			i++;
		}

		WifiSettings.finalize();
		try
		{
			connector.send(WifiSettings);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			return false;
		}
		return true;

	}

	public String getWifiName(Packet p)
	{
		byte[] wifiname = new byte[32];
		for (int i = 0; i < 32; i++)
		{
			wifiname[i] = p.getData(i + 4);
		}
		String s = new String(wifiname);
		s = s.trim();
		return s;
	}

	public String getWifiPassword(Packet p)
	{
		byte[] wifiname = new byte[32];
		for (int i = 0; i < 32; i++)
		{
			wifiname[i] = p.getData(i + 36);
		}
		String s = new String(wifiname);
		s = s.trim();
		return s;
	}

	public String getScopeBuildNumber(Packet p)
	{
		byte[] wifiname = new byte[4];
		for (int i = 0; i < 4; i++)
		{
			wifiname[i] = p.getData(i + 68);
		}
		String s = new String(wifiname);
		s = s.trim();
		return s;
	}

	public String getWifiBuildNumber(Packet p)
	{
		byte[] wifiname = new byte[16];
		for (int i = 0; i < 16; i++)
		{
			wifiname[i] = p.getData(i + 72);
		}
		String s = new String(wifiname);
		s = s.trim();
		return s;
	}

	static Boolean isRunningCalc = false;

	/**
	 *   
	 */

	public Commands getCommandByValue(int i)
	{
		Commands command = null;
		switch (i)
		{
		case 32:
			command = Commands.RECEIVE_SCOPE_SETTINGS;
		default:
			break;
		}
		return command;

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

	@Override
	public void disconnected()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void connected()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void generateFakeSignals()
	{
		// TODO Auto-generated method stub

	}

}

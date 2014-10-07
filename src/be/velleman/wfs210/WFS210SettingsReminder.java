package be.velleman.wfs210;

import java.util.HashMap;
import java.util.Map;

import be.velleman.wfs210.Channel.InputCoupling;
import be.velleman.wfs210.Trigger.ManualTriggering;
import be.velleman.wfs210.Trigger.RestartTriggering;
import be.velleman.wfs210.Trigger.TriggerChannel;
import be.velleman.wfs210.Trigger.TriggerMode;
import be.velleman.wfs210.Trigger.TriggerSlope;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class WFS210SettingsReminder implements ScopeDataChangedListener
{

	Context context;
	Boolean hasSettings;

	public WFS210SettingsReminder(Context newContext)
	{
		context = newContext;

	}

	@Override
	public void updatedSettings(Map<String, String> settings)
	{
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		Boolean isCorrupt = false;
		for (String s : settings.keySet())
		{
			if (settings.get(s) != null)
				editor.putString(s, settings.get(s));
			else
			{
				isCorrupt = true;
				break;
			}
		}
		if (isCorrupt)
		{
			setDefaultSettings();
		}
		if (sp.getString("SETTINGS", "FALSE").equals("FALSE"))
		{
			editor.putString("SETTINGS", "TRUE");
		}
		editor.commit();

	}

	private void setDefaultSettings()
	{
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString("VPOS1", "128");
		editor.putString("VPOS2", "128");
		editor.putString("VDIV1", "1V");
		editor.putString("VDIV2", "1V");
		editor.putString("AUTORANGE", "FALSE");
		editor.putString("IC1", "AC");
		editor.putString("IC2", "AC");
		editor.putString("TRIGGERCHANNEL", "CH1");
		editor.putString("TIMEBASE", "1ms");
		editor.putString("HOLD", "FALSE");
		editor.putString("TRIGGERSLOPE", "RISING");
		editor.putString("TRIGGERCHANNEL", "CH1");
		editor.putString("TRIGGERLEVEL", "128");
		editor.putString("TRIGGERMODE", "AUTO");
		editor.commit();

	}

	@Override
	public void updatedWifiSettings(Map<String, String> wifiSettings)
	{
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		Boolean isCorrupt = false;
		for (String s : wifiSettings.keySet())
		{
			if (wifiSettings.get(s) != null)
				editor.putString(s, wifiSettings.get(s));
			else
			{
				isCorrupt = true;
				break;
			}
		}
		if (isCorrupt)
		{
			setDefaultSettings();
		}
		if (sp.getString("SETTINGS", "FALSE").equals("FALSE"))
		{
			editor.putString("SETTINGS", "TRUE");
		}
		editor.commit();

	}

	public Map<String, String> getWFS210Settings()
	{
		Map<String, String> settings = new HashMap<String, String>();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		settings.put("VPOS1", sp.getString("VPOS1", "0"));
		settings.put("VPOS2", sp.getString("VPOS2", "0"));
		settings.put("VDIV1", sp.getString("VDIV1", "OFF"));
		settings.put("VDIV2", sp.getString("VDIV2", "OFF"));
		settings.put("AUTORANGE", sp.getString("AUTORANGE", "FALSE"));
		settings.put("IC1", sp.getString("IC1", "AC"));
		settings.put("IC2", sp.getString("IC2", "AC"));
		settings.put("TRIGGERCHANNEL", sp.getString("TRIGGERCHANNEL", "CH1"));
		settings.put("TIMEBASE", sp.getString("TIMEBASE", "1ms"));
		settings.put("HOLD", sp.getString("HOLD", "TRUE"));
		settings.put("TRIGGERSLOPE", sp.getString("TRIGGERSLOPE", "RISING"));
		settings.put("TRIGGERCHANNEL", sp.getString("TRIGGERCHANNEL", "CH1"));
		settings.put("TRIGGERLEVEL", sp.getString("TRIGGERLEVEL", "128"));
		settings.put("TRIGGERMODE", sp.getString("TRIGGERMODE", "AUTO"));
		return settings;
	}

	public Boolean hasSettings()
	{
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (sp.getString("SETTINGS", "FALSE").equals("FALSE"))
		{
			return false;
		} else
		{
			return true;
		}
	}

}

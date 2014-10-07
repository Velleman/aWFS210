package be.velleman.wfs210;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference scopeversion = (Preference) getPreferenceManager()
				.findPreference("VERSIONNUMBERSCOPE");
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		scopeversion.setTitle("SCOPE VERSION: " + sp
				.getString("VERSIONNUMBERSCOPE", "unknown scope version"));

		Preference wifiversion = (Preference) getPreferenceManager()
				.findPreference("VERSIONNUMBERWIFI");
		wifiversion.setTitle("WIFI VERSION: " + sp
				.getString("VERSIONNUMBERWIFI", "unknown wifi version"));

		EditTextPreference wifiName = (EditTextPreference) getPreferenceManager()
				.findPreference("WIFINAME");
		wifiName.setDefaultValue(sp
				.getString("WIFINAME", "No scope name found"));

		Preference button = (Preference) getPreferenceManager()
				.findPreference("CALIBRATE");
		if (button != null)
		{
			button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference arg0)
				{
					SharedPreferences.Editor editor = sp.edit();
					editor.putBoolean("STARTCALIBRATING", true);
					editor.commit();
					finish();
					return true;
				}
			});
		}
	}

	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		super.onStop();

	}

}

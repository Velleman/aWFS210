package be.velleman.wfs210;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class PrefsFragment extends PreferenceFragment {
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stu
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		for(int i=0; i < getPreferenceScreen().getPreferenceCount(); i++)
		{
			if(getPreferenceScreen().getPreference(i) instanceof PreferenceCategory)
			{
				PreferenceCategory pc = (PreferenceCategory) getPreferenceScreen().getPreference(i);
				if(pc.getTitle().toString().contentEquals("Versions"))
				{
					SharedPreferences sp = getPreferenceManager().getSharedPreferences();
					Preference p = pc.getPreference(0);
					p.setSummary(sp.getString("VERSIONNUMBERSCOPE", "SCOPE VERSION NOT FOUND"));
					Preference p2 = pc.getPreference(1);
					p2.setSummary(sp.getString("VERSIONNUMBERWIFI", "WIFI VERSION NOT FOUND"));
					Preference p3 = pc.getPreference(2); 
					p3.setSummary(sp.getString("APPVERSION", "APP VERSION NOT FOUND"));
				}
				if(pc.getTitle().toString().contentEquals("Settings"))
				{
					SharedPreferences sp = getPreferenceManager().getSharedPreferences();
					Preference p = pc.getPreference(0);
					p.setSummary(sp.getString("WIFINAME", ""));
				}
				if(pc.getTitle().toString().contentEquals("Calibration"))
				{
					SharedPreferences sp = getPreferenceManager().getSharedPreferences();
					Preference p = pc.getPreference(0);
					p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						
						@Override
						public boolean onPreferenceClick(Preference preference) {
							// TODO Auto-generated method stub
							
							return false;
						}
					});
				}
				
			}
		}
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
	}
}

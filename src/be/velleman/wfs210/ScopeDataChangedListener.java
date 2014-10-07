package be.velleman.wfs210;

import java.util.Map;

interface ScopeDataChangedListener
{

	public void updatedSettings(Map<String, String> settings);

	public void updatedWifiSettings(Map<String, String> wifiSettings);
}
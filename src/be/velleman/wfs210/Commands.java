package be.velleman.wfs210;

enum Commands
{
	RECEIVE_WIFI_SETTINGS(34), RECEIVE_SAMPLES_FROM_SCOPE(33), RECEIVE_SCOPE_SETTINGS(
			32), REQUEST_SCOPE_SAMPLE_DATA(18), SEND_SCOPE_SETTINGS(17), REQUEST_STATUS(
			16), REQUEST_WIFI_SETTINGS(10), SET_DEBUG_MODE(13), CLEAR_DEBUG_MODE(
			12), START_CALIBRATE_REQUEST(202), SEND_WIFI_SETTINGS(11), ENTER_BOOTLOADER(
			0x0F);
	private int value;

	private Commands(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

}

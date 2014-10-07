package be.velleman.wfs210;

public class Trigger
{

	private TriggerMode Trigger_Mode = TriggerMode.AUTO;
	private TriggerChannel triggerChannel = TriggerChannel.CH1;
	private TriggerSlope triggerSlope = TriggerSlope.RISING_EDGE;
	private Boolean isHold = false;// bool
	private RestartTriggering restartTriggering = RestartTriggering.NO_RESTART;
	private ManualTriggering manualTriggering = ManualTriggering.NO_MANUAL_TRIGGERING;
	private Boolean isAutoRange = false;// bool
	private int triggerLevel = 0;

	public TriggerMode getTrigger_Mode()
	{
		return Trigger_Mode;
	}

	public void setTrigger_Mode(TriggerMode trigger_Mode)
	{
		Trigger_Mode = trigger_Mode;
	}

	public Boolean getRun_Hold()
	{
		return isHold;
	}

	public void setRun_Hold(Boolean run_Hold)
	{
		isHold = run_Hold;
	}

	public RestartTriggering getRestart_Triggering()
	{
		return restartTriggering;
	}

	public void setRestart_Triggering(RestartTriggering restart_Triggering)
	{
		restartTriggering = restart_Triggering;
	}

	public ManualTriggering getManual_Triggering()
	{
		return manualTriggering;
	}

	public void setManual_Triggering(ManualTriggering manual_Triggering)
	{
		manualTriggering = manual_Triggering;
	}

	public Boolean getAutorange()
	{
		return isAutoRange;
	}

	public void setAutorange(Boolean autorange)
	{
		isAutoRange = autorange;
	}

	public TriggerChannel getTrigger_Channel()
	{
		return triggerChannel;
	}

	public void setTrigger_Channel(TriggerChannel trigger_Channel)
	{
		triggerChannel = trigger_Channel;
	}

	public TriggerSlope getTrigger_Slope()
	{
		return triggerSlope;
	}

	public void setTrigger_Slope(TriggerSlope trigger_Slope)
	{
		triggerSlope = trigger_Slope;
	}

	public Trigger()
	{

	}

	public int getTriggerLevel()
	{
		return triggerLevel;
	}

	public void setTriggerLevel(int triggerLevel)
	{
		this.triggerLevel = triggerLevel;
	}

	enum TriggerMode
	{
		NORMAL, AUTO, ONCE
	}

	enum TriggerChannel
	{
		CH1, CH2
	}

	enum TriggerSlope
	{
		RISING_EDGE, FALLING_EDGE
	}

	enum RestartTriggering
	{
		NO_RESTART, RESTART_TRIGGERING
	}

	enum ManualTriggering
	{
		NO_MANUAL_TRIGGERING, FORCE_MANUAL_TRIGGERING
	}

}

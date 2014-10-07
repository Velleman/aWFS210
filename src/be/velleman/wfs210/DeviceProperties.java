package be.velleman.wfs210;

public class DeviceProperties
{

	private float TOTAL_SAMPLES;
	private float DIVISIONS;

	public DeviceProperties()
	{
		// TODO Auto-generated constructor stub
	}

	public int getTOTAL_SAMPLES()
	{
		return (int) this.TOTAL_SAMPLES;
	}

	public void setTOTAL_SAMPLES(float totalsamples)
	{
		this.TOTAL_SAMPLES = totalsamples;
	}

	public float getDIVISIONS()
	{
		return DIVISIONS;
	}

	public void setDIVISIONS(float divisions)
	{
		this.DIVISIONS = divisions;
	}

	public void calculateProperties()
	{
	}
}

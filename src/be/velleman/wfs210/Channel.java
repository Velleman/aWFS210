package be.velleman.wfs210;

/**
 * @author bn
 * 
 */
public class Channel
{
	public String name;
	private InputCoupling inputCoupling;
	private VoltageDiv verticalDiv;
	private int verticalPosition;
	private byte[] samples = new byte[4096];
	public Boolean isNewData = false;

	private Boolean isX10 = false;

	/**
	 * This is the class that represents a Oscilloscope channel
	 */
	public Channel()
	{
		super();
		for (int i = 0; i <= samples.length - 1; i++)
		{
			this.samples[i] = 0;
		}
	}

	public int getVerticalPosition()
	{
		return verticalPosition;
	}

	public void setVerticalPosition(int verticalPosition)
	{
		this.verticalPosition = verticalPosition;
	}

	public Channel(String sName)
	{
		super();
		this.name = sName;
		for (int i = 0; i <= samples.length - 1; i++)
		{
			this.samples[i] = 0;
		}
	}

	public InputCoupling getInputCoupling()
	{
		return inputCoupling;
	}

	public void setInputCoupling(InputCoupling inputCoupling)
	{
		this.inputCoupling = inputCoupling;
	}

	public VoltageDiv getVerticalDiv()
	{
		return verticalDiv;
	}

	public void setVerticalDiv(VoltageDiv verticalDiv)
	{
		this.verticalDiv = verticalDiv;
	}

	/**
	 * Checks if the current channel is filled or not
	 * 
	 * @return is <code>true</code> if the channel settings are filled else
	 *         <code>false</false>
	 */
	public Boolean isFilled()
	{
		if (this.verticalDiv == null && this.inputCoupling == null)
		{
			return false;
		} else
		{
			return true;
		}
	}

	/**
	 * This will set new data at a given offset
	 * 
	 * @param offSet
	 *            the offset for the data
	 * @param data
	 *            the data that needs to be replaced
	 */
	public void setSampleData(int offSet, byte[] data)
	{
		int length = data.length - 1;
		for (int i = offSet; i <= length + offSet; i++)
		{
			if (i - offSet == 0)
				this.samples[i] = data[i - offSet];
			else
				this.samples[i] = data[i - offSet - 1];

		}
		if (offSet >= 2048)
			isNewData = true;

	}

	/**
	 * This will retrieve data from this channel within a range
	 * 
	 * @param start
	 *            the start position where the data needs to be taken
	 * @param end
	 *            the end positon where the data needs to be taken
	 * @return Returns a byte array of length end-start
	 */
	public byte[] getSamplDataFromRange(int start, int end)
	{
		byte[] data;
		if (end == 0 && start == 0)
			data = new byte[1];
		else
			data = new byte[end - start];
		int length = end - 1;
		for (int i = start; i <= length; i++)
		{
			data[i - start] = this.samples[i];
		}
		return data;
	}

	public Boolean getIsX10()
	{
		return isX10;
	}

	public void setIsX10(Boolean isX10)
	{
		this.isX10 = isX10;
	}

	public byte[] getSamples()
	{
		return this.samples;
	}

	enum InputCoupling
	{
		AC, DC, GND
	}

}

package be.velleman.wfs210;

enum VoltageDiv
{
	off(0), VDIV_20V(1), VDIV_10V(2), VDIV_4V(3), VDIV_2V(4), VDIV_1(5), VDIV_500mV(
			6), VDIV_200mV(7), VDIV_100mV(8), VDIV_50mV(9), VDIV_25mV(10), VDIV_10mV(
			11), VDIV_5mV(12);
	private static VoltageDiv[] allValues = values();

	public static VoltageDiv fromOrdinal(int n)
	{
		return allValues[n];
	}

	private int value;

	private VoltageDiv(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
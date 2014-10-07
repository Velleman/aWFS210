package be.velleman.wfs210;

public enum TimeBase
{
	HDIV_1uS(0), HDIV_2uS(1), HDIV_5uS(2), HDIV_10uS(3), HDIV_20uS(4), HDIV_50uS(
			5), HDIV_100uS(6), HDIV_200uS(7), HDIV_500uS(8), HDIV_1mS(9), HDIV_2mS(
			10), HDIV_5mS(11), HDIV_10mS(12), HDIV_20mS(13), HDIV_50mS(14), HDIV_100mS(
			15), HDIV_200mS(16), HDIV_500mS(17), HDIV_1S(18);
	private static TimeBase[] allValues = values();
	private int value;

	public static TimeBase fromOrdinal(int n)
	{
		return allValues[n];
	}

	private TimeBase(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}

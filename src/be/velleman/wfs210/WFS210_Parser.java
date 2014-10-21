package be.velleman.wfs210;

import data.Buffer;

/**
 * @author Brecht Nuyttens For Velleman nv
 * 
 */
public class WFS210_Parser
{

	private static final int HeaderSize = 4;
	private static final int StxChar = 0x02;
	private static final int EtxChar = 0x0a;
	private Buffer buffer;
	private Packet foundPacket;

	/**
	 * 
	 */
	public WFS210_Parser()
	{
		buffer = new Buffer(10240);
	}

	/**
	 * @param inComingDat
	 *            The data that needs to be added to the parser
	 * @param length
	 *            The length of the data
	 */
	public void addDataToParse(byte[] inComingData, int length)
	{
		//Log.i(TAG,Integer.toString(length));
		buffer.Append(inComingData, length);
	}

	long time1, time2;

	/**
	 * This will try to find a packet in the buffer
	 * 
	 * @return Returns a Packet if found else <code>null</code>
	 */
	public Packet parseNext()
	{
		time1 = System.currentTimeMillis();
		int PacketSize;
		foundPacket = null;

		while ((buffer.getSize() >= HeaderSize) && (!bufferValid()))
		{
			seekTo((byte) StxChar);
		}
		if (buffer.getSize() >= HeaderSize)
		{
			if (bufferValid())
			{
				PacketSize = (int) ((unsignedToBytes(buffer.GetData(3)) << 8) | unsignedToBytes(buffer
						.GetData(2)));
				if (buffer.getSize() >= PacketSize)
				{

					foundPacket = new Packet(PacketSize);
					for (int i = 0; i < PacketSize - 1; i++)
					{
						foundPacket.setRawData(i,buffer.GetData(i));
					}

					buffer.Discard(PacketSize);
				}
			}
		}
		time2 = System.currentTimeMillis();
		//Log.i(TAG,Long.toString(time2-time1));
		return foundPacket;

	}

	/**
	 * Checks if the buffer of the parser is valid
	 * 
	 * @return is <code>true
	 */
	public Boolean bufferValid()
	{
		int lSize;
		if (buffer.getSize() >= HeaderSize)
		{
			byte high = (byte) unsignedToBytes(buffer.GetData(3));
			byte low = (byte) unsignedToBytes(buffer.GetData(2));
			lSize = (int) (((high) << 8) | low);
			if (lSize > 1042)
			{
				return false;
			}
			if (buffer.GetData(0) != StxChar)
				return false;
			if (buffer.getSize() >= lSize)
			{
				if (buffer.GetData(lSize - 1) != EtxChar)
					return false;
			}
		}
		return true;
	}

	/**
	 * @param value
	 */
	public void seekTo(byte value)
	{
		int i = 1;
		while ((i < buffer.getSize()) && (buffer.GetData(i) != value))
			i++;
		buffer.Discard(i);
	}

	/**
	 * @param b
	 * @return
	 */
	private static int unsignedToBytes(byte b)
	{
		return b & 0xFF;
	}

	/**
	 * @param data
	 * @return
	 */
	public byte calculateCheckSum(byte[] data)
	{
		int chkSum = 0;
		int length = data.length;
		for (int i = 0; i < length; i++)
		{

			chkSum = (chkSum + data[i]);
		}
		chkSum = ~chkSum;
		chkSum += 1;
		return (byte) chkSum;
	}

}
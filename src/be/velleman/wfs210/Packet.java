package be.velleman.wfs210;

public class Packet
{

	public int getSize()
	{
		return bufferSize;
	}

	private byte[] buffer;
	private int bufferSize;
	public static final int STX = 2, ETX = 0x0a;
	public static final String TAG = "Packet";

	public Packet(int size)
	{
		buffer = new byte[size];
		bufferSize = size;
		if (size > 255)
		{
			buffer[2] = (byte) 255;
			buffer[3] = (byte) (size - 255);
		} else
			if (size > 0)
			{
				buffer[2] = (byte) size;
				buffer[3] = 0x00;
			}

	}

	public void finalize()
	{
		buffer[0] = Packet.STX;
		buffer[buffer.length - 1] = Packet.ETX;
		byte chkSum = calculateCheckSum(buffer);
		buffer[buffer.length - 2] = chkSum;
	}

	public byte calculateCheckSum(byte[] data)
	{
		byte chkSum = 0;
		int length = data.length - 3;
		for (int i = 0; i < length; i++)
		{

			chkSum = (byte) (chkSum + data[i]);
		}
		chkSum = (byte) ~chkSum;
		chkSum += 1;
		return chkSum;
	}

	public byte[] getPacket()
	{
		return buffer;
	}

	public Boolean setData(int position, int data)
	{
		if (bufferSize == 0)
		{
			return false;
		} else
		{
			if ((position + 4) > (bufferSize - 2))
			{
				throw new IllegalArgumentException();

			} else
			{
				buffer[position + 4] = (byte) data;
				return true;
			}

		}
	}

	public byte getData(int position)
	{
		if (bufferSize == 0)
		{
			throw new NullPointerException();
		} else
		{
			if ((position + 4) > (bufferSize - 2))
			{
				throw new IllegalArgumentException();

			} else
			{
				return buffer[position + 4];
			}

		}
	}

	public void setCommand(Commands commands)
	{
		buffer[1] = (byte) commands.getValue();
	}

	public byte getCommand()
	{
		return buffer[1];
	}

	public void setPacket(byte[] packet)
	{
		buffer = packet;
	}

}

package data;

import java.nio.BufferOverflowException;

public class Buffer
{
	byte[] Data;
	int Capacity = 0;
	int Size = 0;
	int Garbage = 0;

	public Buffer(int Cap)
	{
		this.Capacity = Cap;
		Data = new byte[this.Capacity];
	}

	public void Clear()
	{
		Size = 0;
		Garbage = 0;
	}

	public byte GetData(int pos)
	{
		if ((Garbage + pos) < Capacity)
			if (pos < 0)
			{
				return 0;
			} else
				return this.Data[Garbage + pos];
		else
			return 0;
	}

	public void SetData(int pos, byte val)
	{
		this.Data[Garbage + pos] = val;
	}

	public int GetBytesFree()
	{
		return Capacity - Garbage - Size;
	}

	public void GarbageCollect()
	{
		if (Garbage > 0)
		{
			System.arraycopy(Data, Garbage, Data, 0, this.Size);
			Garbage = 0;
		}
	}

	public int getCapacity()
	{
		return Data.length;
	}

	/**
	 * Gets the total number of bytes that are still available to be appended to
	 * the buffer. This value may increase after garbage collection.
	 * 
	 * @return available bytes that can be appended to the buffer
	 */
	public int getAvailableSize()
	{
		return (getCapacity() - this.Garbage - Size);
	}

	public int getSize()
	{
		return Size;
	}

	public void Append(byte[] data, int size)
	{
		if (getAvailableSize() < size)
		{
			GarbageCollect();
		}

		if (getAvailableSize() >= size)
		{
			System.arraycopy(data, 0, Data, Garbage + getSize(), size);
			Size += size;
		} else
		{
			throw new BufferOverflowException();
		}
	}

	public void Discard(int Count)
	{
		Size = Size - Count;
		if (this.Size == 0)
		{
			Garbage = 0; // free garbage collect
		} else
		{
			Garbage += Count;
		}
	}

}

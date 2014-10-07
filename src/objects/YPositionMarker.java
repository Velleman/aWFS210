package objects;

import android.graphics.Point;
import be.velleman.wfs210.Constants;

public class YPositionMarker extends Marker
{
	private int id;
	private int totalSamples;

	public YPositionMarker(int samples)
	{
		this.totalSamples = samples;
		this.VERTEX_DATA[0] = 0;
		this.VERTEX_DATA[1] = 128;
		this.VERTEX_DATA[2] = 255;
		this.VERTEX_DATA[3] = 255;
		this.VERTEX_DATA[4] = 255;
		this.VERTEX_DATA[5] = this.totalSamples;
		this.VERTEX_DATA[6] = 128;
		this.VERTEX_DATA[7] = 255;
		this.VERTEX_DATA[8] = 255;
		this.VERTEX_DATA[9] = 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
		this.isXMarker = false;
	}

	public YPositionMarker(int Pos, int samples)
	{

		this.VERTEX_DATA[0] = 0;
		this.VERTEX_DATA[1] = Pos;
		this.VERTEX_DATA[2] = 255 / 255;
		this.VERTEX_DATA[3] = 255 / 255;
		this.VERTEX_DATA[4] = 255 / 255;
		this.VERTEX_DATA[5] = 255 / 255;
		this.VERTEX_DATA[6] = samples;
		this.VERTEX_DATA[7] = Pos;
		this.VERTEX_DATA[8] = 255 / 255;
		this.VERTEX_DATA[9] = 255 / 255;
		this.VERTEX_DATA[10] = 255 / 255;
		this.VERTEX_DATA[11] = 255 / 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
		this.isXMarker = false;
	}

	public void setData(int Pos)
	{

		if (Pos > Constants.SAMPLE_HEIGHT)
		{
			this.VERTEX_DATA[1] = Constants.SAMPLE_HEIGHT;
			this.VERTEX_DATA[7] = Constants.SAMPLE_HEIGHT;
		}
		if (Pos < 0)
		{
			this.VERTEX_DATA[1] = 0;
			this.VERTEX_DATA[7] = 0;
		}
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public Point getPosition()
	{
		return new Point((int) VERTEX_DATA[0], (int) VERTEX_DATA[1]);
	}

	public void SetColor(int R, int G, int B, int A)
	{
		this.VERTEX_DATA[2] = (float) R / 255;
		this.VERTEX_DATA[3] = (float) G / 255;
		this.VERTEX_DATA[4] = (float) B / 255;
		this.VERTEX_DATA[5] = (float) A / 255;
		this.VERTEX_DATA[8] = (float) R / 255;
		this.VERTEX_DATA[9] = (float) G / 255;
		this.VERTEX_DATA[10] = (float) B / 255;
		this.VERTEX_DATA[11] = (float) A / 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

}

package objects;

import android.graphics.Point;
import be.velleman.wfs210.Constants;

public class YTriggerLevelMarker extends Marker
{

	private int samples;

	public YTriggerLevelMarker(int totalSamples)
	{
		this.samples = totalSamples;
		this.VERTEX_DATA[0] = 0;
		this.VERTEX_DATA[1] = 128;
		this.VERTEX_DATA[2] = 255;
		this.VERTEX_DATA[3] = 255;
		this.VERTEX_DATA[4] = 255;
		this.VERTEX_DATA[5] = this.samples;
		this.VERTEX_DATA[6] = 128;
		this.VERTEX_DATA[7] = 255;
		this.VERTEX_DATA[8] = 255;
		this.VERTEX_DATA[9] = 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
		this.isXMarker = false;
	}

	public YTriggerLevelMarker(int Pos, int samples)
	{

		this.VERTEX_DATA[0] = 0;
		this.VERTEX_DATA[1] = Pos;
		this.VERTEX_DATA[2] = 255 / 255;
		this.VERTEX_DATA[3] = 255 / 255;
		this.VERTEX_DATA[4] = 255 / 255;
		this.VERTEX_DATA[5] = 255 / 255;
		this.VERTEX_DATA[6] = samples;
		this.VERTEX_DATA[7] = Pos;
		this.VERTEX_DATA[8] = 255;
		this.VERTEX_DATA[9] = 255;
		this.VERTEX_DATA[10] = 255;
		this.VERTEX_DATA[11] = 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
		this.isXMarker = false;
	}

	public Point getPosition()
	{
		return new Point((int) VERTEX_DATA[0], (int) VERTEX_DATA[1]);
	}

	public void setData(int Pos)
	{

		if (Pos > Constants.SAMPLE_HEIGHT)
		{
			this.VERTEX_DATA[1] = Constants.SAMPLE_HEIGHT - 1;
			this.VERTEX_DATA[7] = Constants.SAMPLE_HEIGHT - 1;
		}
		if (Pos < 0)
		{
			this.VERTEX_DATA[1] = 1;
			this.VERTEX_DATA[7] = 1;
		}
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public void SetColor(int R, int G, int B, int A)
	{
		this.VERTEX_DATA[2] = R / 255;
		this.VERTEX_DATA[3] = G / 255;
		this.VERTEX_DATA[4] = B / 255;
		this.VERTEX_DATA[5] = A / 255;
		this.VERTEX_DATA[8] = R / 255;
		this.VERTEX_DATA[9] = G / 255;
		this.VERTEX_DATA[10] = B / 255;
		this.VERTEX_DATA[11] = A / 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

}

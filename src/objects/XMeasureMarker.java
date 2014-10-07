package objects;

import static be.velleman.wfs210.Constants.SAMPLE_HEIGHT;
import android.graphics.Point;
import data.VertexArray;

public class XMeasureMarker extends Marker
{
	private int totalSamples;

	public XMeasureMarker(int samples)
	{
		this.totalSamples = samples;
		this.VERTEX_DATA[0] = this.totalSamples / 2;
		this.VERTEX_DATA[1] = 0;
		this.VERTEX_DATA[2] = 255;
		this.VERTEX_DATA[3] = 255;
		this.VERTEX_DATA[4] = 255;
		this.VERTEX_DATA[5] = this.totalSamples / 2;
		this.VERTEX_DATA[6] = SAMPLE_HEIGHT;
		this.VERTEX_DATA[7] = 255;
		this.VERTEX_DATA[8] = 255;
		this.VERTEX_DATA[9] = 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public XMeasureMarker(int Pos, int samples)
	{
		this.totalSamples = samples;
		this.VERTEX_DATA[0] = Pos;
		this.VERTEX_DATA[1] = 0;
		this.VERTEX_DATA[2] = 255;
		this.VERTEX_DATA[3] = 255;
		this.VERTEX_DATA[4] = 255;
		this.VERTEX_DATA[5] = 255;
		this.VERTEX_DATA[6] = Pos;
		this.VERTEX_DATA[7] = SAMPLE_HEIGHT;
		this.VERTEX_DATA[8] = 255;
		this.VERTEX_DATA[9] = 255;
		this.VERTEX_DATA[10] = 255;
		this.VERTEX_DATA[11] = 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public Point getPosition()
	{
		return new Point((int) VERTEX_DATA[0], (int) VERTEX_DATA[1]);
	}

	public void setData(int Pos)
	{
		if (Pos > this.totalSamples)
		{
			this.VERTEX_DATA[0] = this.totalSamples - 5;
			this.VERTEX_DATA[6] = this.totalSamples - 5;
		} else
			if (Pos <= 0)
			{
				this.VERTEX_DATA[0] = 5;
				this.VERTEX_DATA[6] = 5;
			} else
			{
				this.VERTEX_DATA[0] = Pos;
				this.VERTEX_DATA[6] = Pos;
			}

		this.vertexArray = new VertexArray(this.VERTEX_DATA);
	}

	public void SetColor(int R, int G, int B, int A)
	{
		this.VERTEX_DATA[2] = R;
		this.VERTEX_DATA[3] = G;
		this.VERTEX_DATA[4] = B;
		this.VERTEX_DATA[5] = A;
		this.VERTEX_DATA[8] = R;
		this.VERTEX_DATA[9] = G;
		this.VERTEX_DATA[10] = B;
		this.VERTEX_DATA[11] = A;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

}

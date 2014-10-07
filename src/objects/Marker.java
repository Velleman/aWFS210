package objects;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.glDrawArrays;
import static be.velleman.wfs210.Constants.BYTES_PER_FLOAT;
import be.velleman.wfs210.Constants;
import android.graphics.Point;
import data.VertexArray;
import programs.ColorShaderProgram;

public class Marker
{
	protected static final int POSITION_COMPONENT_COUNT = 2;
	protected static final int COLOR_COMPONENT_COUNT = 4;
	protected static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

	protected float[] VERTEX_DATA =
	{
			// Order of coordinates: X1,Y1,R1,G1,B1,A1,X2,Y2,R2,G2,B2,A2

			// Triangle Fan
	0, 0, 128, 128, 0, 0, 0, 255, 128, 128, 0, 0 };

	protected VertexArray vertexArray;
	protected Point position;
	public Boolean isTouched = false;
	public int Z = 0;
	public Boolean isXMarker = true;

	public Marker()
	{
		this.vertexArray = new VertexArray(this.VERTEX_DATA);
		this.position = new Point();
		this.position.x = 0;
		this.position.y = 128;
	}

	public Marker(int xPos1, int yPos1, int R1, int G1, int B1, int xPos2,
			int yPos2, int R2, int G2, int B2)
	{
		this.vertexArray = new VertexArray(this.VERTEX_DATA);
	}

	public void bindData(ColorShaderProgram colorProgram)
	{
		vertexArray
				.setVertexAttribPointer(0, colorProgram
						.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);

		vertexArray
				.setVertexAttribPointer(POSITION_COMPONENT_COUNT, colorProgram
						.getColorAttributeLocation(), COLOR_COMPONENT_COUNT, STRIDE);
	}

	public void draw()
	{
		glDrawArrays(GL_LINES, 0, 2);
	}

	public void setData(int yPos, int xPos)
	{
		if (yPos > Constants.SAMPLE_HEIGHT)
		{
			this.VERTEX_DATA[1] = Constants.SAMPLE_HEIGHT - 5;
			this.VERTEX_DATA[7] = Constants.SAMPLE_HEIGHT - 5;
		} else
			if (yPos <= 0)
			{
				this.VERTEX_DATA[1] = 5;
				this.VERTEX_DATA[7] = 5;
			} else
			{
				this.VERTEX_DATA[1] = yPos;
				this.VERTEX_DATA[7] = yPos;
			}

		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public void setData(int xPos1, int yPos1, int R1, int G1, int B1, int A1,
			int xPos2, int yPos2, int R2, int G2, int B2, int A2)
	{
		this.VERTEX_DATA[0] = xPos1;
		this.VERTEX_DATA[1] = yPos1;
		this.VERTEX_DATA[2] = R1 / 255;
		this.VERTEX_DATA[3] = G1 / 255;
		this.VERTEX_DATA[4] = B1 / 255;
		this.VERTEX_DATA[5] = A1 / 255;
		this.VERTEX_DATA[6] = xPos2;
		this.VERTEX_DATA[7] = yPos2;
		this.VERTEX_DATA[8] = R2 / 255;
		this.VERTEX_DATA[9] = G2 / 255;
		this.VERTEX_DATA[10] = B2 / 255;
		this.VERTEX_DATA[11] = A2 / 255;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

	public Point getPosition()
	{
		return new Point((int) VERTEX_DATA[0], (int) VERTEX_DATA[1]);
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

	public void setAlpha(int a)
	{
		float alpha = (float) a / 255;
		this.VERTEX_DATA[5] = alpha;
		this.VERTEX_DATA[11] = alpha;
		this.vertexArray.SetData(this.VERTEX_DATA);
	}

}

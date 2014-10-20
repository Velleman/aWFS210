package objects;

import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static be.velleman.wfs210.Constants.BYTES_PER_FLOAT;
import be.velleman.wfs210.DeviceProperties;
import be.velleman.wfs210.TimeBase;
import be.velleman.wfs210.WFS210;
import programs.ColorShaderProgram;
import data.VertexArray;

public class ChannelSamples
{
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int COLOR_COMPONENT_COUNT = 4;
	private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
	private float[] fData;
	private DeviceProperties dp;
	private float[] VERTEX_DATA;
	private WFS210 scope;
	private final VertexArray vertexArray;
	private int drawableSamples;

	private double arraySize = 0;

	public ChannelSamples(DeviceProperties device)
	{
		dp = device;

		VERTEX_DATA = new float[(int) (dp.getTOTAL_SAMPLES() * (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT))];
		vertexArray = new VertexArray(VERTEX_DATA);
	}

	public void setScope(WFS210 sc)
	{
		scope = sc;
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
		if (scope != null && scope.getTimeBase() != null)
		{
			if (scope.getTimeBase().ordinal() == TimeBase.HDIV_1uS.ordinal())
			{
				glDrawArrays(GL_LINE_STRIP, 0, drawableSamples);
			} else
				if (scope.getTimeBase().ordinal() == TimeBase.HDIV_2uS
						.ordinal())
				{

					glDrawArrays(GL_LINE_STRIP, 0, drawableSamples);
				} else
				{
					glDrawArrays(GL_LINE_STRIP, 0, drawableSamples);
				}
		}

	}

	public void setData(byte[] data, int r, int g, int b)
	{

		//for (int i = 0; i <= length; i++) {
		//	fch1[i] = (float) unsignedToBytes(data[i]);
		//}		

		vertexArray.SetData(addPlacesAndColor(data, r, g, b));
	}

	private float[] addPlacesAndColor(byte[] samples, float r, float g, float b)
	{

		drawableSamples = samples.length - 1;
		if (drawableSamples != arraySize)
		{
			if ((samples.length % 2) == 0)
				fData = new float[(samples.length) * (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)];
			else
				fData = new float[samples.length * (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT)];
			arraySize = drawableSamples;
		}

		int y = 0;
		float x = 0;
		int length = fData.length - 1;
		for (int i = 0; i <= length - 1; i++)
		{
			if (scope != null && scope.getTimeBase() != null)
			{
				if (scope.getTimeBase().ordinal() == TimeBase.HDIV_1uS
						.ordinal())
				{
					fData[i] = x;
					x += 5;
				} else
					if (scope.getTimeBase().ordinal() == TimeBase.HDIV_2uS
							.ordinal())
					{

						fData[i] = x;
						x += 2.5f;
					} else
					{
						fData[i] = y;
					}
			} else
			{
				fData[i] = y;
				x++;
			}
			i++;
			if (y < samples.length)
				fData[i] = unsignedToBytes(samples[y]);
			y++;
			i++;
			fData[i] = r / 255;
			i++;
			fData[i] = g / 255;
			i++;
			fData[i] = b / 255;
			i++;
			fData[i] = 1f;

		}
		return fData;
	}

	private static int unsignedToBytes(byte b)
	{
		return b & 0xFF;
	}

}

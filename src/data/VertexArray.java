/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package data;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static be.velleman.wfs210.Constants.BYTES_PER_FLOAT;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexArray
{
	private FloatBuffer floatBuffer;
	private Double vertexSize = 0d;

	public VertexArray(float[] vertexData)
	{
		floatBuffer = ByteBuffer
				.allocateDirect(vertexData.length * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
	}

	public void setVertexAttribPointer(int dataOffset, int attributeLocation,
			int componentCount, int stride)
	{
		floatBuffer.position(dataOffset);
		glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, true, stride, floatBuffer);
		glEnableVertexAttribArray(attributeLocation);

	}

	public void setVertexAttribPointer(int dataOffset, int attributeLocation,
			int componentCount, int stride, Boolean normalized)
	{
		floatBuffer.position(dataOffset);
		glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, normalized, stride, floatBuffer);
		glEnableVertexAttribArray(attributeLocation);

	}

	public void SetData(float[] vertexData)
	{
		double length = (double) vertexData.length;
		if (vertexSize == length)
		{
			try
			{
				floatBuffer.clear();
				floatBuffer.put(vertexData);
			} catch (Exception e)
			{

			}

		} else
		{
			floatBuffer.clear();
			vertexSize = (double) vertexData.length;
			floatBuffer = ByteBuffer
					.allocateDirect(vertexData.length * BYTES_PER_FLOAT)
					.order(ByteOrder.nativeOrder()).asFloatBuffer()
					.put(vertexData);
		}

	}
}

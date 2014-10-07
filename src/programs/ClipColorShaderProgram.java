/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package programs;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.content.Context;

import be.velleman.wfs210.R;

public class ClipColorShaderProgram extends ShaderProgram
{
	// Uniform locations
	private final int uMatrixLocation;

	// Attribute locations
	private final int aPositionLocation;
	private final int aColorLocation;

	public ClipColorShaderProgram(Context context)
	{
		super(context, R.raw.clip_vertex_shader, R.raw.clip_fragment_shader);
		// Retrieve uniform locations for the shader program.
		uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
		// Retrieve attribute locations for the shader program.
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		aColorLocation = glGetAttribLocation(program, A_COLOR);
	}

	public void setUniforms(float[] matrix)
	{
		// Pass the matrix into the shader program.
		glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
	}

	public int getPositionAttributeLocation()
	{
		return aPositionLocation;
	}

	public int getColorAttributeLocation()
	{
		return aColorLocation;
	}
}

package be.velleman.wfs210;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.orthoM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import objects.ChannelSamples;
import objects.Line;
import objects.Marker;
import objects.XMeasureMarker;
import objects.YMeasureMarker;
import objects.YPositionMarker;
import objects.YTriggerLevelMarker;
import programs.ClipColorShaderProgram;
import programs.ColorShaderProgram;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import be.velleman.wfs210.Trigger.TriggerMode;

public class MyGLRenderer implements GLSurfaceView.Renderer
{

	private final Context context;
	int scrollPos = 0;
	private final float[] projectionMatrix = new float[16];
	private final float[] invertedViewProjectionMatrix = new float[16];
	public ChannelSamples samplesCh1;
	public ChannelSamples samplesCh2;
	Marker xMarker1, xMarker2, yMarker1, yMarker2, yPosMarker1, yPosMarker2,
			yTriggerMarker;
	private int width, height;
	private ColorShaderProgram colorProgram;
	private ClipColorShaderProgram clipColorProgram;

	WFS210 scope;
	Boolean enableMarkers = true;
	List<Marker> listOfMarkers = new ArrayList<Marker>();
	List<Line> listOfLines = new ArrayList<Line>();
	Line scrollLine = new Line();
	public DeviceProperties dp = new DeviceProperties();
	List<UpdatedMarkerListener> updatedMarkerListeners = new ArrayList<UpdatedMarkerListener>();
	float precentageview;
	List<NewFrameListener> newFrameListeners = new ArrayList<NewFrameListener>();

	public void addNewFrameListener(NewFrameListener nfl)
	{
		newFrameListeners.add(nfl);
	}

	public void newFrame()
	{
		for (NewFrameListener nfl : newFrameListeners)
		{
			nfl.newFrame();
		}
	}

	public MyGLRenderer(Context context)
	{
		this.context = context;
	}

	public void addUpdatedMarkersListener(UpdatedMarkerListener uml)
	{
		updatedMarkerListeners.add(uml);
	}

	public void notifyUpdatedMarkers(Map<String, String> markerinfo)
	{
		for (UpdatedMarkerListener uml : updatedMarkerListeners)
		{
			uml.updatedMarkers(markerinfo);
		}
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
		float[] color = convertHexColorToFloatArray("#1D1D1D");
		glClearColor(color[0], color[1], color[2], 1f);
		colorProgram = new ColorShaderProgram(context);
		clipColorProgram = new ClipColorShaderProgram(context);

	}

	private float[] convertHexColorToFloatArray(String hexColor)
	{
		int parsedColor = Color.parseColor(hexColor);

		float r = Color.red(parsedColor) / 255f;
		float g = Color.green(parsedColor) / 255f;
		float b = Color.blue(parsedColor) / 255f;
		return new float[]
		{ r, g, b };
	}

	/**
	 * onSurfaceChanged is called whenever the surface has changed. This is
	 * called at least once when the surface is initialized. Keep in mind that
	 * Android normally restarts an Activity on rotation, and in that case, the
	 * renderer will be destroyed and a new one created.
	 * 
	 * @param width
	 *            The new width, in pixels.
	 * @param height
	 *            The new height, in pixels.
	 */
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height)
	{
		// Set the OpenGL viewport to fill the entire surface.
		glViewport(0, 0, width, height);
		this.height = height;
		this.width = width;
		float totalsamples = calculateraster(width, height);
		scope.totalSamples = (int) totalsamples;
		scope.totalDivisions = Math.round(dp.getDIVISIONS());
		orthoM(projectionMatrix, 0, 0f, totalsamples, 255f, 0f, -1, 1);
		invertM(invertedViewProjectionMatrix, 0, projectionMatrix, 0);
		samplesCh1 = new ChannelSamples(dp);
		samplesCh2 = new ChannelSamples(dp);
		samplesCh1.setScope(scope);
		samplesCh2.setScope(scope);
		listOfMarkers.clear();

		xMarker1 = new XMeasureMarker((int) (totalsamples / 4),
				(int) totalsamples);
		xMarker2 = new XMeasureMarker((int) ((totalsamples / 4) * 3),
				(int) totalsamples);
		yMarker1 = new YMeasureMarker(64, (int) totalsamples);
		yMarker2 = new YMeasureMarker(192, (int) totalsamples);
		xMarker1.Z = 1;
		xMarker2.Z = 2;
		yMarker1.Z = 3;
		yMarker1.SetColor(255, 255, 255, 255);
		yMarker2.Z = 4;
		yMarker2.SetColor(255, 255, 255, 255);
		yPosMarker1 = new YPositionMarker(120, (int) totalsamples);
		yPosMarker1.Z = 5;
		yPosMarker1.SetColor(44, 161, 45, 128);
		((YPositionMarker) yPosMarker1).setId(1);
		yPosMarker2 = new YPositionMarker(134, (int) totalsamples);
		yPosMarker2.Z = 5;
		yPosMarker2.SetColor(252, 235, 33, 128);
		((YPositionMarker) yPosMarker2).setId(2);
		yTriggerMarker = new YTriggerLevelMarker(145, (int) totalsamples);
		yTriggerMarker.Z = 6;
		yTriggerMarker.SetColor(0, 0, 255, 128);

		listOfMarkers.add(yTriggerMarker);
		listOfMarkers.add(yPosMarker1);
		listOfMarkers.add(yPosMarker2);
		listOfMarkers.add(xMarker1);
		listOfMarkers.add(yMarker1);
		listOfMarkers.add(xMarker2);
		listOfMarkers.add(yMarker2);
		float percentagetotal = (totalsamples / 4096) * 100;
		precentageview = (totalsamples / 100) * percentagetotal;

		scrollLine
				.setData(0, 254, 0.37f, 0.37f, 0.37f, 1f, (int) precentageview, 254, 0.37f, 0.37f, 0.37f, 1f);
	}

	long time1 = 0, time2;
	float currentfps = 0, previousfps = 0;

	/**
	 * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
	 * this is done at the refresh rate of the screen.
	 */
	@Override
	public void onDrawFrame(GL10 glUnused)
	{
		time1 = System.currentTimeMillis();
		// Clear the rendering surface.
		glClear(GL_COLOR_BUFFER_BIT);

		// Draw the table.
		/*
		 * stextureProgram.useProgram();
		 * textureProgram.setUniforms(projectionMatrix, texture);
		 * bg.bindData(textureProgram); bg.draw();
		 */

		glEnable(GL_BLEND);
		glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);

		colorProgram.useProgram();
		colorProgram.setUniforms(projectionMatrix);

		glLineWidth(1f);

		for (Line line : listOfLines)
		{
			line.bindData(colorProgram);
			line.draw();
		}

		if (enableMarkers)
		{
			for (Marker marker : listOfMarkers)
			{
				marker.bindData(colorProgram);
				marker.draw();
			}
		}

		glLineWidth(2f);

		scrollLine.bindData(colorProgram);
		scrollLine.draw();

		clipColorProgram.useProgram();
		clipColorProgram.setUniforms(projectionMatrix);
		glLineWidth(3f);

		if (scope.getChannel1().getVerticalDiv() != VoltageDiv.off)
		{
			samplesCh1.bindData(colorProgram);
			samplesCh1.draw();
		}

		if (scope.getChannel2().getVerticalDiv() != VoltageDiv.off)
		{
			samplesCh2.bindData(colorProgram);
			samplesCh2.draw();
		}
	}

	public void setScope(WFS210 osci)
	{
		scope = osci;
		if (samplesCh1 != null)
		{
			samplesCh1.setScope(osci);
			samplesCh1.setScope(osci);
		}

	}

	float calculateraster(int width, int height)
	{

		float pixelsperdiv = height / 10;

		float totalDivisions = width / pixelsperdiv;
		float totalXPoints = totalDivisions * Constants.SAMPLES_PER_DIVISION;
		dp.setDIVISIONS(totalDivisions);
		dp.setTOTAL_SAMPLES(totalXPoints);
		int space = 25;
		space = (int) Math.floor(space);
		Line line = new Line();
		float[] color = convertHexColorToFloatArray("#414141");
		line.setData(0, 128, color[0], color[1], color[2], 1f, (int) totalXPoints, 128, color[0], color[1], color[2], 1f);
		listOfLines.add(line);
		for (int i = 1; i < 5; i++)
		{
			line = new Line();
			line.setData(0, 128 + space * i, color[0], color[1], color[2], 1f, (int) totalXPoints, 128 + space * i, color[0], color[1], color[2], 1f);
			listOfLines.add(line);
		}
		for (int i = 1; i < 5; i++)
		{
			line = new Line();
			line.setData(0, 128 - space * i, color[0], color[1], color[2], 1f, (int) totalXPoints, 128 - space * i, color[0], color[1], color[2], 1f);
			listOfLines.add(line);
		}
		int space2 = (int) Math.floor(totalXPoints / totalDivisions);
		for (int i = 1; i < totalDivisions; i++)
		{
			line = new Line();
			line.setData(space2 * i, 0, color[0], color[1], color[2], 1f, space2 * i, (int) totalXPoints, color[0], color[1], color[2], 1f);
			listOfLines.add(line);
		}
		line = new Line();
		color = convertHexColorToFloatArray("#5F5F5F");
		line.setData(0, 253, color[0], color[1], color[2], 1f, (int) totalXPoints, 253, color[0], color[1], color[2], 1f);
		listOfLines.add(line);

		return totalXPoints;
	}

	public void handleTouchPress(float normalizedX, float normalizedY)
	{
		final float[] normalizedPoint =
		{ normalizedX, normalizedY, -1, 1 };
		final float[] ScopePoint = new float[4];
		int X, Y;

		multiplyMV(ScopePoint, 0, invertedViewProjectionMatrix, 0, normalizedPoint, 0);
		X = (int) ScopePoint[0];
		Y = (int) ScopePoint[1];
		Log.i("Renderer", Integer.toString(Y));
		previousX = X;
		for (Marker marker : listOfMarkers)
		{
			if (marker instanceof XMeasureMarker)
			{
				XMeasureMarker m = (XMeasureMarker) marker;
				if (Math.abs(m.getPosition().x - X) < 20)
				{
					marker.isTouched = true;
				}
			} else
			{

				if (marker instanceof YMeasureMarker)
				{
					YMeasureMarker m = (YMeasureMarker) marker;
					if (Math.abs(m.getPosition().y - Y) < 15)
					{
						if (!marker.isXMarker)
							marker.isTouched = true;

					}
				}
				if (marker instanceof YPositionMarker)
				{
					YPositionMarker m = (YPositionMarker) marker;
					if (Math.abs(m.getPosition().y - Y) < 15)
					{
						if (!marker.isXMarker)
							marker.isTouched = true;

					}
				}
				if (marker instanceof YTriggerLevelMarker)
				{
					YTriggerLevelMarker m = (YTriggerLevelMarker) marker;
					if (Math.abs(m.getPosition().y - Y) < 15)
					{
						if (!marker.isXMarker)
							marker.isTouched = true;

					}
				}

			}

		}
		List<Marker> listOfTouchedMarkers = new ArrayList<Marker>();

		for (Marker marker : listOfMarkers)
		{
			if (marker.isTouched)
			{
				listOfTouchedMarkers.add(marker);
			}
		}
		Marker m = getHighestMarker(listOfTouchedMarkers);
		if (m != null)
			m.setAlpha(255);
		//Log.i(TAG,"Touch "+Integer.toString(X)  + " " + Integer.toString(Y));
	}

	int previousX = 0;

	public void handleTouchDrag(float normalizedX, float normalizedY)
	{
		//Log.i(TAG,"Drag"+Float.toString(normalizedX));

		final float[] normalizedPoint =
		{ normalizedX, normalizedY, -1, 1 };
		final float[] ScopePoint = new float[4];
		int X, Y;

		multiplyMV(ScopePoint, 0, invertedViewProjectionMatrix, 0, normalizedPoint, 0);
		X = (int) ScopePoint[0];
		Y = (int) ScopePoint[1];

		List<Marker> listOfTouchedMarkers = new ArrayList<Marker>();

		for (Marker marker : listOfMarkers)
		{
			if (marker.isTouched)
			{
				listOfTouchedMarkers.add(marker);
			}
		}
		if (listOfTouchedMarkers.size() != 0)
		{
			Marker m = getHighestMarker(listOfTouchedMarkers);
			if (m instanceof XMeasureMarker)
			{
				((XMeasureMarker) m).setData(X);
			} else
			{
				m.setData(Y, -1);
			}

		} else
			if (scope.getTriggerSettings().getRun_Hold() || !scope.isFakeData || (scope
					.getTriggerSettings().getTrigger_Mode().ordinal() != TriggerMode.AUTO
					.ordinal()))
			{
				int dX = X - previousX;
				int buf = 0;
				scrollPos -= dX;
				buf = scrollPos;
				if (buf < 0)
				{
					buf = 0;
				}
				if (buf >= (4094 - dp.getTOTAL_SAMPLES()))
				{
					buf = (4094 - dp.getTOTAL_SAMPLES());
				}
				scrollPos = buf;
				float ratio = (float) ((float) scrollPos / 4096);
				float scrollratio = (float) (dp.getTOTAL_SAMPLES() * ratio);
				scrollLine
						.setData((int) scrollratio, 254, 0.37f, 0.37f, 0.37f, 1f, (int) (scrollratio + precentageview), 254, 0.37f, 0.37f, 0.37f, 1f);
				previousX = X;
				newFrame();
				//Log.i(TAG,Integer.toString(scrollPos));
			}
	}

	public void handleTouchUp(float normalizedX, float normalizedY)
	{
		//Log.i(TAG,"Drag"+Float.toString(normalizedX));

		final float[] normalizedPoint =
		{ normalizedX, normalizedY, -1, 1 };
		final float[] ScopePoint = new float[4];
		int X;

		multiplyMV(ScopePoint, 0, invertedViewProjectionMatrix, 0, normalizedPoint, 0);
		X = (int) ScopePoint[0];
		previousX = X;

		if (scope.connector.isConnected)
		{
			for (Marker marker : listOfMarkers)
			{
				if (marker.isTouched)
				{
					if (marker instanceof YTriggerLevelMarker)
					{

						Log.i("triggerlevel marker", Integer.toString(marker
								.getPosition().y));
						scope.getTriggerSettings().setTriggerLevel(marker
								.getPosition().y);
						scope.sendSettings();
						marker.setAlpha(128);
					}

					if (marker instanceof YPositionMarker)
					{
						if (((YPositionMarker) marker).getId() == 1)
						{
							scope.getChannel1().setVerticalPosition(marker
									.getPosition().y);
						} else
							scope.getChannel2().setVerticalPosition(marker
									.getPosition().y);
						scope.sendSettings();
						marker.setAlpha(128);
					}

					marker.isTouched = false;
				}
			}
		} else
		{

		}

	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public Marker getHighestMarker(List<Marker> markers)
	{

		Marker result = null;
		if (markers.size() != 0)
		{
			int HighestZ = 0;
			int HighestIndex = 0;
			int Index = 0;
			for (Marker marker : markers)
			{
				Index++;
				if (HighestZ < marker.Z)
				{
					HighestZ = marker.Z;
					HighestIndex = Index;
				}
			}
			if (markers.size() != 0)
			{
				if (HighestIndex != 0)
				{
					result = markers.get(HighestIndex - 1);
				} else
				{
					result = markers.get(HighestIndex);
				}
			}
		}
		return result;
	}
}

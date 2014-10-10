/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.velleman.wfs210;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import be.velleman.wfs210.Trigger.TriggerMode;

/**
 * A view container where OpenGL ES graphics can be drawn on screen. This view
 * can also be used to capture touch events, such as a user interacting with
 * drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView
{

	private final MyGLRenderer mRenderer;

	public MyGLRenderer getRenderer()
	{
		return this.mRenderer;
	}

	private static final String TAG = "MyGLSurfaceView";
	private ScaleGestureDetector mScaleDetector;
	private WFS210 scope;
	private Boolean isScaling = false;
	private Display display;
	private Point displayformat;

	public MyGLSurfaceView(Context context)
	{
		super(context);
		setEGLContextClientVersion(2);
		mRenderer = new MyGLRenderer(context);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		display = wm.getDefaultDisplay();
		displayformat = new Point();
		display.getSize(displayformat);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

	}

	public MyGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setEGLContextClientVersion(2);
		mRenderer = new MyGLRenderer(context);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		displayformat = new Point(mRenderer.getWidth(), mRenderer.getHeight());
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

	}

	public void setScope(WFS210 scope2)
	{
		this.scope = scope2;
		mRenderer.setScope(scope);
		

	}

	private Boolean isLeft = false;
	
	@SuppressWarnings("unused")
	private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			Log.i("Scroll", "scrolling");
			return true;
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event != null)
		{
			displayformat = new Point(mRenderer.getWidth(), mRenderer
					.getHeight());
			mScaleDetector.onTouchEvent(event);
			final float normalizedX = ((event.getX() / (float) this.getWidth()) * 2 - 1);
			final float normalizedY = -((event.getY() / (float) this
					.getHeight()) * 2 - 1);

			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				if (!isScaling)
				{
					queueEvent(new Runnable()
					{
						@Override
						public void run()
						{
							if (mRenderer.enableMarkers)
								mRenderer
										.handleTouchPress(normalizedX, normalizedY);
						}
					});
				} else
				{
					if (normalizedX > normalizedX / 2)
					{
						isLeft = true;
					}
				}
			} else
				if (event.getAction() == MotionEvent.ACTION_MOVE)
				{

					if (!isScaling)
					{
						//Log.i(TAG, "Move :" + Float.toString(normalizedX));
						queueEvent(new Runnable()
						{
							@Override
							public void run()
							{
								if (mRenderer.enableMarkers)
									mRenderer
											.handleTouchDrag(normalizedX, normalizedY);
							}
						});
					} else
					{
						if (normalizedX < 0)
						{
							isLeft = true;
						} else
							isLeft = false;
					}
				}
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				if (scope.connector.isConnected)
				{
					if (!isScaling)
					{
						queueEvent(new Runnable()
						{
							@Override
							public void run()
							{
								if (mRenderer.enableMarkers)
									mRenderer
											.handleTouchUp(normalizedX, normalizedY);
							}
						});
					} else
					{
						isScaling = false;
						startScale = false;
						scope.updateScaleData(false, false, isLeft);
						scope.triggerSettings.setAutorange(false);
						scope.sendSettings();
					}
				}
			}
			requestRender();
			return true;
		} else
		{
			requestRender();
			return false;
		}

	}

	public void setYPos1(int yPos)
	{
		mRenderer.yPosMarker1.setData(yPos, -1);
	}

	public void setYPos2(int yPos)
	{
		mRenderer.yPosMarker2.setData(yPos, -1);
	}

	public void setTriggerLevel(int yPos)
	{
		mRenderer.yTriggerMarker.setData(yPos, -1);
	}

	byte[] scopedata1 = new byte[4096], scopedata2 = new byte[4096];

	public void startUpdatingData()
	{
		Timer updater = new Timer("GL DATA");
		updater.scheduleAtFixedRate(new TimerTask()
		{

			@Override
			public void run()
			{
				while (scope == null)
					;
				while (scope.connector == null)
					;
				while (mRenderer.samplesCh1 == null)
					;
				while (mRenderer.samplesCh2 == null)
					;
				while (!(scope.connector.isConnected & scope.getHasSettings()))
					;
				if (scope.isNewData || scope.isFakeData || (scope
						.getTriggerSettings().getTrigger_Mode().ordinal() == TriggerMode.ONCE
						.ordinal()) || (scope.getTriggerSettings()
						.getTrigger_Mode().ordinal() == TriggerMode.NORMAL
						.ordinal()) || (scope.getTriggerSettings()
						.getRun_Hold()))
				{
					scope.getChannel1().isNewData = false;
					if (scope.getTimeBase().ordinal() == TimeBase.HDIV_1uS
							.ordinal())
					{
						System.arraycopy(scope.getChannel1().getSamples(), mRenderer.scrollPos, scopedata1, 0, (int) (mRenderer.dp
								.getTOTAL_SAMPLES() / 5) + 2);

					} else
						if (scope.getTimeBase().ordinal() == TimeBase.HDIV_2uS
								.ordinal())
						{
							System.arraycopy(scope.getChannel1().getSamples(), mRenderer.scrollPos, scopedata1, 0, (int) (mRenderer.dp
									.getTOTAL_SAMPLES() / 2.5f) + 2);
						} else
						{
							System.arraycopy(scope.getChannel1().getSamples(), mRenderer.scrollPos, scopedata1, 0, (int) mRenderer.dp
									.getTOTAL_SAMPLES() + 2);
						}
					mRenderer.samplesCh1.setData(scopedata1, 44, 161, 45);
				}

				if (scope.getChannel2().isNewData || scope.isFakeData || (scope
						.getTriggerSettings().getTrigger_Mode().ordinal() == TriggerMode.ONCE
						.ordinal()) || (scope.getTriggerSettings()
						.getTrigger_Mode().ordinal() == TriggerMode.NORMAL
						.ordinal()) || (scope.getTriggerSettings()
						.getRun_Hold()))
				{
					scope.getChannel2().isNewData = false;
					if (scope.getTimeBase().ordinal() == TimeBase.HDIV_1uS
							.ordinal())
					{
						System.arraycopy(scope.getChannel2().getSamples(), mRenderer.scrollPos, scopedata2, 0, (int) (mRenderer.dp
								.getTOTAL_SAMPLES() / 5) + 2);
					} else
						if (scope.getTimeBase().ordinal() == TimeBase.HDIV_2uS
								.ordinal())
						{
							System.arraycopy(scope.getChannel2().getSamples(), mRenderer.scrollPos, scopedata2, 0, (int) (mRenderer.dp
									.getTOTAL_SAMPLES() / 2.5f) + 2);
						} else
						{
							System.arraycopy(scope.getChannel2().getSamples(), mRenderer.scrollPos, scopedata2, 0, (int) (mRenderer.dp
									.getTOTAL_SAMPLES() + 2));
						}
					mRenderer.samplesCh2.setData(scopedata2, 237, 255, 33);
				}

			}

		}, 33, 33);

	}

	float mScaleFactor;
	private Boolean startScale = false;
	private Point startScaleData;
	private TimeBase timebase;
	private VoltageDiv voltageDiv;

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			isScaling = true;

			mScaleFactor = detector.getScaleFactor();
			if (scope.getChannel1().isFilled() && scope.getChannel2()
					.isFilled())
			{
				if (mScaleFactor > 1)
				{ // ZOOM OUT

					if (!startScale)
					{
						startScale = true;
						startScaleData = new Point((int) detector
								.getCurrentSpanX(), (int) detector
								.getCurrentSpanY());
						timebase = scope.getTimeBase();
						voltageDiv = scope.selectedChannel.getVerticalDiv();
					} else
					{
						double hoek = (Math
								.atan2(detector.getCurrentSpanY(), detector
										.getCurrentSpanX()));

						hoek = Math.toDegrees(hoek);
						if (hoek > 45) //is YScaling
						{
							if (detector.getCurrentSpanY() > (startScaleData.y + (displayformat.y / 15)))
							{
								int voltageDivSetting = scope.selectedChannel
										.getVerticalDiv().ordinal() + 1;
								if (voltageDivSetting < 12)
								{
									voltageDiv = VoltageDiv
											.fromOrdinal(voltageDivSetting);
									//Log.i(TAG, "voltageDiv scale:" + voltageDiv.ordinal());
									scope.selectedChannel
											.setVerticalDiv(voltageDiv);
									scope.updateScaleData(true, false, isLeft);
									startScaleData.y += (displayformat.y / 15);
								} else
									if (voltageDivSetting == 12)
									{
										voltageDiv = VoltageDiv
												.fromOrdinal(voltageDivSetting);
										//Log.i(TAG, "voltageDiv scale:" + voltageDiv.ordinal());
										scope.selectedChannel
												.setVerticalDiv(voltageDiv);
										scope.updateScaleData(true, false, isLeft);
										startScaleData.y += (displayformat.y / 15);
									}
							}
						} else
						//is XScaling
						{
							if (detector.getCurrentSpanX() > (startScaleData.x + (displayformat.x / 22)))
							{
								int timeBaseSetting = timebase.ordinal() - 1;
								if (timeBaseSetting >= 0)
								{
									timebase = TimeBase.values()[timebase
											.ordinal() - 1];
									//Log.i(TAG, "TimeBase scale:" + timebase.ordinal());
									scope.setTimeBase(timebase);
									scope.updateScaleData(true, true, isLeft);
									startScaleData.x += (displayformat.x / 22);
								}
							}
						}

					}
					//Log.i(TAG, "Scale : " + detector.getCurrentSpanY());
				} else
				{ //ZOOM IN

					if (!startScale)
					{
						startScale = true;
						startScaleData = new Point((int) detector
								.getCurrentSpanX(), (int) detector
								.getCurrentSpanY());
						timebase = scope.getTimeBase();
						voltageDiv = scope.selectedChannel.getVerticalDiv();
					} else
					{
						double hoek = (Math
								.atan2(detector.getCurrentSpanY(), detector
										.getCurrentSpanX()));

						hoek = Math.toDegrees(hoek);

						if (hoek > 45) //is YScaling
						{
							if (detector.getCurrentSpanY() < (startScaleData.y - (displayformat.y / 15)))
							{
								int voltageDivSetting = scope.selectedChannel
										.getVerticalDiv().ordinal() - 1;
								if (voltageDivSetting >= 0)
								{
									voltageDiv = VoltageDiv
											.fromOrdinal(voltageDivSetting);
									//Log.i(TAG, "voltageDiv scale:" + voltageDiv.ordinal());
									scope.selectedChannel
											.setVerticalDiv(voltageDiv);
									scope.updateScaleData(true, false, isLeft);
									startScaleData.y -= (displayformat.y / 15);
								}
							}
						} else
						// is XScaling
						{

							if (detector.getCurrentSpanX() < (startScaleData.x - (displayformat.x / 22)))
							{
								int timeBaseSetting = timebase.ordinal() + 1;
								if (timeBaseSetting < 18)
								{
									timebase = TimeBase.values()[timebase
											.ordinal() + 1];
									//Log.i(TAG, "Timebase scale:" + timebase.ordinal());
									scope.setTimeBase(timebase);
									scope.updateScaleData(true, true, isLeft);
									startScaleData.x -= (displayformat.x / 22);
								} else
									if (timeBaseSetting == 18)
									{
										timebase = TimeBase.values()[timeBaseSetting];
										//Log.i(TAG, "Timebase scale:" + timebase.ordinal());
										scope.setTimeBase(timebase);
										scope.updateScaleData(true, true, isLeft);
									}

							}

						}

					}
					//Log.i(TAG, "Scale : " + detector.getCurrentSpanY());
				}

				// Don't let the object get too small or too large.
				mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
			} else
			{
				//TODO zoomfunction ?
			}
			//invalidate();

			return true;
		}
		
		
	}

}

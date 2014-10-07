package be.velleman.wfs210;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.velleman.wfs210.Channel.InputCoupling;
import be.velleman.wfs210.Trigger.TriggerChannel;
import be.velleman.wfs210.Trigger.TriggerMode;
import be.velleman.wfs210.Trigger.TriggerSlope;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScopeActivity extends Activity implements OnItemSelectedListener,
		ConnectionListener, ScopeDataChangedListener, UpdatedMarkerListener,
		WFS210MeasurementsListener
{
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		//scope.updateSettings();
	}

	Context context;
	Intent intent;
	List<ScanResult> wifilist;
	TCPConnector connector = null;
	WFS210 scope;
	MyGLSurfaceView mGLView;
	OsciCalculator calculator;

	WifiManager mainWifi;
	//Channel1
	TextView MarkerMeasurement1, Measurement1, VoltageMarker1, VoltageDiv1;
	Button channelName1, AC1, DC1, GND1, Probe1, voltUp1, voltDown1;
	Spinner Channel1MeasurementSpinner, Channel1MarkerMeasurementSpinner;
	//Channel2
	TextView MarkerMeasurement2, Measurement2, VoltageMarker2, VoltageDiv2;
	Button channelName2, AC2, DC2, GND2, Probe2, voltUp2, voltDown2;
	Spinner Channel2MeasurementSpinner, Channel2MarkerMeasurementSpinner;
	//Trigger
	TextView trigger;
	Button triggerCH1, triggerCH2, triggerNormal, triggerRun, triggerOnce,
			triggerHold, triggerRising, triggerFalling;
	//TimeMeasurement
	TextView time, frequency;
	//Miscellaneous
	TextView timeBase;
	Button autoRange, btnSettings, timeBaseLeft, timeBaseRight;
	//SurfaceView views
	TextView txtVoltTimeSetting, txtCalibrating;
	String currentWifiName = "", currentWifiChannel = "",
			currentWifiPassword = "";
	AnimationDrawable batteryAnimation;
	Map<String, String> markersInfo;
	WFS210SettingsReminder reminder;
	int SelectedItemChannel1 = 0;
	int SelectedItemChannel2 = 0;
	int SelectedMarkerItemChannel1 = 0;
	int SelectedMarkerItemChannel2 = 0;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	private boolean isDemo;
	private static final String TAG = "Activity";

	@Override
	protected void onPause()
	{

		super.onPause();
		if (mGLView != null)
			mGLView.onPause();

		try
		{
			clearApplicationData();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	protected void onResume()
	{

		super.onResume();
		if (mGLView != null)
			mGLView.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filter.addAction("android.net.wifi.STATE_CHANGE");

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		if (sp.getBoolean("STARTCALIBRATING", false))
		{
			scope.requestCalibrate();

			editor.putBoolean("STARTCALIBRATING", false);
			editor.commit();

		}
		Boolean isChanged = false;
		if (currentWifiName != sp.getString("WIFINAME", "unknown"))
		{
			isChanged = true;
			currentWifiName = sp.getString("WIFINAME", "unknown");
		}
		if (currentWifiChannel != sp.getString("WIFICHANNEL", "unknown"))
		{
			isChanged = true;
			currentWifiChannel = sp.getString("WIFICHANNEL", "unknown");
		}
		if (currentWifiPassword != sp.getString("WIFIPASSWORD", "unknown"))
		{
			isChanged = true;
			currentWifiPassword = sp.getString("WIFIPASSWORD", "unknown");
		}
		if (isChanged)
			scope.sendWifiSettings(currentWifiName, currentWifiChannel);
		if (sp.getBoolean("DEMO", false))
		{
			if (scope instanceof RealWFS210)
			{ //Demo check if current scope is a Real One if yes change to Fake one.
				isDemo = true;
				connector.clearAllConnectionListeners();
				connector.close();
				connector = null;
				fakeConnector = new FakeConnector();
				scope = null;
				scope = new FakeWFS210(fakeConnector);
				scope.selectedChannel = scope.getChannel1();
				mGLView.setScope(scope);
				fakeConnector.open();
				scope.addScopeDataChangedListener(this);
				scope.generateFakeSignals();
				scope.updateSettings();
				calculator.clearListners(this);
				calculator.stopCalculating();
				calculator.setScope(scope);
				calculator.startCalculating();
				calculator.addMeasurementsListener(this);
			}
		} else
		{
			if (scope instanceof FakeWFS210)
			{
				connector = new TCPConnector("169.254.1.1", 2000);
				connector.addConnectionListener(this);
				scope = new RealWFS210(connector);
				scope.selectedChannel = scope.getChannel1();
				mGLView.setScope(scope);
				reminder = new WFS210SettingsReminder(getApplicationContext());
				scope.addScopeDataChangedListener(this);
				scope.addScopeDataChangedListener(reminder);
				scope.generateFakeSignals();

				isDemo = false;
				connector.open();
				calculator.clearListners(this);
				calculator.stopCalculating();
				calculator.setScope(scope);
				calculator.startCalculating();
				calculator.addMeasurementsListener(this);
			}
		}

	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		try
		{
			if (connector != null)
				connector.close();
			clearApplicationData();

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setViewSelected(View v)
	{
		if (v instanceof Button)
		{
			Button b = (Button) v;
			b.setTextAppearance(this, R.style.SelectedStyle);
		}
	}

	public void setViewBackground(View v, int StyleId)
	{
		if (v instanceof Button)
		{
			final int paddingBottom = v.getPaddingBottom(), paddingLeft = v
					.getPaddingLeft();
			final int paddingRight = v.getPaddingRight(), paddingTop = v
					.getPaddingTop();

			v.setBackgroundResource(StyleId);
			v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		}
	}

	static Handler mHandler;
	Connector fakeConnector;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = this;
		
		txtVoltTimeSetting = new TextView(this);
		txtVoltTimeSetting.setVisibility(View.INVISIBLE);
		txtCalibrating = new TextView(this);
		txtCalibrating.setVisibility(View.INVISIBLE);
		markersInfo = new HashMap<String, String>();
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (sp.getBoolean("DEMO", false))
		{
			fakeConnector = new FakeConnector();
			scope = new FakeWFS210(fakeConnector);
			scope.selectedChannel = scope.getChannel1();
			isDemo = true;
		} else
		{
			connector = new TCPConnector("169.254.1.1", 2000);

			scope = new RealWFS210(connector);
			scope.selectedChannel = scope.getChannel1();
			isDemo = false;

		}
		reminder = new WFS210SettingsReminder(getApplicationContext());
		setContentView(R.layout.activity_scope);
		mGLView = (MyGLSurfaceView) findViewById(R.id.coolview);
		mGLView.setZOrderMediaOverlay(true);
		mGLView.setScope(scope);
		calculator = new OsciCalculator(scope, mGLView.getRenderer());

		RelativeLayout rl = (RelativeLayout) findViewById(R.id.helperrelativelayout);

		txtVoltTimeSetting.setId(50);
		txtVoltTimeSetting.setBackgroundResource(R.drawable.toettoet);
		txtVoltTimeSetting.setTextAppearance(this, R.style.LargeText);
		txtVoltTimeSetting.setText("TOETTOET");
		txtVoltTimeSetting.setGravity(Gravity.CENTER);

		txtCalibrating.setId(50);
		txtCalibrating.setBackgroundResource(R.drawable.backgroundcalibrating);
		txtCalibrating.setTextAppearance(this, R.style.LargeText);
		txtCalibrating.setText("Calibrating...");
		txtCalibrating.setGravity(Gravity.CENTER);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		lp.topMargin = 50;
		lp.bottomMargin = 50;
		txtCalibrating.setLayoutParams(lp);
		txtCalibrating.setVisibility(View.INVISIBLE);
		rl.addView(txtVoltTimeSetting);
		rl.addView(txtCalibrating);

		// Initiate wifi service manager
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!mainWifi.isWifiEnabled())
		{
			showToast("Wifi is not enabled, it will be enabled NOW");
			mainWifi.setWifiEnabled(true);
			mainWifi.startScan();
		}

		//Channel1
		channelName1 = (Button) findViewById(R.id.btnCH1);
		channelName1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (scope.selectedChannel != scope.getChannel1())
				{
					scope.selectedChannel = scope.getChannel1();
					setViewBackground(channelName1, R.drawable.buttongreen);
					setViewBackground(channelName2, R.drawable.button);
				}
			}
		});
		AC1 = (Button) findViewById(R.id.btnAC1);
		AC1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getChannel1().setInputCoupling(InputCoupling.AC);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});
		DC1 = (Button) findViewById(R.id.btnDC1);
		DC1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getChannel1().setInputCoupling(InputCoupling.DC);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});
		GND1 = (Button) findViewById(R.id.btnGND1);
		GND1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getChannel1().setInputCoupling(InputCoupling.GND);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});
		Probe1 = (Button) findViewById(R.id.btnPROBE1);
		Probe1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (scope.getChannel1().getIsX10())
				{
					scope.getChannel1().setIsX10(false);
					setViewBackground(Probe1, R.drawable.button);
				} else
				{
					scope.getChannel1().setIsX10(true);
					setViewBackground(Probe1, R.drawable.buttongreen);
				}
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		Measurement1 = (TextView) findViewById(R.id.txtMeasurement1);
		MarkerMeasurement1 = (TextView) findViewById(R.id.txtMarkerMeasurement1);

		//Channel2
		channelName2 = (Button) findViewById(R.id.btnCH2);
		channelName2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (scope.selectedChannel != scope.getChannel2())
				{
					scope.selectedChannel = scope.getChannel2();
					setViewBackground(channelName2, R.drawable.buttonyellow);
					setViewBackground(channelName1, R.drawable.button);
				}
			}
		});
		AC2 = (Button) findViewById(R.id.btnAC2);
		AC2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getChannel2().setInputCoupling(InputCoupling.AC);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});
		DC2 = (Button) findViewById(R.id.btnDC2);
		DC2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getChannel2().setInputCoupling(InputCoupling.DC);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}

			}
		});
		GND2 = (Button) findViewById(R.id.btnGND2);
		GND2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getChannel2().setInputCoupling(InputCoupling.GND);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});
		Probe2 = (Button) findViewById(R.id.btnPROBE2);
		Probe2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (scope.getChannel2().getIsX10())
				{
					scope.getChannel2().setIsX10(false);
					setViewBackground(Probe2, R.drawable.button);
				} else
				{
					scope.getChannel2().setIsX10(true);
					setViewBackground(Probe2, R.drawable.buttonyellow);
				}
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}

			}
		});

		Measurement2 = (TextView) findViewById(R.id.txtMeasurement2);
		MarkerMeasurement2 = (TextView) findViewById(R.id.txtMarkerMeasurement2);
		//Trigger
		triggerCH1 = (Button) findViewById(R.id.btnTriggerCh1);
		triggerCH1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings()
						.setTrigger_Channel(TriggerChannel.CH1);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerCH2 = (Button) findViewById(R.id.btnTriggerCh2);
		triggerCH2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings()
						.setTrigger_Channel(TriggerChannel.CH2);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerNormal = (Button) findViewById(R.id.btnNormal);
		triggerNormal.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings().setTrigger_Mode(TriggerMode.NORMAL);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerRun = (Button) findViewById(R.id.btnRun);
		triggerRun.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings().setTrigger_Mode(TriggerMode.AUTO);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerOnce = (Button) findViewById(R.id.btnOnce);
		triggerOnce.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings().setTrigger_Mode(TriggerMode.ONCE);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerHold = (Button) findViewById(R.id.btnHold);
		triggerHold.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (scope.getTriggerSettings().getRun_Hold())
				{
					scope.getTriggerSettings().setRun_Hold(false);
				} else
				{
					scope.getTriggerSettings().setRun_Hold(true);
				}
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerFalling = (Button) findViewById(R.id.btnFalling);
		triggerFalling.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings()
						.setTrigger_Slope(TriggerSlope.FALLING_EDGE);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		triggerRising = (Button) findViewById(R.id.btnRising);
		triggerRising.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				scope.getTriggerSettings()
						.setTrigger_Slope(TriggerSlope.RISING_EDGE);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		btnSettings = (Button) findViewById(R.id.btnSettings);
		btnSettings.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				startSettings();
			}
		});

		voltDown1 = (Button) findViewById(R.id.btnVoltDown1);
		voltDown1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if ((scope.getChannel1().getVerticalDiv().ordinal() - 1) >= 0)
				{
					scope.getChannel1().setVerticalDiv(VoltageDiv
							.fromOrdinal(scope.getChannel1().getVerticalDiv()
									.ordinal() - 1));
					if (!scope.sendSettings())
					{
						alertUser("Could not send command");
					}
				}

			}
		});

		voltUp1 = (Button) findViewById(R.id.btnVoltUp1);
		voltUp1.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if ((scope.getChannel1().getVerticalDiv().ordinal() + 1) < VoltageDiv
						.values().length)
				{
					scope.getChannel1().setVerticalDiv(VoltageDiv
							.fromOrdinal(scope.getChannel1().getVerticalDiv()
									.ordinal() + 1));
					if (!scope.sendSettings())
					{
						alertUser("Could not send command");
					}
				}

			}
		});

		voltDown2 = (Button) findViewById(R.id.btnVoltDown2);
		voltDown2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				try
				{
					if ((scope.getChannel2().getVerticalDiv().ordinal() - 1) >= 0)
					{
						scope.getChannel2().setVerticalDiv(VoltageDiv
								.fromOrdinal(scope.getChannel2()
										.getVerticalDiv().ordinal() - 1));
						if (!scope.sendSettings())
						{
							alertUser("Could not send command");
						}
					}
				} catch (Exception e)
				{
					alertUser("Could not send command");
				}

			}
		});

		voltUp2 = (Button) findViewById(R.id.btnVoltUp2);
		voltUp2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				try
				{
					if ((scope.getChannel2().getVerticalDiv().ordinal() + 1) < VoltageDiv
							.values().length)
					{
						scope.getChannel2().setVerticalDiv(VoltageDiv
								.fromOrdinal(scope.getChannel2()
										.getVerticalDiv().ordinal() + 1));
						if (!scope.sendSettings())
						{
							alertUser("Could not send command");
						}
					}
				} catch (Exception e)
				{
					alertUser("Could not send command");
				}

			}
		});

		timeBase = (TextView) findViewById(R.id.txtTimebase);

		timeBaseLeft = (Button) findViewById(R.id.btnTimeBaseLeft);
		timeBaseLeft.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				try
				{
					if ((scope.getTimeBase().ordinal() + 1) < TimeBase.values().length)
					{
						scope.setTimeBase(TimeBase.fromOrdinal(scope
								.getTimeBase().ordinal() + 1));
						if (!scope.sendSettings())
						{
							alertUser("Could not send command");
						}
					}
				} catch (Exception e)
				{
					alertUser("Could not send command");
				}

			}
		});

		timeBaseRight = (Button) findViewById(R.id.btnTimeBaseRight);
		timeBaseRight.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				try
				{
					if ((scope.getTimeBase().ordinal() - 1) >= 0)
					{
						scope.setTimeBase(TimeBase.fromOrdinal(scope
								.getTimeBase().ordinal() - 1));
						if (!scope.sendSettings())
						{
							alertUser("Could not send command");
						}
					}
				} catch (Exception e)
				{
					alertUser("Could not send command");
				}

			}
		});

		autoRange = (Button) findViewById(R.id.btnAutoRange);
		autoRange.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (!scope.triggerSettings.getAutorange())
					scope.triggerSettings.setAutorange(true);
				else
					scope.triggerSettings.setAutorange(false);
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
			}
		});

		VoltageDiv1 = (TextView) findViewById(R.id.txtVDIV1);
		VoltageDiv2 = (TextView) findViewById(R.id.txtVDIV2);
		trigger = (TextView) findViewById(R.id.txtTrigger);

		Channel1MeasurementSpinner = (Spinner) findViewById(R.id.spMeasurement1);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.Measurements, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(R.layout.spinner_item);
		// Apply the adapter to the spinner
		Channel1MeasurementSpinner.setAdapter(adapter);

		Channel1MeasurementSpinner.setOnItemSelectedListener(this);

		Channel1MarkerMeasurementSpinner = (Spinner) findViewById(R.id.spMarkerMeasurement1);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> markerAdapter = ArrayAdapter
				.createFromResource(this, R.array.MarkerMeasurements, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		markerAdapter.setDropDownViewResource(R.layout.spinner_item);
		// Apply the adapter to the spinner
		Channel1MarkerMeasurementSpinner.setAdapter(markerAdapter);

		Channel1MarkerMeasurementSpinner.setOnItemSelectedListener(this);

		Channel2MeasurementSpinner = (Spinner) findViewById(R.id.spMeasurement2);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter
				.createFromResource(this, R.array.Measurements, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter2.setDropDownViewResource(R.layout.spinner_item);
		// Apply the adapter to the spinner
		Channel2MeasurementSpinner.setAdapter(adapter2);
		Channel2MeasurementSpinner.setOnItemSelectedListener(this);

		Channel2MarkerMeasurementSpinner = (Spinner) findViewById(R.id.spMarkerMeasurement2);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> markerAdapter2 = ArrayAdapter
				.createFromResource(this, R.array.MarkerMeasurements, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		markerAdapter2.setDropDownViewResource(R.layout.spinner_item);
		// Apply the adapter to the spinner
		Channel2MarkerMeasurementSpinner.setAdapter(markerAdapter2);
		Channel2MarkerMeasurementSpinner.setOnItemSelectedListener(this);

		Button fake = (Button) findViewById(R.id.fakebutton1);
		fake.setBackgroundColor(Color.TRANSPARENT);
		fake.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Channel1MeasurementSpinner.performClick();
			}
		});

		Button fakeMarker = (Button) findViewById(R.id.fakeMarkerButton1);
		fakeMarker.setBackgroundColor(Color.TRANSPARENT);
		fakeMarker.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Channel1MarkerMeasurementSpinner.performClick();
			}
		});

		Button fake2 = (Button) findViewById(R.id.fakebutton2);
		fake2.setBackgroundColor(Color.TRANSPARENT);
		fake2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Boolean b = Channel2MeasurementSpinner.performClick();
				String s = b ? "TRUE" : "False";
				Log.i(TAG, "Clicked:" + s);
			}
		});

		Button fakeMarker2 = (Button) findViewById(R.id.fakeMarkerButton2);
		fakeMarker2.setBackgroundColor(Color.TRANSPARENT);
		fakeMarker2.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Channel2MarkerMeasurementSpinner.performClick();
			}
		});

		sp = PreferenceManager.getDefaultSharedPreferences(context);
		currentWifiName = sp.getString("WIFINAME", "unknown");
		currentWifiChannel = sp.getString("WIFICHANNEL", "unknown");
		currentWifiPassword = sp.getString("WIFIPASSWORD", "unknown");
		scope.addScopeDataChangedListener(this);
		scope.addScopeDataChangedListener(reminder);
		if (isDemo)
		{
			if (fakeConnector != null)
				fakeConnector.addConnectionListener(this);
			fakeConnector.open();
			scope.generateFakeSignals();
			mGLView.setScope(scope);
			mGLView.startUpdatingData();
			calculator.startCalculating();
		} else
		{
			if (connector != null)
				connector.addConnectionListener(this);
			connector.open();
			mGLView.setScope(scope);
			mGLView.startUpdatingData();
			calculator.startCalculating();
		}

		mGLView.getRenderer().addUpdatedMarkersListener(this);
		calculator.addMeasurementsListener(this);
	}

	private void startSettings()
	{
		//getFragmentManager().beginTransaction()
		//      .replace(R.id.rlActivity, new SettingsFragment())
		//    .commit();

		startActivity(new Intent(this, SettingsActivity.class));
	}

	public boolean isOnline()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		cm.getActiveNetworkInfo();
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo()
				.isConnectedOrConnecting())
		{
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3)
	{
		if (arg0.equals(Channel1MarkerMeasurementSpinner) || arg0
				.equals(Channel2MarkerMeasurementSpinner))
		{
			if (arg2 == Channel1MarkerMeasurementSpinner.getAdapter()
					.getCount() - 1)
			{
				mGLView.getRenderer().enableMarkers = !mGLView.getRenderer().enableMarkers;
				if (arg0.equals(Channel2MarkerMeasurementSpinner))
				{
					Channel2MarkerMeasurementSpinner
							.setSelection(SelectedMarkerItemChannel2);
				} else
				{
					Channel1MarkerMeasurementSpinner
							.setSelection(SelectedMarkerItemChannel1);
				}
			} else
			{
				if (arg0.equals(Channel2MarkerMeasurementSpinner))
				{
					SelectedMarkerItemChannel2 = arg2;

				} else
				{
					SelectedMarkerItemChannel1 = arg2;
				}
			}
		} else
		{
			if (arg0.equals(Channel2MeasurementSpinner))
			{
				SelectedItemChannel2 = arg2;

			} else
			{
				SelectedItemChannel1 = arg2;
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
		// TODO Auto-generated method stub
		int i = 1;
		i = 2;

	}

	static Boolean isRunning = false;

	public void clearApplicationData()
	{
		File cache = getCacheDir();
		File appDir = new File(cache.getParent());
		if (appDir.exists())
		{
			String[] children = appDir.list();
			for (String s : children)
			{
				if (s.contentEquals("cache"))
				{

					deleteDir(new File(appDir, s));
					Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");

				}
			}
		}
	}

	public static boolean deleteDir(File dir)
	{
		if (dir != null && dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}
		return dir.delete();
	}

	private void showToast(final String msg)
	{
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				//Your code here
				Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
			}
		});

	}

	private void unSelect(ViewGroup vg)
	{
		int childCount = vg.getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			View v = vg.getChildAt(i);
			if (v instanceof ViewGroup)
			{
				unSelect((ViewGroup) v);
			}
			if (v instanceof Button)
			{

				final int paddingBottom = v.getPaddingBottom(), paddingLeft = v
						.getPaddingLeft();
				final int paddingRight = v.getPaddingRight(), paddingTop = v
						.getPaddingTop();

				v.setBackgroundResource(R.drawable.button);
				v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
			}
		}
	}

	@Override
	public void disconnected()
	{
		// TODO Auto-generated method stub
		showToast("Disconnected");
		if (connector.isConnected)
			connector.close();
		disableButtons();

	}

	private void disableViews(ViewGroup vg)
	{
		int childCount = vg.getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			View v = vg.getChildAt(i);
			if (v instanceof ViewGroup)
			{
				disableViews((ViewGroup) v);
			}
			if (v instanceof Button)
			{

				final int paddingBottom = v.getPaddingBottom(), paddingLeft = v
						.getPaddingLeft();
				final int paddingRight = v.getPaddingRight(), paddingTop = v
						.getPaddingTop();

				v.setBackgroundResource(R.drawable.button);
				if (v.getId() == R.id.btnVoltUp1 || v.getId() == R.id.btnVoltUp2)
				{
					v.setBackgroundResource(R.drawable.voltupdisabled);
				}
				if (v.getId() == R.id.btnVoltDown1 || v.getId() == R.id.btnVoltDown2)
				{
					v.setBackgroundResource(R.drawable.voltdowndisabled);
				}
				if (v.getId() == R.id.btnRising)
				{
					v.setBackgroundResource(R.drawable.slopeupunselected);
				}
				if (v.getId() == R.id.btnFalling)
				{
					v.setBackgroundResource(R.drawable.slopedownunselected);
				}
				if (v.getId() == R.id.btnTimeBaseLeft)
				{
					v.setBackgroundResource(R.drawable.timebaseleftdisabled);
				}
				if (v.getId() == R.id.btnTimeBaseRight)
				{
					v.setBackgroundResource(R.drawable.timebaserightdisabled);
				}
				if (v.getId() == R.id.fakebutton1)
				{
					v.setBackgroundResource(R.color.transparent);
				}
				if (v.getId() == R.id.fakebutton2)
				{
					v.setBackgroundResource(R.color.transparent);
				}
				if (v.getId() == R.id.fakebutton1)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				if (v.getId() == R.id.fakebutton2)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				if (v.getId() == R.id.fakeMarkerButton1)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				if (v.getId() == R.id.fakeMarkerButton2)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				v.setClickable(false);
			}
		}
	}

	private void enableViews(ViewGroup vg)
	{
		int childCount = vg.getChildCount();
		for (int i = 0; i < childCount; i++)
		{
			View v = vg.getChildAt(i);
			if (v instanceof ViewGroup)
			{
				enableViews((ViewGroup) v);
			}
			if (v instanceof Button)
			{

				final int paddingBottom = v.getPaddingBottom(), paddingLeft = v
						.getPaddingLeft();
				final int paddingRight = v.getPaddingRight(), paddingTop = v
						.getPaddingTop();

				v.setBackgroundResource(R.drawable.button);
				if (v.getId() == R.id.btnVoltUp1)
				{
					v.setBackgroundResource(R.drawable.voltup1);
				}
				if (v.getId() == R.id.btnVoltUp2)
				{
					v.setBackgroundResource(R.drawable.voltup2);
				}
				if (v.getId() == R.id.btnVoltDown1)
				{
					v.setBackgroundResource(R.drawable.voltdown1);
				}
				if (v.getId() == R.id.btnVoltDown2)
				{
					v.setBackgroundResource(R.drawable.voltdown2);
				}
				if (v.getId() == R.id.btnFalling)
				{
					v.setBackgroundResource(R.drawable.slopedownunselected);
				}
				if (v.getId() == R.id.btnRising)
				{
					v.setBackgroundResource(R.drawable.slopeupunselected);
				}
				if (v.getId() == R.id.btnTimeBaseLeft)
				{
					v.setBackgroundResource(R.drawable.timebaseleft);
				}
				if (v.getId() == R.id.btnTimeBaseRight)
				{
					v.setBackgroundResource(R.drawable.timebaseright);
				}
				if (v.getId() == R.id.btnAutoRange)
				{
					if (scope.getTriggerSettings().getAutorange())
						v.setBackgroundResource(R.drawable.buttonred);
					else
						v.setBackgroundResource(R.drawable.button);
				}

				if (v.getId() == R.id.fakebutton1)
				{
					v.setBackgroundResource(R.color.transparent);
				}
				if (v.getId() == R.id.fakebutton2)
				{
					v.setBackgroundResource(R.color.transparent);
				}

				if (v.getId() == R.id.fakebutton1)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}
				if (v.getId() == R.id.fakebutton2)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}

				if (v.getId() == R.id.fakeMarkerButton1)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}

				if (v.getId() == R.id.fakeMarkerButton2)
				{
					v.setBackgroundColor(Color.TRANSPARENT);
				}

				v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				v.setClickable(true);
			}
		}
	}

	private void disableButtons()
	{
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				LinearLayout ll1 = (LinearLayout) findViewById(R.id.llCH1);
				LinearLayout ll2 = (LinearLayout) findViewById(R.id.llCH2);
				RelativeLayout rl1 = (RelativeLayout) findViewById(R.id.rltriggerlayout);
				disableViews(ll1);
				disableViews(ll2);
				disableViews(rl1);
			}
		});
	}

	@Override
	public void connected()
	{

		
		if (!isDemo)
		{
			if (!connector.isReceiving)
			{
				connector.startReceivingPackets();
				calculator.startCalculating();

			}
			if (reminder.hasSettings())
			{
				restoreSettings(scope);
				scope.updateSettings();
				if (!scope.sendSettings())
				{
					alertUser("Could not send command");
				}
				scope.requestSettings();

			} else
			{
				scope.requestSettings();
			}

			scope.requestWifiSettings();
			enableButtons();
			showToast("Connected");
		} else
		{
			showToast("Demo Mode");
			enableButtons();
			scope.updateSettings();
			calculator.startCalculating();
		}
	}

	private void restoreSettings(WFS210 newScope)
	{
		newScope.setSettingByMap(reminder.getWFS210Settings());
	}

	private void enableButtons()
	{
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				LinearLayout ll1 = (LinearLayout) findViewById(R.id.llCH1);
				LinearLayout ll2 = (LinearLayout) findViewById(R.id.llCH2);
				RelativeLayout rl1 = (RelativeLayout) findViewById(R.id.rltriggerlayout);
				enableViews(ll1);
				enableViews(ll2);
				enableViews(rl1);
			}
		});
	}

	@Override
	public void newPacketFound(Packet p)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updatedMeasurements()
	{

		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				switch (SelectedMarkerItemChannel1)
				{
				case 0:
					MarkerMeasurement1
							.setText("     Δt\n" + calculator.getdt());
					break;
				case 1:
					MarkerMeasurement1.setText("    1/Δt\n" + calculator
							.getFreq());
					break;
				case 2:
					if (calculator.getdV1().contains("mV"))
						MarkerMeasurement1.setText("     ΔV1\n" + calculator
								.getdV1());
					else
						MarkerMeasurement1.setText("   ΔV1\n" + calculator
								.getdV1());
					break;
				case 3:
					if (calculator.getdV2().contains("mV"))
						MarkerMeasurement1.setText("     ΔV2\n" + calculator
								.getdV2());
					else
						MarkerMeasurement1.setText("   ΔV2\n" + calculator
								.getdV2());
					break;
				default:
					break;
				}
				switch (SelectedItemChannel1)
				{
				case 0:
					Measurement1.setText("Vdc1:\n" + calculator.getVdc1());
					break;
				case 1:
					Measurement1.setText("RMS1:\n" + calculator.getRMS1());
					break;
				case 2:
					Measurement1.setText("Trms1:\n" + calculator.getTRMS1());
					break;
				case 3:
					Measurement1.setText("Vptp1:\n" + calculator.getVpkpk1());
					break;
				case 4:
					Measurement1.setText("VMAX1:\n" + calculator.getVmax1());
					break;
				case 5:
					Measurement1.setText("VMIN1:\n" + calculator.getVmin1());
					break;
				case 6:
					Measurement1.setText("RMS1 2W:\n" + calculator.getW1rms2());
					break;
				case 7:
					Measurement1.setText("RMS1 4W:\n" + calculator.getW1rms4());
					break;
				case 8:
					Measurement1.setText("RMS1 8W:\n" + calculator.getW1rms8());
					break;
				case 9:
					Measurement1
							.setText("RMS1 16W:\n" + calculator.getW1rms16());
					break;
				case 10:
					Measurement1
							.setText("RMS1 32W:\n" + calculator.getW1rms32());
					break;
				case 11:
					Measurement1
							.setText("Dbm1:\n" + calculator.getDbM1());
					break;
				case 12:
					Measurement1
							.setText("Dbm2:\n" + calculator.getDbM2());
					break;
				case 13:
					Measurement1
							.setText("DbGain:\n" + calculator.getDbGain());
					break;
				default:
					break;
				}
				switch (SelectedMarkerItemChannel2)
				{
				case 0:
					MarkerMeasurement2
							.setText("     Δt\n" + calculator.getdt());
					break;
				case 1:
					MarkerMeasurement2.setText("    1/Δt\n" + calculator
							.getFreq());
					break;
				case 2:
					if (calculator.getdV1().contains("mV"))
						MarkerMeasurement2.setText("     ΔV1\n" + calculator
								.getdV1());
					else
						MarkerMeasurement2.setText("   ΔV1\n" + calculator
								.getdV1());
					break;
				case 3:
					if (calculator.getdV2().contains("mV"))
						MarkerMeasurement2.setText("     ΔV2\n" + calculator
								.getdV2());
					else
						MarkerMeasurement2.setText("   ΔV2\n" + calculator
								.getdV2());
					break;
				default:
					break;
				}
				switch (SelectedItemChannel2)
				{
				case 0:
					Measurement2.setText("Vdc2:\n" + calculator.getVdc2());
					break;
				case 1:
					Measurement2.setText("RMS2:\n" + calculator.getRMS2());
					break;
				case 2:
					Measurement2.setText("Trms2:\n" + calculator.getTRMS2());
					break;
				case 3:
					Measurement2.setText("Vptp2:\n" + calculator.getVpkpk2());
					break;
				case 4:
					Measurement2.setText("VMAX2:\n" + calculator.getVmax2());
					break;
				case 5:
					Measurement2.setText("VMIN2:\n" + calculator.getVmin2());
					break;
				case 6:
					Measurement2.setText("RMS2 2W:\n" + calculator.getW2rms2());
					break;
				case 7:
					Measurement2.setText("RMS2 4W:\n" + calculator.getW2rms4());
					break;
				case 8:
					Measurement2.setText("RMS2 8W:\n" + calculator.getW2rms8());
					break;
				case 9:
					Measurement2
							.setText("RMS2 16W:\n" + calculator.getW2rms16());
					break;
				case 10:
					Measurement2
							.setText("RMS2 32W:\n" + calculator.getW2rms32());
					break;
				case 11:
					Measurement2
							.setText("dBm1:\n" + calculator.getDbM1());
					break;
				case 12:
					Measurement2
							.setText("dBm2:\n" + calculator.getDbM2());
					break;
				case 13:
					Measurement2
							.setText("dBGain:\n" + calculator.getDbGain());
					break;
				default:
					break;
				}

			}
		});

	}

	@Override
	public void updatedSettings(final Map<String, String> settingsMap)
	{

		final Map<String, String> settings = settingsMap;
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{

				VoltageDiv1.setText(settings.get("VDIV1") + "/DIV");
				txtVoltTimeSetting.setText(settings.get("VDIV2") + "/DIV");
				VoltageDiv2.setText(settings.get("VDIV2") + "/DIV");
				timeBase.setText(settings.get("TIMEBASE") + "/DIV");

				if (scope.getChannel1().getIsX10())
					setViewBackground(Probe1, R.drawable.buttongreen);
				else
					setViewBackground(Probe1, R.drawable.button);

				if (scope.getChannel2().getIsX10())
					setViewBackground(Probe2, R.drawable.buttonyellow);
				else
					setViewBackground(Probe2, R.drawable.button);

				try
				{
					int iTrigger = Integer.decode(trigger.getText().toString());
					mGLView.setTriggerLevel(iTrigger);
				} catch (Exception e)
				{

				}
				if (settings.get("HOLD").contentEquals("TRUE"))
				{
					setViewBackground(triggerHold, R.drawable.buttoncyan);
				} else
				{
					setViewBackground(triggerHold, R.drawable.button);
				}
				if (settings.get("IC1").contentEquals("AC"))
				{
					setViewBackground(GND1, R.drawable.button);
					setViewBackground(DC1, R.drawable.button);
					setViewBackground(AC1, R.drawable.buttongreen);
				}
				if (settings.get("IC1").contentEquals("DC"))
				{
					setViewBackground(GND1, R.drawable.button);
					setViewBackground(DC1, R.drawable.buttongreen);
					setViewBackground(AC1, R.drawable.button);
				}
				if (settings.get("IC1").contentEquals("GND"))
				{
					setViewBackground(GND1, R.drawable.buttongreen);
					setViewBackground(DC1, R.drawable.button);
					setViewBackground(AC1, R.drawable.button);
				}
				if (settings.get("IC2").contentEquals("AC"))
				{
					setViewBackground(GND2, R.drawable.button);
					setViewBackground(DC2, R.drawable.button);
					setViewBackground(AC2, R.drawable.buttonyellow);
				}
				if (settings.get("IC2").contentEquals("DC"))
				{
					setViewBackground(GND2, R.drawable.button);
					setViewBackground(DC2, R.drawable.buttonyellow);
					setViewBackground(AC2, R.drawable.button);
				}
				if (settings.get("IC2").contentEquals("GND"))
				{
					setViewBackground(GND2, R.drawable.buttonyellow);
					setViewBackground(DC2, R.drawable.button);
					setViewBackground(AC2, R.drawable.button);
				}
				if (settings.get("TRIGGERMODE").contentEquals("Auto"))
				{
					setViewBackground(triggerRun, R.drawable.buttoncyan);
					setViewBackground(triggerOnce, R.drawable.button);
					setViewBackground(triggerNormal, R.drawable.button);
				}
				if (settings.get("TRIGGERMODE").contentEquals("Normal"))
				{
					setViewBackground(triggerRun, R.drawable.button);
					setViewBackground(triggerOnce, R.drawable.button);
					setViewBackground(triggerNormal, R.drawable.buttoncyan);
				}
				if (settings.get("TRIGGERMODE").contentEquals("Once"))
				{
					setViewBackground(triggerRun, R.drawable.button);
					setViewBackground(triggerOnce, R.drawable.buttoncyan);
					setViewBackground(triggerNormal, R.drawable.button);
				}
				if (settings.get("TRIGGERCHANNEL").contentEquals("CH1"))
				{
					setViewBackground(triggerCH1, R.drawable.buttoncyan);
					setViewBackground(triggerCH2, R.drawable.button);
				} else
				{
					setViewBackground(triggerCH1, R.drawable.button);
					setViewBackground(triggerCH2, R.drawable.buttoncyan);
				}
				if (settings.get("TRIGGERSLOPE").contentEquals("RISING"))
				{
					setViewBackground(triggerFalling, R.drawable.slopedownunselected);
					setViewBackground(triggerRising, R.drawable.slopeupselected);
				} else
				{
					setViewBackground(triggerFalling, R.drawable.slopedownselected);
					setViewBackground(triggerRising, R.drawable.slopeupunselected);
				}
				if (settings.get("AUTORANGE") != null)
				{
					if (settings.get("AUTORANGE").contentEquals("TRUE"))
					{
						setViewBackground(autoRange, R.drawable.buttonred);
					} else
					{
						setViewBackground(autoRange, R.drawable.button);
					}
				}

				if (settings.get("ENDSCALE") != null)
				{
					if (settings.get("ENDSCALE").contentEquals("TRUE"))
					{
						txtVoltTimeSetting.setVisibility(View.INVISIBLE);
					} else
					{
						if (settings.get("LEFT").contentEquals("TRUE"))
						{
							RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
									RelativeLayout.LayoutParams.WRAP_CONTENT,
									RelativeLayout.LayoutParams.WRAP_CONTENT);

							lp.addRule(RelativeLayout.ALIGN_RIGHT, RelativeLayout.TRUE);
							lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
							lp.topMargin = 50;
							lp.rightMargin = 50;
							txtVoltTimeSetting.setLayoutParams(lp);
							if (settings.get("XSCALE").contentEquals("TRUE"))
							{
								txtVoltTimeSetting.setText(settings
										.get("TIMEBASE"));
							} else
							{
								if (scope.selectedChannel == scope
										.getChannel1())
								{
									txtVoltTimeSetting
											.setText("Volts/Div" + settings
													.get("VDIV1"));
								} else
								{
									txtVoltTimeSetting
											.setText("Volts/Div" + settings
													.get("VDIV2"));
								}
							}
							txtVoltTimeSetting.setVisibility(View.VISIBLE);
						} else
						{
							RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
									RelativeLayout.LayoutParams.WRAP_CONTENT,
									RelativeLayout.LayoutParams.WRAP_CONTENT);

							lp.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
							lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
							lp.topMargin = 50;
							lp.leftMargin = 50;
							txtVoltTimeSetting.setLayoutParams(lp);
							if (settings.get("XSCALE").contentEquals("TRUE"))
							{
								txtVoltTimeSetting.setText(settings
										.get("TIMEBASE"));
							} else
							{
								if (scope.selectedChannel == scope
										.getChannel1())
								{
									txtVoltTimeSetting
											.setText("Volts/Div" + settings
													.get("VDIV1"));
								} else
								{
									txtVoltTimeSetting
											.setText("Volts/Div" + settings
													.get("VDIV2"));
								}
							}
							txtVoltTimeSetting.setVisibility(View.VISIBLE);
						}
					}
				}
				try
				{
					mGLView.setYPos1(Integer.valueOf(settings.get("VPOS1")));
					mGLView.setYPos2(Integer.valueOf(settings.get("VPOS2")));
					mGLView.setTriggerLevel(Integer.valueOf(settings
							.get("TRIGGERLEVEL")));
				} catch (Exception e)
				{

				}
				if (settings.get("CALIBRATING") != null)
				{
					if (settings.get("CALIBRATING").contentEquals("TRUE"))
					{
						txtCalibrating.setVisibility(View.VISIBLE);
					} else
					{
						txtCalibrating.setVisibility(View.INVISIBLE);
					}
				}
				if (settings.get("BATTERY") != null)
				{
					if (settings.get("BATTERY").contentEquals("CHARGING"))
					{

						if (batteryAnimation != null)
						{
							if (!batteryAnimation.isRunning())
							{
								batteryAnimation.setOneShot(false);
								batteryAnimation.start();
							}
						} else
						{
							ImageView battery = (ImageView) findViewById(R.id.imgbatteryindicator);

							battery.setBackgroundResource(0);
							battery.setBackgroundResource(R.drawable.batteryanimation);
							batteryAnimation = (AnimationDrawable) battery
									.getBackground();
							batteryAnimation.setOneShot(false);
							batteryAnimation.start();
						}
					}
					if (settings.get("BATTERY").contentEquals("FULL"))
					{
						ImageView battery = (ImageView) findViewById(R.id.imgbatteryindicator);
						if (batteryAnimation != null)
							if (batteryAnimation.isRunning())
								batteryAnimation.stop();
						battery.setBackgroundResource(0);
						battery.setBackgroundResource(R.drawable.battfull);
					}
					if (settings.get("BATTERY").contentEquals("LOW"))
					{
						ImageView battery = (ImageView) findViewById(R.id.imgbatteryindicator);
						if (batteryAnimation != null)
							if (batteryAnimation.isRunning())
								batteryAnimation.stop();
						battery.setBackgroundResource(0);
						battery.setBackgroundResource(R.drawable.battlow);
					}
					if (settings.get("BATTERY").contentEquals("NO"))
					{
						ImageView battery = (ImageView) findViewById(R.id.imgbatteryindicator);
						if (batteryAnimation != null)
							if (batteryAnimation.isRunning())
								batteryAnimation.stop();
						battery.setBackgroundResource(0);
					}
				}
			}
		});

	}

	@Override
	public void updatedWifiSettings(Map<String, String> wifiSettings)
	{
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		String wifiversion = wifiSettings.get("WIFIVERSION");
		editor.putString("VERSIONNUMBERWIFI", wifiversion);
		String scopeversion = wifiSettings.get("SCOPEVERSION");
		editor.putString("VERSIONNUMBERSCOPE", scopeversion);
		String wifiName = wifiSettings.get("SCOPEVERSION");
		editor.putString("VERSIONNUMBERSCOPE", scopeversion);
		String wifiChannel = wifiSettings.get("SCOPEVERSION");
		editor.putString("VERSIONNUMBERSCOPE", scopeversion);
		editor.commit();
	}

	@Override
	public void updatedMarkers(Map<String, String> markerinfo)
	{
		// TODO Auto-generated method stub
		markersInfo = markerinfo;
	}

	private void alertUser(final String text)
	{
		this.runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

}

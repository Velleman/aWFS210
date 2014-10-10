package be.velleman.wfs210;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;

/**
 * @author bn
 * 
 */

public class TCPConnector extends Connector
{

	private static final String TAG = "WFS210-TCPConnector";
	int iPoort;
	String ip;
	InetAddress oIP;
	Socket socket = null;
	OutputStream oStream;
	BufferedInputStream iStream;
	Boolean isReceiving = false;
	Context context;

	public TCPConnector(String ip, int poort)
	{

		this.ip = ip;
		try
		{
			InetAddress[] inetarray = InetAddress.getAllByName(ip);
			oIP = inetarray[0];
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.iPoort = poort;

	}

	private static Boolean isReceiveRunning = false;

	/**
	 * A Thread that will start listening on the socket of this class It will be
	 * interrupted if the socket is <code>null</code> Or there is no bytes to be
	 * red.
	 * 
	 */
	public void startReceivingPackets()
	{

		if (!isReceiveRunning)
		{
			isReceiveRunning = true;
			Thread receiveWorker = new Thread("Receiving Thread")
			{
				public void run()
				{

					try
					{
						byte[] buffer = new byte[4096];

						Packet foundPacket = null;
						int Count = 0;
						isReceiving = true;
						while (isReceiveRunning)
						{
							if (socket != null)
							{
								Count = iStream.read(buffer);
								if (Count > 0)
								{
									//Log.i(TAG,Integer.toString(iStream.available()));								
									parser.addDataToParse(buffer, Count); // try adding Data							
									System.currentTimeMillis();
									foundPacket = parser.parseNext();
									while (foundPacket != null)
									{
										//send foundPacket;
										//Log.i(TAG,"Packet Found");									
										notifyNewPacket(foundPacket);

										foundPacket = parser.parseNext();
									}
									System.currentTimeMillis();
								} else
								{
									notifyDisconnectListeners();
									isReceiveRunning = false;

								}
							} else
							{
								isReceiveRunning = false;

							}

						}
						isReceiving = false;
					} catch (IOException error)
					{
						error.printStackTrace();
						notifyDisconnectListeners();
						isReceiveRunning = false;
						isReceiving = false;
					}

				}
			};
			receiveWorker.start();
		}
	}

	@Override
	public void open()
	{
		Thread worker1 = new Thread("Connection Init")
		{
			public void run()
			{
				try
				{
					socket = new Socket(ip, iPoort);
					socket.setTcpNoDelay(true);

					while (!socket.isConnected())
						;
					//ShowToast("Connected");
					isConnected = true;
					oStream = socket.getOutputStream();
					iStream = new BufferedInputStream(socket.getInputStream());
					notifyConnectListeners();
				} catch (IOException e)
				{
					//ShowToast("Connecting Failed - Please Connect to Wifi from the wifi scope");
					//Close();
					//Log.d(TAG, e.toString());
				} catch (Exception e)
				{
					//Close();
				}
			}
		};
		worker1.start();

	}

	@Override
	public void close()
	{
		try
		{
			if (socket != null)
				socket.close();
		} catch (IOException e)
		{

			e.printStackTrace();
		}
		socket = null;
		iStream = null;
		oStream = null;
		isConnected = false;
		notifyDisconnectListeners();
	}

	@Override
	public void send(final Packet packet) throws IOException
	{
		// TODO Auto-generated method stub
		Thread worker1 = new Thread("Sending Thread")
		{
			public void run()
			{
				try
				{
					while (oStream == null)
						;
					oStream.write(packet.getPacket());
				} catch (IOException e)
				{
					close();
				}
			}
		};
		worker1.start();

	}

}

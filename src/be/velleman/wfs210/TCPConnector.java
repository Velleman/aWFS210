package be.velleman.wfs210;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.util.Log;

/**
 * @author bn
 * 
 */

public class TCPConnector extends Connector {

	private static final String TAG = "WFS210-TCPConnector";
	int iPoort;
	String ip;
	InetAddress oIP;
	Socket socket = null;
	OutputStream oStream;
	BufferedInputStream iStream;
	Boolean isReceiving = false;
	Context context;

	public TCPConnector(String ip, int poort) {

		this.ip = ip;
		try {
			InetAddress[] inetarray = InetAddress.getAllByName(ip);
			oIP = inetarray[0];
		} catch (UnknownHostException e) {
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
	public void startReceivingPackets() {

		if (!isReceiveRunning) {
			isReceiveRunning = true;
			Thread receiveWorker = new Thread("Receiving Thread") {
				public void run() {

					try {
						byte[] buffer = new byte[4096];

						Packet foundPacket = null;
						int Count = 0;
						isReceiving = true;
						while (isReceiveRunning) {
							if (socket != null) {
								Count = iStream.read(buffer);
								if (Count > 0) {
									parser.addDataToParse(buffer, Count); 
									System.currentTimeMillis();
									foundPacket = parser.parseNext();
									while (foundPacket != null) {
										notifyNewPacket(foundPacket);
										foundPacket = parser.parseNext();
									}
									System.currentTimeMillis();
								} else {
									notifyDisconnectListeners();
									isReceiveRunning = false;
								}
							} else {
								isReceiveRunning = false;
							}
						}
						isReceiving = false;
					} catch (IOException error) {
						Log.e(TAG,"IOException startReceivingPackets");
						close();
						isReceiveRunning = false;
						isReceiving = false;
					} /*catch (Exception e) {
						Log.e(TAG,"Exception startReceivingPackets");
						close();
						isReceiveRunning = false;
						isReceiving = false;
					}*/

				}
			};
			receiveWorker.start();
		}
	}

	@Override
	public void open() {
		Thread worker1 = new Thread("Connection Init") {
			public void run() {
				try {
					socket = new Socket(ip, iPoort);
					socket.setTcpNoDelay(true);
					while (!socket.isConnected());
					isConnected = true;
					oStream = socket.getOutputStream();
					iStream = new BufferedInputStream(socket.getInputStream());
					notifyConnectListeners();
				} catch (IOException e) {
					Log.e(TAG,"IOException open");
					e.printStackTrace();
					close();
				} catch (Exception e) {
					Log.e(TAG,"Exception open");
					e.printStackTrace(); 
					close();
				}
			}
		};
		worker1.start();

	}

	@Override
	public void close() {
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG,"Failed to properly close the socket");
		}
		socket = null;
		iStream = null;
		oStream = null;
		isConnected = false;
		notifyDisconnectListeners();
	}

	@Override
	public void send(Packet packet) {
		try {
			oStream.write(packet.getPacket());
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
		catch(NullPointerException e)
		{
			Log.e(TAG,"NullPointerException send");
		}
	}

}

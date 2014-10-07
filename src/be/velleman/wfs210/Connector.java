package be.velleman.wfs210;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Connector
{
	List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();

	protected WFS210_Parser parser;
	protected Boolean isConnected = false;

	public Connector()
	{
		this.parser = new WFS210_Parser();
		this.isConnected = false;
	}

	public abstract void open();

	public abstract void send(Packet packet) throws IOException;

	public abstract void close();

	public void notifyConnectListeners()
	{
		for (ConnectionListener cl : connectionListeners)
		{
			cl.connected();
		}
	}

	public void notifyDisconnectListeners()
	{
		for (ConnectionListener dl : connectionListeners)
		{
			dl.disconnected();
		}
	}

	public void notifyNewPacket(Packet p)
	{
		for (ConnectionListener dl : connectionListeners)
		{
			dl.newPacketFound(p);
		}
	}

	public void addConnectionListener(ConnectionListener cl)
	{
		connectionListeners.add(cl);
	}

	public void clearAllConnectionListeners()
	{
		connectionListeners.clear();
	}

}

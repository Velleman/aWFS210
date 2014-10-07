package be.velleman.wfs210;

import java.io.IOException;

public class FakeConnector extends Connector
{

	@Override
	public void open()
	{
		// TODO Auto-generated method stub
		notifyConnectListeners();
		isConnected = true;
	}

	@Override
	public void send(Packet packet) throws IOException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void close()
	{
		// TODO Auto-generated method stub
		notifyDisconnectListeners();

	}

}

package be.velleman.wfs210;

interface ConnectionListener
{
	public void disconnected();

	public void connected();

	public void newPacketFound(Packet p);
}

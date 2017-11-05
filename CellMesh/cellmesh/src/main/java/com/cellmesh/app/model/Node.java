package com.cellmesh.app.model;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.util.Xml;

import org.slf4j.impl.StaticLoggerBinder;
import org.w3c.dom.NodeList;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Queue;

import io.netty.util.AsciiString;
import io.underdark.Underdark;
import com.cellmesh.app.MainActivity;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;
import io.underdark.util.nslogger.NSLogger;
import io.underdark.util.nslogger.NSLoggerAdapter;

public class Node implements TransportListener
{
	private boolean running;
	private MainActivity activity;
	private long nodeId;
	private Transport transport;
	private INodeListener listener;
	private ArrayList<Link> links = new ArrayList<>();
	private Set<Long> ids=new HashSet<>();
	private String name;
	private nameManager nm;

	final private int SEND_NAMEHASH = 1;
	final private int SEND_NAMEDATA = 2;
	final private int SEND_MESSAGE = 10;

	public Node(MainActivity activity,INodeListener listener,String name)
	{
		this.name=name;
		this.activity = activity;
		this.listener=listener;
		do
		{
			nodeId = new Random().nextLong();
		} while (nodeId == 0);

		if(nodeId < 0)
			nodeId = -nodeId;

		nm = new nameManager(nodeId, name);

		ids.add(nodeId);
		configureLogging();
		EnumSet<TransportKind> kinds = EnumSet.of(TransportKind.BLUETOOTH, TransportKind.WIFI);
		this.transport = Underdark.configureTransport(
				234235,
				nodeId,
				this,
				null,
				activity.getApplicationContext(),
				kinds
		);
	}

	public Map<Long,String> getNamesMap() {
		return Collections.unmodifiableMap(nm.getMap());
	}

	private void configureLogging()
	{
		NSLoggerAdapter adapter = (NSLoggerAdapter)
				StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger(Node.class.getName());
		adapter.logger = new NSLogger(activity.getApplicationContext());
		adapter.logger.connect("192.168.5.203", 50000);
		Underdark.configureLogging(true);
	}

	public void start()
	{
		if(running)
			return;
		Log.d("Mesh","Starting Node");
		running = true;
		transport.start();
	}

	public void stop()
	{
		if(!running)
			return;

		Log.d("Mesh","Stopping Node");
		running = false;
		transport.stop();
	}

	public void broadcastMessage(String frameData)
	{
		if(links.isEmpty())
			return;
		listener.onDataSent(frameData,nodeId);
		for(Link link : links)
			link.sendFrame(addOp(SEND_MESSAGE, frameData));
	}

	public void broadcastMessage(byte[] frameData) {
		if ( !links.isEmpty() ) {
			// listener.onDataSent(frameData, nodeId);
			for ( Link link : links ) {
				link.sendFrame(frameData);
			}
		}
	}
	public void sendMessage(int op, String frameData, Link target)
	{
		target.sendFrame(addOp(op, frameData));
	}
	private byte[] addOp(int op, String data) {
		byte[] r = ('x' + data).getBytes();
		r[0] = (byte) op;
		return r;
	}

	//Call this when the names need to be updated by the UID
	private void doNameUpdate(){
		listener.onNamesUpdated(null);
	}
	private void handleEmergencyMessage(){
		//listener.onEmergency(null);
	}
	//region TransportListener
	@Override
	public void transportNeedsActivity(Transport transport, ActivityCallback callback)
	{
		callback.accept(activity);
	}

	@Override
	public void transportLinkConnected(Transport transport, Link link)
	{
		Log.d("Mesh","Link "+Long.toString(link.getNodeId())+" Joined");
		links.add(link);
		ids.add(link.getNodeId());

		// Send our name data hash.
		sendMessage(SEND_NAMEHASH, nm.getHash(), link);

		listener.onConnected(Collections.unmodifiableSet(ids),link.getNodeId());
	}

	@Override
	public void transportLinkDisconnected(Transport transport, Link link)
	{
		Log.d("Mesh","Link "+Long.toString(link.getNodeId())+" Left");
		ids.remove(link.getNodeId());
		links.remove(link);
		listener.onDisconnected(Collections.unmodifiableSet(ids),link.getNodeId());
	}

	private void compareHash(String hash, Link link) {
		if ( !hash.equals(nm.getHash()) ) {
			// Send our names.
			Map<Long, String> map = nm.getMap();
			for ( Map.Entry<Long,String> item : map.entrySet() ) {
				String message =  Long.toString(item.getKey()) + ':' + item.getValue();
				sendMessage(SEND_NAMEDATA, message, link);
			}
		}
	}
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData)
	{
		/*
		00: reserved
		01: node hash received
		02: node data received
		10: chat message received
		 */
		int op = Integer.parseInt(new String(frameData, 0, 1, StandardCharsets.US_ASCII));
		String data = new String(frameData, 1, frameData.length, StandardCharsets.US_ASCII);

		switch(op) {
			case 0:
				// Reserved
				break;
			case SEND_NAMEHASH:
				// Name hash received
				compareHash(data, link);
				break;
			case SEND_NAMEDATA:
				// Name data received
				if ( data.indexOf(':') > 0 ) {
					Long srcId = Long.parseLong(data.substring(0, data.indexOf(':')));
					String name = data.substring(data.indexOf(':'), data.length());

					nm.addName(srcId, name);
				}
			case SEND_MESSAGE:
				// Message received
				listener.onDataReceived(data, link.getNodeId());
		}
	}
	//endregion
} // Node

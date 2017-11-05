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
			link.sendFrame(frameData.getBytes());
	}

	//Call this when the names need to be updated by the UID

	private void doNameUpdate(){
		listener.onNamesUpdated();
	}
	private void handleEmergencyMessage(){
		listener.onEmergency(null);
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

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData)
	{
		listener.onDataReceived(new String(frameData, StandardCharsets.US_ASCII),link.getNodeId());
	}
	//endregion
} // Node

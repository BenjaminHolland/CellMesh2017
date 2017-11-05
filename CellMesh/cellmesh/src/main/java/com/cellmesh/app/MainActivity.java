package com.cellmesh.app;

import android.app.LauncherActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.cellmesh.app.model.INodeListener;
import com.cellmesh.app.model.Node;

public class MainActivity extends AppCompatActivity implements INodeListener
{
	private ListView peersTextView;
	private ListView chatTextView;

	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	Node node;
	Map<Long,String> names;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
		peersTextView = (ListView) findViewById(R.id.peersTextView);
		//peersTextView.setAdapter(adapter);
		chatTextView = (ListView) findViewById(R.id.recieved_message);
		chatTextView.setAdapter(adapter);

		//UI Must gather a name and create a listener before calling node.start
		node = new Node(this,this,"namidy name name");
		names = node.getNamesMap();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if ( node != null ) {
			node.start();
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		if(node != null)
			node.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	public void onDisconected(){
		//remove from array of peers
	}
	public void onConnected(){
		//add to array of peers
	}

	public void BroadcastMessage(String message) {
		node.broadcastMessage(message);
	}

	public void onEmergency(String newMessage, Long fromLinkId) {
		onDataReceived( "<b>" + newMessage + "</b>", fromLinkId);
	}
	public void onNamesUpdated(Map<Long, String> names){

	}

	@Override
	public void onNamesUpdated(Map<Long, String> names) {
		names = node.getNamesMap();
	}

	@Override
	public void onConnected(Set<Long> readOnlyIds, Long newId) {

	}

	@Override
	public void onDisconnected(Set<Long> readOnlyIds, Long oldLinkId) {

	}

	public void onDataReceived(String newMessage, Long fromLinkId) {

		listItems.add(names.get(fromLinkId) + " ---> " + newMessage);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onDataSent(String newMessage, Long fromLinkId) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static boolean started = false;

} // MainActivity

package com.cellmesh.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.cellmesh.app.model.INodeListener;
import com.cellmesh.app.model.Node;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class MessagingActivity extends Activity implements INodeListener
{
	private ListView peersTextView;
	private ListView chatTextView;
	private EditText Message;
	ArrayList<String> listItems = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	ArrayList<String> listItems2 = new ArrayList<String>();
	ArrayAdapter<String> adapter2;

	Node node;
	Map<Long,String> names;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SharedPreferences sharedPref = this.getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		String name = sharedPref.getString(getString(R.string.pref_name), "");

		if ( name.equals("") ) {
			Intent intent = new Intent(MessagingActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}

		setContentView(R.layout.actvity_messaging);

		adapter2=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems2);
		peersTextView = (ListView) findViewById(R.id.peersTextView);
		peersTextView.setAdapter(adapter2);

		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
		chatTextView = (ListView) findViewById(R.id.recieved_message);
		chatTextView.setAdapter(adapter);

		Message = (EditText) findViewById(R.id.message);
		//UI Must gather a name and create a listener before calling node.start
		node = new Node(MessagingActivity.this,this,name);
		names = node.getNamesMap();

		Button Send_button = (Button) findViewById(R.id.Send);
		Send_button.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v){
				BroadcastMessage(Message.getText().toString());
				onDataReceived(Message.getText().toString(), node.getNodeId());
				Message.setText("");
			}
		});
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

	@Override
	public void onEmergency(String newMessage, Long fromLinkId) {
		onDataReceived( "<b>" + newMessage + "</b>", fromLinkId);
	}

	@Override
	public void onNamesUpdated(Map<Long, String> names) {
		names = node.getNamesMap();
		updatePeerList();
	}

	public void updatePeerList() {
		listItems2.clear();
		Set<Long> ids = node.getPeerIds();

		for ( Long id : ids ) {
			if ( names.get(id) != null ) {
				if ( !id.equals(node.getNodeId()) )
					listItems2.add(names.get(id));
			} else {
				listItems2.add("Unknown");
			}
		}

		adapter2.notifyDataSetChanged();
	}

	@Override
	public void onConnected(Set<Long> readOnlyIds, Long newId) {
		updatePeerList();
	}

	@Override
	public void onDisconnected(Set<Long> readOnlyIds, Long oldLinkId) {
		updatePeerList();
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

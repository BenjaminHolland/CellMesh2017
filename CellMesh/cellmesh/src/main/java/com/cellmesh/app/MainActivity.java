package com.cellmesh.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.cellmesh.app.model.INodeListener;
import com.cellmesh.app.model.Node;

public class MainActivity extends AppCompatActivity
{
	private TextView peersTextView;
	private TextView framesTextView;

	Node node;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		peersTextView = (TextView) findViewById(R.id.peersTextView);
		framesTextView = (TextView) findViewById(R.id.framesTextView);

		//UI Must gather a name and create a listener before calling node.start
		node = new Node(this,null,"");

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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
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

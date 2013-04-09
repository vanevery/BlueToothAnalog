package com.example.bluetoothanalog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;
import cc.arduino.btserial.BtSerial;

public class MainActivity extends Activity implements OnClickListener {

	public static final String LOGTAG = "BlueToothAnalog";
	public static final String BLUETOOTH_MAC_ADDRESS = "00:06:66:42:1F:DF";
	
	public static final int DELIMITER = 10;  // Newline in ASCII
	
	BtSerial btserial;
	
	// Declare the custom view
	MyDrawingView myDrawingView;
	
	Button connectButton;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connectButton = (Button) this.findViewById(R.id.connectButton);
		connectButton.setOnClickListener(this);
		
		// Setup the custom view
		myDrawingView = (MyDrawingView) this.findViewById(R.id.myDrawingView);
		
		btserial = new BtSerial(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		btserial.disconnect();
	}

	// Handlers let us interact with threads on the UI thread
	// The handleMessage method receives messages from other threads and will act upon it on the UI thread
	Handler handler = new Handler() {
		  @Override
		  public void handleMessage(Message msg) {
		    
		    // Pull out the data that was packed into the message with the key "serialvalue"
			int messageData = msg.getData().getInt("serialvalue");
			
			// Send it over to the custom view
			myDrawingView.setYoverTime(messageData);
		  }
	};	
	
	public void btSerialEvent(BtSerial btserialObject) {
		String serialValue = btserialObject.readStringUntil(DELIMITER);
		
		if (serialValue != null)
		{
			Log.v(LOGTAG,"Data: " + serialValue);

			try {
				// The data is coming to us as an ASCII string so we have to turn it into an int
				// First we have to trim it to remove the newline
				int intSerialValue = Integer.parseInt(serialValue.trim());

				// Since btSerialEvent is happening in a separate thread, 
				// we need to use a handler to send a message in order to interact with the UI thread
				
				// First we obtain a message object
				Message msg = handler.obtainMessage();
				
				// Create a bundle to hold data
				Bundle bundle = new Bundle();
				
				// Put our value with the key "serialvalue"
				bundle.putInt("serialvalue", intSerialValue);
				
				// Set the message data to our bundle
				msg.setData(bundle);
				
				// and finally send the message via the handler
				handler.sendMessage(msg);
			
			} catch (NumberFormatException nfe) {
				// Not a number
				Log.v(LOGTAG,"" + serialValue + " is not a number");
			}
			
		}
	}

	@Override
	public void onClick(View clickedView) {
		if (clickedView == connectButton) {
			if (btserial.isConnected()) {
				Log.v(LOGTAG, "Already Connected, Disconnecting");
				btserial.disconnect();
			}
			
			btserial.connect(BLUETOOTH_MAC_ADDRESS);
			if (btserial.isConnected()) {
				Log.v(LOGTAG,"Connected");
			}
			else {
				Log.v(LOGTAG,"Not Connected");
			}
		}
	}
}
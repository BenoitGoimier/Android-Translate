package fr.benoitgoimier.translate;


import java.util.ArrayList;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognizerIntent;
import android.view.GestureDetector;

import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener  {

	public static final int CODE_RETOUR = 222; 
	public static final int CODE_RETOUR_RECOGNIZER = 333; 
	public static final String PARAM_SOURCE = "MOT_SOURCE"; 
	public static final String PARAM_REPONSE = "MOT_REPONSE";
	private static final int GLISSE_MIN_DISTANCE = 400;
	
	private Button btnTraduire;
	private Button btnReconnaitre;
	private EditText txtMotFrancais;
	// Pour la gestion du fling
	private GestureDetector gesture;
	// Pour la gestion de l'�tat Wifi et GSM
	private BroadcastReceiver connexionReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		gesture = new GestureDetector(this,this);
				
		txtMotFrancais = (EditText)findViewById(R.id.mot_edittext);
		btnTraduire =(Button)findViewById(R.id.buttonTraduire);
		btnTraduire.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, getResources().getString(R.string.msgToast1), Toast.LENGTH_LONG).show();	
				Intent intent = new Intent(MainActivity.this,ChildActivity.class);
				intent.putExtra(PARAM_SOURCE, txtMotFrancais.getText().toString());
				startActivityForResult(intent, CODE_RETOUR);
			}
		});
		
		btnReconnaitre =(Button)findViewById(R.id.buttonReconnaitre);
		btnReconnaitre.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, getResources().getString(R.string.msgToast2), Toast.LENGTH_LONG).show();	
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "fr-FR");
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
				try{
					startActivityForResult(intent, CODE_RETOUR_RECOGNIZER);
				}
				catch(ActivityNotFoundException ex)
				{
					Toast.makeText(MainActivity.this, getResources().getString(R.string.msgToast3), Toast.LENGTH_LONG).show();	
				}
			}
		});
		
		IntentFilter filtreConnectivity = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		connexionReceiver = new BroadcastReceiver() {
			 
			private boolean activeWIFI;
			private boolean activeGSM;
			
			
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
					if(activeNetwork != null){
						
					    if(activeNetwork.isConnected()==true)
					    {
					        if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
					        {
					        	activeWIFI = true;
					        	activeGSM = false;
					        }
				        	else if(activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE)
				        	{
					        	activeWIFI = false;
					        	activeGSM = true;
				        	}		
					    }
					    else
					    {
					    	activeWIFI=false;
							activeGSM = false;
					    }
					}
					else
					{
						activeWIFI = false;
						activeGSM = false;
					}
					if (activeWIFI)
					{
						Toast.makeText(context,R.string.wifiON, Toast.LENGTH_LONG).show();
						btnTraduire.setEnabled(true);
						btnReconnaitre.setEnabled(true);
					}
					else if (activeGSM)
					{
						Toast.makeText(context,R.string.gsmON, Toast.LENGTH_LONG).show();
						btnTraduire.setEnabled(true);
						btnReconnaitre.setEnabled(true);
					}
					else
					{
						Toast.makeText(context,R.string.wifigsmOFF, Toast.LENGTH_LONG).show();
						btnTraduire.setEnabled(false);
						btnReconnaitre.setEnabled(false);
					}
			}
		};
		registerReceiver(connexionReceiver,filtreConnectivity);			
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		switch(requestCode)
		{
		case (MainActivity.CODE_RETOUR):
			switch(resultCode)
			{
			case( Activity.RESULT_OK):
				String mot = intent.getStringExtra(MainActivity.PARAM_REPONSE);
				Toast.makeText(this,getResources().getString(R.string.msgToast5,mot) , Toast.LENGTH_LONG).show();
				return;
			case (Activity.RESULT_CANCELED):
				Toast.makeText(this,R.string.msgToast4, Toast.LENGTH_LONG).show();
				return;
			}
		case(MainActivity.CODE_RETOUR_RECOGNIZER):
			switch(resultCode)
			{
			case( Activity.RESULT_OK):
				if (intent != null)
				{
					ArrayList<String> mots = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					if (mots.size()>0)
					{
						this.txtMotFrancais.setText("");
						//for(String mot : mots)
						//	this.txtMotFrancais.setText(txtMotFrancais.getText()+mot+" ");
						this.txtMotFrancais.setText(mots.get(0));
					}
				}
				return;
			case (Activity.RESULT_CANCELED):
				Toast.makeText(this,R.string.msgToast4, Toast.LENGTH_LONG).show();
				return;
			}
		}
	}

	// Gesture - Premi�re �tape : d�tecter les �v�nements "touch" sur l'activit�
	// Signaler l'�v�nement � l'objet de prise en charge de la gesture
	@Override
	public boolean onTouchEvent(MotionEvent event){ 
	    this.gesture.onTouchEvent(event);
	    return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if(e1.getX() > e2.getX() && Math.abs(e1.getX() - e2.getX()) > GLISSE_MIN_DISTANCE) 
		{            
			Toast.makeText(MainActivity.this, getResources().getString(R.string.msgToast1), Toast.LENGTH_LONG).show();	
			Intent intent = new Intent(MainActivity.this,ChildActivity.class);
			intent.putExtra(PARAM_SOURCE, txtMotFrancais.getText().toString());
			startActivityForResult(intent, CODE_RETOUR);
		}
		return true;
	}
	@Override
	public void onLongPress(MotionEvent e) {
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {	
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}
	
}

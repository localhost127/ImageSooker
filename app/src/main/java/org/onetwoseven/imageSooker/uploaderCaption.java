package org.onetwoseven.imageSooker;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.onetwoseven.imageSooker.Network;

import org.onetwoseven.imageSooker.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

public class uploaderCaption extends Activity {
	public final static String CAPTION = "org.onetwoseven.imageSooker.CAPTION";
	Intent j;
	Intent login;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		
		j = getIntent();
		
		/*login = new Intent(this,Login.class);
		
		startActivityForResult(login, 1);*/
		
		
		//String hostname = "http://10.0.1.99";
		//String hostname = "http://10.0.1.154:8080";
		//String path = "/Forests";
		//new CheckLoggedIn().execute(hostname + path);
		new CheckLoggedIn().execute();
		
		setContentView(R.layout.caption);
		
		Button okBtn = (Button) findViewById(R.id.okBtn);
		Button notYouBtn = (Button) findViewById(R.id.notYouBtn);
		notYouBtn.setEnabled(false);
		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		
		okBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        doUpload(v);
		    }
		});
		notYouBtn.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        notYou(v);
		        
		    }
		});
		
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cancel(v);
				
			}
		});
		
	}	
	
	public void doUpload(View view){
		 
		
	 
		final Intent i = new Intent(this,uploader.class);
				
		EditText userInput = (EditText)findViewById(R.id.captionEdit);
		
			
		String caption = userInput.getText().toString();
	  
		i.putExtra(CAPTION, caption);
		i.putExtras(j.getExtras());
		i.setAction(j.getAction());
		startActivity(i);
		setResult(RESULT_OK);
		finish();
	
		
		
				
		
	}
	public void notYou(View view){
		SharedPreferences settings = getSharedPreferences("store",MODE_PRIVATE);
		SharedPreferences.Editor edit1 = settings.edit();
		edit1.clear();
		edit1.commit();
		TextView txtView = (TextView) findViewById(R.id.userReminder);
		Button notYouBtn = (Button) findViewById(R.id.notYouBtn);
		txtView.setText("Not Logged In");
		notYouBtn.setEnabled(false);
		//Intent login = new Intent(this, Login.class);
		//startActivityForResult(login, 1);
		
	}
	
	public void cancel(View view){
		setResult(RESULT_CANCELED);
		finish();
		
	}
	
	class CheckLoggedIn extends AsyncTask<String,Void,Boolean>{

		
		@Override
		protected Boolean doInBackground(String... uri) {
			Log.v(this.getClass().getName(), "Checking for logged in status");
				if (new Network(getBaseContext()).checkLoggedIn()){
					return true;
				}else{
					return false;
				}
				
			
		}
		
		protected void onPostExecute(Boolean status){
			SharedPreferences settings = getSharedPreferences("store",MODE_PRIVATE);
			SharedPreferences.Editor edit1 = settings.edit();
			
			TextView txtView = (TextView) findViewById(R.id.userReminder);
			Button notYouBtn = (Button) findViewById(R.id.notYouBtn);
			
			
			if(status){
				notYouBtn.setEnabled(true);
				edit1.putBoolean("LoggedIn", true);
				
				
				txtView.setText("Logged In");
				
			}else{
				edit1.clear();
				txtView.setText("Not Logged In");
			}
			edit1.commit();
			
		}

		
	}

	
	
}



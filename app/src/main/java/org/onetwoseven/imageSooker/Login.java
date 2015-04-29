package org.onetwoseven.imageSooker;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import android.app.Activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import android.widget.EditText;


public class Login extends Activity {
    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		CookieManager cookieManager = new CookieManager();
//		CookieHandler.setDefault(cookieManager);


        settings = getSharedPreferences("store", MODE_PRIVATE);
//		
//		Log.v(this.getClass().getName(),"Hmm " + settings.getString("username", ""));
        //Log.v(this.getClass().getName(),Boolean.toString(settings.getString("username", null).equals(null)));
        if (settings.contains("session")) {
            setResult(RESULT_OK);
            finish();
        } else {
            setContentView(R.layout.login);

            Button btn = (Button) findViewById(R.id.login);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doLogin(v);

                }
            });
        }


    }


    public void doLogin(View view) {

        //SharedPreferences settings = getSharedPreferences("store",MODE_PRIVATE);

        EditText usernameBox = (EditText) view.getRootView().findViewById(R.id.username);
        EditText passwordBox = (EditText) view.getRootView().findViewById(R.id.password);
        //CheckBox rememberBox = (CheckBox)view.getRootView().findViewById(R.id.remember);

        String username = usernameBox.getText().toString();
        String password = passwordBox.getText().toString();
        //Boolean  remember = rememberBox.isChecked();

        //String hash = sha1(username.toLowerCase() + password).toLowerCase();

        //String hostname = "http://10.0.1.99";
        //String hostname = "http://10.0.1.154:8080";
        //String path = "/Account/Login";

        String[] LogInArgs = new String[2];
        //LogInArgs[0] =  hostname + path;
        LogInArgs[0] = username;
        //LogInArgs[1] = password;
        LogInArgs[1] = sha1(username.toLowerCase() + password).toLowerCase();
        new LogIn().execute(LogInArgs);

        //username: username
        //password2: lowercase(sha1(lowercase(login) + password))


//	    SharedPreferences.Editor editor = settings.edit();
//	    
//	    editor.putString("username", username);
//	    editor.putString("hash", hash);
//	    
//	    Log.v(this.getClass().getName(),hash);
//	    
//	    if(!remember){
//	    	editor.putBoolean("consume", true);
//	    }
//	    editor.commit();


    }

    public String sha1(String input) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            ByteArrayInputStream fis = new ByteArrayInputStream(input.getBytes());

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            ;
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;

        } catch (IOException e) {
            return null;
        }
    }


    class LogIn extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... args) {

            //String uri = args[0];
            String username = args[0];
            String password = args[1];

            if (new Network(getBaseContext()).logIn(username, password)) {
                return true;
            } else {
                return false;
            }

        }

        protected void onPostExecute(Boolean status) {
            //SharedPreferences settings = getSharedPreferences("store",MODE_PRIVATE);
            SharedPreferences.Editor edit1 = settings.edit();

            if (status) {
                edit1.putBoolean("LoggedIn", true);
                edit1.commit();
                setResult(RESULT_OK);
                finish();

            } else {
                edit1.clear();
                edit1.commit();

            }


        }


    }


}

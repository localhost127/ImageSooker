package org.onetwoseven.imageSooker;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.onetwoseven.imageSooker.uploader.AsyncUpload;
import org.onetwoseven.imageSooker.uploader.AsyncUpload.uploadResults;

import android.content.SharedPreferences;
import android.util.Log;

import android.content.Context;
import java.net.CookieManager;


public class Network {
    public String IP = "http://img.127001.org";
    //public String IP = "http://10.127.0.1:5001";
    private String Boundary = "----xxxRRRFFFfffxxx----";
    private String Newline = "\r\n";
    private int Buffsize = 1024;

    private HttpURLConnection conn;
    private Context context;
    private SharedPreferences settings;

    public Network(Context ctx) {
        context = ctx;
        settings = context.getSharedPreferences("store", Context.MODE_PRIVATE);
    }

    public boolean checkLoggedIn() {
        DataOutputStream foo = setupConnection(IP + "/loggedin.php");
        try {
            foo.flush();
            int responseCode = conn.getResponseCode();

            Log.v(this.getClass().getName(), "Response Code:" + Integer.toString(responseCode));
            if (responseCode == 200) {
                return true;
            }
        } catch (Exception e) {
            Log.v(this.getClass().getName(), e.toString());
        } finally {
            conn.disconnect();
        }
        return false;

    }


    public boolean logIn(String username, String password) {


        SharedPreferences.Editor edit1 = settings.edit();

        DataOutputStream foo = setupConnection(IP + "/loggedin.php");
        try {

            // Oh god, password
            foo.writeBytes("--" + Boundary + Newline);
            foo.writeBytes("Content-Disposition: form-data; name=\"username\"" + Newline + Newline
                    + username + Newline);

            foo.writeBytes("--" + Boundary + Newline);
            foo.writeBytes("Content-Disposition: form-data; name=\"remember\"" + Newline + Newline
                    + "true" + Newline);


            foo.writeBytes("--" + Boundary + Newline);
            foo.writeBytes("Content-Disposition: form-data; name=\"password2\"" + Newline + Newline
                    + password + Newline);


            foo.writeBytes("--" + Boundary + "--" + Newline);
            foo.flush();
            int responseCode = conn.getResponseCode();
            Log.v(this.getClass().getName(), "Response Code:" + Integer.toString(responseCode));
            if (responseCode == 200) {
                List<String> cookieList = conn.getHeaderFields().get("Set-Cookie");
//				for(List<String> headers : conn.getHeaderFields().values()){
//					
//					for(String header: headers){
//						Log.v(this.getClass().getName(), "Header:" + header);
//					}
//				}
                if (cookieList != null) {
                    for (String cookieTemp : cookieList) {
                        Log.v(this.getClass().getName(), "Cookie:" + cookieTemp);
                        if (cookieTemp.startsWith("imgsooker=")) {
                            edit1.putString("session", cookieTemp.substring(0, cookieTemp.indexOf(";")));
                            Log.v(this.getClass().getName(), "Saved cookie!" + cookieTemp);
                            edit1.commit();


                        }
                    }

                }
                return true;
            }
        } catch (Exception e) {
            Log.v(this.getClass().getName(), e.toString());
        } finally {
            conn.disconnect();
        }
        return false;

    }


    public uploadResults uploadImage(InputStream is, String name,
                                     int size, String mime, String caption, AsyncUpload uploadInstance) {
        //String hostname = "http://10.0.1.99";

        String path = "/index.php";

        DataOutputStream foo = setupConnection(IP + path);

        try {
            foo.writeBytes("--" + Boundary + Newline);


            if (caption != null) {

                foo.writeBytes("Content-Disposition: form-data; name=\"caption\"" + Newline + Newline
                        + caption + Newline);
                foo.writeBytes("--" + Boundary + Newline);
            }

            foo.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                    + name + "\"" + Newline);
            foo.writeBytes("Content-Type: " + mime + Newline + Newline);


            // Send data
            int bytesAvailable = is.available();

            bytesAvailable = Math.min(bytesAvailable, Buffsize);

            byte[] buffer = new byte[bytesAvailable];

            int bytesRead = is.read(buffer, 0, Buffsize);
            int noChunks = 1;

            int progress = 0;

            while (bytesRead > 0) {

                foo.write(buffer, 0, bytesAvailable);
                bytesAvailable = is.available();
                bytesAvailable = Math.min(bytesAvailable, Buffsize);

                bytesRead = is.read(buffer, 0, bytesAvailable);
                //Log.v(this.getClass().getName(),
//						"Progress: " + Integer.toString(bytesAvailable)
//						+ " " + Integer.toString(Buffsize) + " "
//						+ Integer.toString(bytesRead));
                int bar = (int) (((float) (noChunks * Buffsize) / size) * 100);
                if (bar != progress) {
                    progress = bar;
                    uploadInstance.myPublish(progress);
                }

                noChunks = noChunks + 1;
                // dos.flush();

            }
            foo.writeBytes(Newline);
            foo.writeBytes("--" + Boundary + "--" + Newline);
            foo.flush();
            int statusCode = conn.getResponseCode();

            return uploadInstance.new uploadResults(statusCode,
                    conn.getHeaderField("Location"));

        } catch (Exception e) {
            return uploadInstance.new uploadResults(0, "");
        } finally{
            conn.disconnect();
        }


    }


    private DataOutputStream setupConnection(String uri) {


        DataOutputStream dos;

        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url
                    .openConnection();

//			CookieManager cookieManager = CookieManager.getInstance();
//			String cookie = cookieManager.getCookie(conn.getURL().toString());

            //This deletes cookies erroneously stored (and then concatenated against the spec, with a comma)
            CookieStore cookieStore = ((java.net.CookieManager)CookieHandler.getDefault()).getCookieStore();
            List<HttpCookie> cookies = cookieStore.getCookies();
            cookieStore.removeAll();


            //Prints cookies
            /*for (HttpCookie cookie3 : cookies) {
                String setCookie = new StringBuilder(cookie3.toString())
                        .append("; domain=").append(cookie3.getDomain())
                        .append("; path=").append(cookie3.getPath())
                        .toString();
                Log.v(this.getClass().getName(), "Sending java.net.CookieManager cookie: " + setCookie);
                //webCookieManager.setCookie(url, setCookie);
            }*/



            String cookie = settings.getString("session", null);
            if (cookie != null) {

                Log.v(this.getClass().getName(), "Sending cookie: " + cookie);

                conn.setRequestProperty("Cookie", cookie);
            }

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + Boundary);
            conn.setChunkedStreamingMode(Buffsize);
            conn.setInstanceFollowRedirects(false);


            dos = new DataOutputStream(
                    conn.getOutputStream());
            dos.writeBytes(Newline);
            dos.writeBytes(Newline);


        } catch (Exception e) {
            Log.v(this.getClass().getName(), e.toString());
            dos = null;
        }
        return dos;
    }


}

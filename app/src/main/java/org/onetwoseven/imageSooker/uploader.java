package org.onetwoseven.imageSooker;


import java.io.InputStream;

import java.util.ArrayList;

import java.net.CookieHandler;
import java.net.CookieManager;




import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class uploader extends Activity {
    public final static String CAPTION = "org.onetwoseven.imageSooker.CAPTION";
    Intent intent;
    Bundle extras;
    String action;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);


        // setContentView(R.layout.main);

        intent = getIntent();
        extras = intent.getExtras();
        action = intent.getAction();
//hmm
        Intent login = new Intent(this, Login.class);

        startActivityForResult(login, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            boolean lastPass = false;
            if (Intent.ACTION_SEND.equals(action)) {
                if (extras.containsKey(Intent.EXTRA_STREAM)) {
                    try {
                        Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                        if (extras.containsKey(CAPTION)) {
                            if (!extras.getString(CAPTION).isEmpty()) {
                                Log.v(this.getClass().getName(), "caption:" + extras.getString(CAPTION) + "#");
                                new AsyncUpload().execute(lastPass, uri, extras.getString(CAPTION));
                            } else {
                                new AsyncUpload().execute(lastPass, uri);

                            }
                        } else {
                            new AsyncUpload().execute(lastPass, uri);
                        }
                        finish();
                        return;

                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.toString());

                    }
                }
            }
            if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                if (extras.containsKey(Intent.EXTRA_STREAM)) {
                    try {
                        ArrayList<Parcelable> list =
                                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                        int pass = 0;
                        int len = list.size();

                        for (Parcelable p : list) {
                            lastPass = pass + 1 == len;


                            pass = pass + 1;
                            Uri uri = (Uri) p;
                            if (extras.containsKey(CAPTION)) {
                                if (!extras.getString(CAPTION).isEmpty()) {
                                    String cap = extras.getString(CAPTION);
                                    cap = cap + "_" + Integer.toString(pass);
                                    new AsyncUpload().execute(lastPass, uri, cap);
                                } else {
                                    new AsyncUpload().execute(lastPass, uri);
                                }

                            } else {
                                new AsyncUpload().execute(lastPass, uri);
                            }
                        }
                        finish();
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.toString());
                    }
                }
            }

        }


    }


    public class AsyncUpload extends AsyncTask<Object, Integer, String[]> {
        private final ProgressDialog dialog = new ProgressDialog(uploader.this);

        NotificationCompat.Builder mBuilder;
        NotificationManager mNotifyManager;

        @Override
        protected void onPreExecute() {
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(uploader.this)
                    .setSmallIcon(R.drawable.ic_stat_upload)
                    .setContentTitle("Image Uploading...")
                    .setContentText("Starting...");

            mNotifyManager.notify(0, mBuilder.build());

        }

        @Override
        protected String[] doInBackground(Object... data) {
            boolean lastPass = (Boolean) data[0];
            Uri myUri = (Uri) data[1];
            Log.v(this.getClass().getName(),
                    "myUri.getpath: " + myUri.getPath());
            String result = "";
            String imageUrl = "";

            try {

                ContentResolver cr = getContentResolver();
                InputStream is = cr.openInputStream(myUri);

                String mime = cr.getType(myUri);
                String name = "";
                int size = 0;
                String[] proj = {MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.SIZE};
                Cursor mc = cr.query(myUri, proj, null, null, null);
                if (mc != null) {
                    try {
                        if (mc.moveToFirst()) {
                            name = mc.getString(0);
                            size = mc.getInt(1);
                        }

                    } finally {
                        mc.close();
                    }
                }else{
                    name = myUri.getPath();

                }
                String myCaption = null;
                if (data.length > 2) {
                    myCaption = (String) data[2];
                }

                Log.v(this.getClass().getName(),
                        "File size: " + Integer.toString(size) + " Mime: "
                                + mime);

                mBuilder.setContentTitle("Uploading " + (myCaption != null ? myCaption : name));

                SharedPreferences settings = getSharedPreferences("store", 0);

                Log.v(this.getClass().getName(), "Trying caption: " + myCaption);
                // Upload file

                uploadResults results = new Network(getBaseContext()).uploadImage(is, name, size, mime,
                        myCaption, this);

                int statusCode = results.statusCode;

                if (statusCode == 200) {
                    result = "Something went sideways :(";

//					SharedPreferences.Editor edit1 = settings.edit();
//					edit1.clear();
//					edit1.commit();

                } else if (statusCode == 302) {
                    result = "Image Uploaded to: " + results.imgLoc;
//					SharedPreferences.Editor edit1 = settings.edit();
//					edit1.clear();
//					edit1.commit();
                    imageUrl = results.imgLoc;
                } else {
                    result = "Got " + Integer.toString(statusCode) + " code";
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.v(this.getClass().getName(), e.getStackTrace().toString());
                result = "Something went wrong :(";
            }

            return new String[]{result, imageUrl};
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            try {
                //Log.v(this.getClass().getName(), Integer.toString(values[0]));
                mBuilder.setProgress(100, values[0], false);
                mBuilder.setContentText(Integer.toString(values[0]) + "%");
                mNotifyManager.notify(0, mBuilder.build());
            } catch (Exception e) {

                Log.v(this.getClass().getName(),
                        "Values is empty? " + e.toString());
            }
            // Update percentage
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            mBuilder.setContentText(result[0]);
            mBuilder.setProgress(0, 0, false);
            if (result[1] != "") {
                mBuilder.setContentTitle("Done");

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(result[1]));

                PendingIntent pI = PendingIntent.getActivity(uploader.this, 0,
                        i, PendingIntent.FLAG_ONE_SHOT);
                mBuilder.setContentIntent(pI);
            } else {
                mBuilder.setContentTitle("");
            }
            mBuilder.setAutoCancel(true);
            mNotifyManager.notify(0, mBuilder.build());
            // Toast.makeText(uploader.this,
            // result[0],Toast.LENGTH_LONG).show();
        }

        public void myPublish(int progress) {
            publishProgress(progress);
        }

        class uploadResults {
            public final int statusCode;
            public final String imgLoc;

            public uploadResults(int a, String b) {
                this.statusCode = a;
                this.imgLoc = b;
            }

        }


    }

}

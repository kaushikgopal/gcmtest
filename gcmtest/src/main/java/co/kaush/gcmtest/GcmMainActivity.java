package co.kaush.gcmtest;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Main UI for the test app.
 */
public class GcmMainActivity extends Activity {

  static final String TAG = "GcmMainActivity";
  String regid;
  GoogleCloudMessaging gcm;
  AtomicInteger msgId = new AtomicInteger();

  TextView mDisplay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    mDisplay = (TextView) findViewById(R.id.display);

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, new PlaceholderFragment())
          .commit();
    }

    // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
    if (GcmUtils.checkPlayServices(this)) {
      gcm = GoogleCloudMessaging.getInstance(this);
      regid = GcmUtils.getRegistrationId(this);

      if (regid.isEmpty()) {
        registerInBackground();
      }
    } else {
      Log.i(TAG, "No valid Google Play Services APK found.");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.gcmmain, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (item.getItemId()) {
      case R.id.action_settings:
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  // Send an upstream message.
  public void onClick(final View view) {

    if (view == findViewById(R.id.send)) {
      new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
          String msg = "";
          try {
            Bundle data = new Bundle();
            data.putString("my_message", "Hello World");
            data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
            String id = Integer.toString(msgId.incrementAndGet());
            gcm.send(GcmUtils.GCM_SENDER_ID + "@gcm.googleapis.com", id, data);
            msg = "Sent message";
          } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
          }
          return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
          mDisplay = (TextView) findViewById(R.id.display);
          mDisplay.append(msg + "\n");
        }
      }.execute(null, null, null);
    } else if (view == findViewById(R.id.clear)) {
      mDisplay.setText("");
    }

  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      return rootView;
    }
  }

  /**
   * Registers the application with GCM servers asynchronously.
   * <p>
   * Stores the registration ID and the app versionCode in the application's
   * shared preferences.
   */
  private void registerInBackground() {
    new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        String msg = "";
        try {
          if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
          }
          regid = gcm.register(GcmUtils.GCM_SENDER_ID);
          msg = "Device registered, registration ID=" + regid;

          // You should send the registration ID to your server over HTTP, so it
          // can use GCM/HTTP or CCS to send messages to your app.
          sendRegistrationIdToBackend();

          // For this demo: we don't need to send it because the device will send
          // upstream messages to a server that echo back the message using the
          // 'from' address in the message.

          // Persist the regID - no need to register again.
          GcmUtils.storeRegistrationId(GcmMainActivity.this, regid);

        } catch (IOException ex) {
          msg = "Error :" + ex.getMessage();
          // If there is an error, don't just keep trying to register.
          // Require the user to click a button again, or perform
          // exponential back-off.
        }
        return msg;
      }

      @Override
      protected void onPostExecute(String msg) {
        mDisplay.append(msg + "\n");
      }
    }.execute(null, null, null);
  }


  /**
   * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
   * messages to your app. Not needed for this demo since the device sends upstream messages
   * to a server that echoes back the message using the 'from' address in the message.
   */
  private void sendRegistrationIdToBackend() {
    // Your implementation here.
    Log.d(TAG, "The registration ID is " + regid);
  }

}

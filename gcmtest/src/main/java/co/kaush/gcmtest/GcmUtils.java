package co.kaush.gcmtest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;
import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;
import static com.google.android.gms.common.GooglePlayServicesUtil.isUserRecoverableError;

public class GcmUtils {

  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  private static final String TAG = "GCMUtils";


  public static final String GCM_SENDER_ID = "122551869219";

  // push these to an enum in GlobalUtils
  public static final String PROPERTY_REG_ID = "registration_id";
  public static final String PROPERTY_APP_VERSION = "appVersion";


  /**
   * Check the device to make sure it has the Google Play Services APK. If
   * it doesn't, display a dialog that allows users to download the APK from
   * the Google Play Store or enable it in the device's system settings.
   */
  public static boolean checkPlayServices(Activity activity) {
    int resultCode = isGooglePlayServicesAvailable(activity);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (isUserRecoverableError(resultCode)) {
        getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST)
            .show();
      } else {
        Log.i(TAG, "This device is not supported.");
        activity.finish();
      }
      return false;
    }
    return true;
  }

  /**
   * Gets the current registration ID for application on GCM service, if there is one.
   * <p>
   * If result is empty, the app needs to register.
   *
   * @return registration ID, or empty string if there is no existing
   *         registration ID.
   */
  public static String getRegistrationId(Activity activity) {
    final SharedPreferences prefs = getGcmPreferences(activity);
    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
    if (registrationId.isEmpty()) {
      Log.i(TAG, "Registration not found.");
      return "";
    }

    // Check if app was updated; if so, it must clear the registration ID
    // since the existing regID is not guaranteed to work with the new
    // app version.
    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    int currentVersion = getAppVersion(activity);
    if (registeredVersion != currentVersion) {
      Log.i(TAG, "App version changed.");
      return "";
    }
    return registrationId;
  }

  /**
   * Stores the registration ID and the app versionCode in the application's
   * {@code SharedPreferences}.
   *
   * @param activity application's context.
   * @param regId registration ID
   */
  public static void storeRegistrationId(Activity activity, String regId) {
    final SharedPreferences prefs = getGcmPreferences(activity);
    int appVersion = getAppVersion(activity.getApplicationContext());
    Log.i(TAG, "Saving regId on app version " + appVersion);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PROPERTY_REG_ID, regId);
    editor.putInt(PROPERTY_APP_VERSION, appVersion);
    editor.commit();
  }

  /**
   * @return Application's {@code SharedPreferences}.
   */
  private static SharedPreferences getGcmPreferences(Activity activity) {
    // This sample app persists the registration ID in shared preferences, but
    // how you store the regID in your app is up to you.
    return activity.getSharedPreferences(activity.getClass().getSimpleName(),
        Context.MODE_PRIVATE);
  }

  // Push this to Global Utils

  /**
   * @return Application's version code from the {@code PackageManager}.
   */
  private static int getAppVersion(Context context) {
    try {
      PackageInfo packageInfo = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0);
      return packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      // should never happen
      throw new RuntimeException("Could not get package name: " + e);
    }
  }

}

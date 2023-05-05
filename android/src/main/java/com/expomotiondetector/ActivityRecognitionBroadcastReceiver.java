package com.expomotiondetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
  private static final String TAG = "ActivityRecognitionBR";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (ActivityTransitionResult.hasResult(intent)) {
      ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
      ReactContext reactContext = ExpoMotionDetectorModule.getReactAppContext();
      int activityType = intent.getIntExtra("activityType", -1);

      for (ActivityTransitionEvent event : result.getTransitionEvents()) {
        if (event.getActivityType() == activityType) {
          String eventName = event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ? "onStartMotion"
            : "onStopMotion";

          Log.d(TAG, "Sending event: " + eventName);
          reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, null);

        }
      }
    }
  }
}

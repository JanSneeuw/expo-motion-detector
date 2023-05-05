package com.expomotiondetector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@ReactModule(name = ExpoMotionDetectorModule.NAME)
public class ExpoMotionDetectorModule extends ReactContextBaseJavaModule {
  public static final String NAME = "ExpoMotionDetector";
  private static ReactApplicationContext context;
  private ActivityRecognitionClient activityRecognitionClient;
  private Timer timer;


  public ExpoMotionDetectorModule(ReactApplicationContext reactContext) {
    super(reactContext);
    context = reactContext;
    this.activityRecognitionClient = ActivityRecognition.getClient(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void startActivityRecognition(int activityType, Promise promise) {
    Context context = getReactApplicationContext();
    Intent serviceIntent = new Intent(context, DrivingDetectionService.class);
    serviceIntent.putExtra("activityType", activityType);
    ContextCompat.startForegroundService(context, serviceIntent);
    promise.resolve(null);
  }

  @ReactMethod
  public void stopActivityRecognition(Promise promise) {
    Intent serviceIntent = new Intent(getReactApplicationContext(), DrivingDetectionService.class);
    getReactApplicationContext().stopService(serviceIntent);
  }

  @ReactMethod
  public void startMockedActivity(Integer interval, String mockActivity, Promise promise) {
    if (timer != null) {
      timer.cancel();
    }

    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("activityType", mockActivity);

        getReactApplicationContext()
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("onStartMotion", eventData);
      }
    }, 0, interval);

    promise.resolve("Mocked activity started");
  }

  @ReactMethod
  public void stopMockedActivity(Promise promise) {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
    promise.resolve("Mocked activity stopped");
  }



  @Override
  public @Nullable Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("IN_VEHICLE", ExpoMotion.IN_VEHICLE);
    constants.put("ON_BICYCLE", ExpoMotion.ON_BICYCLE);
    constants.put("ON_FOOT", ExpoMotion.ON_FOOT);
    constants.put("RUNNING", ExpoMotion.RUNNING);
    constants.put("STILL", ExpoMotion.STILL);
    constants.put("TILTING", ExpoMotion.TILTING);
    constants.put("WALKING", ExpoMotion.WALKING);
    constants.put("UNKNOWN", ExpoMotion.UNKNOWN);
    return constants;
  }

  private PendingIntent getPendingIntent() {
    Intent intent = new Intent(getReactAppContext(), ActivityRecognitionBroadcastReceiver.class);
    int flags = PendingIntent.FLAG_UPDATE_CURRENT;

    // Add FLAG_IMMUTABLE flag for Android 12 (API level 31) or higher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      flags |= PendingIntent.FLAG_MUTABLE;
    }

    return PendingIntent.getBroadcast(getReactAppContext(), 0, intent, flags);
  }

  public static ReactApplicationContext getReactAppContext() {
    return context;
  }

  public static class DrivingDetectionService extends Service {
    public static final String CHANNEL_ID = "DrivingDetectionServiceChannel";
    private int activityType;

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public void onCreate() {
      super.onCreate();
      createNotificationChannel();
      //Notification notification = createNotification();
      //startForeground(1, notification);
    }

    private void createNotificationChannel() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel serviceChannel = new NotificationChannel(
          CHANNEL_ID,
          "Driving Detection Service Channel",
          NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
          manager.createNotificationChannel(serviceChannel);
        }
      }
    }

    private Notification createNotification() {
      return new NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Motion detection")
        .setContentText("Monitoring motion activity in the background for: " + activityType)
        .setSmallIcon(android.R.drawable.ic_notification_overlay) // Replace this with your app's launcher icon
        .build();
    }

    private PendingIntent getPendingIntent() {
      Intent intent = new Intent(getApplicationContext(), ActivityRecognitionBroadcastReceiver.class);
      int flags = PendingIntent.FLAG_UPDATE_CURRENT;

      intent.putExtra("activityType", activityType);

      // Add FLAG_IMMUTABLE flag for Android 12 (API level 31) or higher
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        flags |= PendingIntent.FLAG_MUTABLE;
      }

      return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, flags);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      List<ActivityTransition> transitions = new ArrayList<>();

      activityType = intent.getIntExtra("activityType", DetectedActivity.UNKNOWN);

      Notification notification = createNotification();
      startForeground(1, notification);

      transitions.add(new ActivityTransition.Builder()
        .setActivityType(activityType)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        .build());

      transitions.add(new ActivityTransition.Builder()
        .setActivityType(activityType)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        .build());

      ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

      Task<Void> task = ActivityRecognition.getClient(getApplicationContext())
        .requestActivityTransitionUpdates(request, getPendingIntent());

      task.addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
          Log.i("ExpoMotionDetector", "Successfully registered motion detector.");
        }
      });

      task.addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Log.e("ExpoMotionDetector", "Failed to register for motion detection updates.", e);
        }
      });

      return START_STICKY;
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      // Remove activity transition updates
      ActivityRecognition.getClient(getApplicationContext())
        .removeActivityTransitionUpdates(getPendingIntent())
        .addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            Log.i("ExpoMotionDetector", "Successfully unregistered for motion detection updates.");
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Log.e("ExpoMotionDetector", "Failed to unregister for motion detection updates.", e);
          }
        });
    }

  }


}

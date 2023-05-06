import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'expo-motion-detector' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ExpoMotionDetector = NativeModules.ExpoMotionDetector
  ? NativeModules.ExpoMotionDetector
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function startActivityRecognition(activityType: any) {
  return ExpoMotionDetector.startActivityRecognition(activityType);
}

export function stopActivityRecognition() {
  return ExpoMotionDetector.stopActivityRecognition();
}

export function startMockedActivity(
  interval: number,
  activityType: any,
  errorCallback: any,
  successCallback: any
) {
  if (Platform.OS === 'ios') {
    return ExpoMotionDetector.startMockedActivity(
      interval,
      activityType,
      errorCallback,
      successCallback
    );
  } else if (Platform.OS === 'android') {
    return ExpoMotionDetector.startMockedActivity(interval, activityType)
      .then(successCallback)
      .catch(errorCallback);
  }
}

export function stopMockedActivity() {
  return ExpoMotionDetector.stopMockedActivity();
}

export function requestMotionPermission(): Promise<{ status: string }> {
  if (Platform.OS === 'android') {
    return ExpoMotionDetector.requestMotionPermission();
  } else {
    console.warn('requestMotionPermission is only available on Android');
    return Promise.resolve({ status: 'granted' });
  }
}

const getActivityTypes = () => {
  if (Platform.OS === 'ios') {
    return {
      AUTOMOTIVE: 'automotive',
      CYCLING: 'cycling',
      RUNNING: 'running',
      STATIONARY: 'stationary',
      UNKNOWN: 'unknown',
      WALKING: 'walking',
    };
  } else {
    return {
      IN_VEHICLE: ExpoMotionDetector.IN_VEHICLE,
      ON_BICYCLE: ExpoMotionDetector.ON_BICYCLE,
      ON_FOOT: ExpoMotionDetector.ON_FOOT,
      RUNNING: ExpoMotionDetector.RUNNING,
      STILL: ExpoMotionDetector.STILL,
      TILTING: ExpoMotionDetector.TILTING,
      WALKING: ExpoMotionDetector.WALKING,
      UNKNOWN: ExpoMotionDetector.UNKNOWN,
    };
  }
};

export const MotionConstants = {
  Android: Platform.OS === 'android' ? getActivityTypes() : {},
  iOS: Platform.OS === 'ios' ? getActivityTypes() : {},
};


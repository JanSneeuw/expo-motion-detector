# expo-motion-detector
## BEAWARE: This package is still in development and not ready for production use
Package to detect when a user is performing a certain activity like driving. The module is compatible with the expo managed workflow, though not with Expo GO. You can create a development build to stay in the managed workflow.

## Installation

```sh
npm install expo-motion-detector
```

## Usage
First get the appropriate permissions using your favorite permission manager. For android you need:
```xml
android.permission.ACTIVITY_RECOGNITION
```
for IOS you need:
```xml
NSMotionUsageDescription (also known as ios.permission.MOTION)
```
Then register the event listener, you can add a listener for onStartMotion and onStopMotion:
```tsx
import {
  DeviceEventEmitter,
} from 'react-native';

const onMotionDetected = async () => {
  // Do something when motion is detected
};
DeviceEventEmitter.addListener('onStartMotion', onMotionDetected);
```

Then import the package and use it like this:
```tsx
import {
  startActivityRecognition,
  MotionConstants,
} from 'expo-motion-detector';

startActivityRecognition(MotionConstants.Android.WALKING).then((result) => {
    console.log(result);
  }
);
```


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

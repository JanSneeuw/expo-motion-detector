import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  PermissionsAndroid,
  DeviceEventEmitter,
  Platform,
  Button,
} from 'react-native';
import {
  startActivityRecognition,
  MotionConstants,
  startMockedActivity,
} from 'expo-motion-detector';
import { PERMISSIONS, request } from 'react-native-permissions';
import notifee from '@notifee/react-native';

export default function App() {
  const [isPerformingMotion, setIsPerformingMotion] = useState(false);

  useEffect(() => {
    async function startDriving() {
      await notifee.requestPermission();

      const channelId = await notifee.createChannel({
        id: 'default',
        name: 'Default Channel',
      });

      if (Platform.OS === 'android') {
        try {
          const granted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.ACTIVITY_RECOGNITION,
            {
              title: 'Activity Recognition Permission',
              message:
                'Your app needs access to your activity recognition data.',
              buttonNeutral: 'Ask Me Later',
              buttonNegative: 'Cancel',
              buttonPositive: 'OK',
            }
          );
          if (granted === PermissionsAndroid.RESULTS.GRANTED) {
            console.log('You can access activity recognition data');
            startActivityRecognition(MotionConstants.Android.WALKING).then((result) => {
                console.log(result);
              }
            );
          } else {
            console.log('Activity recognition permission denied');
          }
        } catch (err) {
          console.warn(err);
        }
      } else if (Platform.OS === 'ios') {
        const permissionResult = await request(PERMISSIONS.IOS.MOTION);
        if (permissionResult !== 'granted') {
          console.log('Permission to access motion data was denied');
          return;
        }
        startActivityRecognition(MotionConstants.iOS.WALKING).then((result) => {
          console.log(result);
        });
      }

      const onMotionDetected = async () => {
        await notifee.displayNotification({
          title: 'You are Walking!',
          body: 'You seem to have started walking.',
          android: {
            channelId,
            smallIcon: 'ic_launcher',
            pressAction: {
              id: 'default',
            },
          },
        });
        setIsPerformingMotion(true);
      };
      DeviceEventEmitter.addListener('onStartMotion', onMotionDetected);
    }
    startDriving();
  }, []);

  function startWalking() {
    const interval = 1000;
    const mockActivity = 'walking';
    startMockedActivity(
      interval,
      mockActivity,
      (error) => {
        if (error) {
          console.error('Error starting mocked activity:', error);
        } else {
          console.log('Mocked activity started');
        }
      },
      (result) => {
        console.log(result);
      }
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.text}>
        {isPerformingMotion ? 'Is performing' : 'Is not performing'}
      </Text>
      <Button title={'startWalking'} onPress={startWalking} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'black',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  text: {
    color: 'white',
  },
});

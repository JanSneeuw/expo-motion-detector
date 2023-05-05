const { withAndroidManifest, withInfoPlist } = require('@expo/config-plugins');

const withMotionDetector = (config) => {
  // Android configurations
  config = withAndroidManifest(config, (conf) => {
    const usesPermissions = conf.modResults['uses-permission'] || [];

    const activityRecognitionPermission = {
      $: {
        'android:name': 'android.permission.ACTIVITY_RECOGNITION',
      },
    };

    if (!usesPermissions.some((permission) => permission.$["android:name"] === "android.permission.ACTIVITY_RECOGNITION")) {
      usesPermissions.push(activityRecognitionPermission);
    }

    conf.modResults['uses-permission'] = usesPermissions;
    return conf;
  });

  // iOS configurations
  config = withInfoPlist(config, (conf) => {
    conf.modResults.UIBackgroundModes = ['location'];
    conf.modResults.NSMotionUsageDescription = 'We need to track your motion.';
    return conf;
  });

  return config;
};

module.exports = withMotionDetector;

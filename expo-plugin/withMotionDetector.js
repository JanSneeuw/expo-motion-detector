const {
  withAndroidManifest,
  withInfoPlist,
  AndroidConfig,
} = require('@expo/config-plugins');

const withMotionDetector = (config) => {
  // Android configurations
  config = withAndroidManifest(config, (config) => {
    config.modResults = addActivityRecognitionPermissionToManifest(config.modResults);
    return config;
  });

  // iOS configurations
  config = withInfoPlist(config, (conf) => {
    conf.modResults.UIBackgroundModes = ['location'];
    conf.modResults.NSMotionUsageDescription = 'We need to track your motion.';
    return conf;
  });

  return config;
};

function addActivityRecognitionPermissionToManifest(androidManifest) {
  if (!Array.isArray(androidManifest.manifest['uses-permission'])) {
    androidManifest.manifest['uses-permission'] = [];
  }

  if (
    !androidManifest.manifest['uses-permission'].some(
      (permission) => permission.$['android:name'] === 'android.permission.ACTIVITY_RECOGNITION',
    )
  ) {
    androidManifest.manifest['uses-permission'].push({
      $: {
        'android:name': 'android.permission.ACTIVITY_RECOGNITION',
      },
    });
  }

  return androidManifest;
}

module.exports = withMotionDetector;

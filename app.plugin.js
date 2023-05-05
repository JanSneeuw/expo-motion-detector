const { createRunOncePlugin } = require('@expo/config-plugins');
const withBeaconRadar = require('./expo-plugin/withMotionDetector');

// A helper function to ensure the plugin is only run once per config
const withRunOnceMotionDetector = createRunOncePlugin(
  withBeaconRadar,
  'expo-motion-detector'
);

module.exports = (config) => {
  return withRunOnceMotionDetector(config);
};

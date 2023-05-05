#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(ExpoMotionDetector, NSObject)

RCT_EXTERN_METHOD(startActivityRecognition:(NSString *)activityType
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(stopActivityRecognition:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(getActivityTypes:(RCTPromiseResolveBlock)resolver
                  rejecter:(RCTPromiseRejectBlock)rejecter)
RCT_EXTERN_METHOD(startMockedActivity:(nonnull NSNumber *)time mockActivity:(NSString *)mockActivity errorCallback:(RCTResponseSenderBlock)errorCallback successCallback:(RCTResponseSenderBlock)successCallback)
RCT_EXTERN_METHOD(stopMockedActivity:(RCTResponseSenderBlock)callback)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end

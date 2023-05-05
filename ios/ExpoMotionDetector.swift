import Foundation
import CoreMotion
import React

@objc(ExpoMotionDetector)
class ExpoMotionDetector: NSObject, RCTBridgeModule {
  
  @objc static func moduleName() -> String {
    return "ExpoMotionDetector"
  }

  private var motionActivityManager: CMMotionActivityManager?
  private var timer: Timer?
    public var bridge: RCTBridge!

  @objc func startMockedActivity(_ interval: Double, mockActivity: String, errorCallback: @escaping RCTPromiseRejectBlock, successCallback: @escaping RCTPromiseResolveBlock) {
    DispatchQueue.main.async { [weak self] in
      self?.timer?.invalidate()
      self?.timer = Timer.scheduledTimer(withTimeInterval: interval / 1000.0, repeats: true) { _ in
          if let bridge = self?.bridge {
              bridge.eventDispatcher().sendAppEvent(withName: "onStartMotion", body: ["activityType": mockActivity])
          }
      }
      successCallback("Mocked activity started")
    }
  }

  @objc func stopMockedActivity(_ callback: RCTResponseSenderBlock) {
    timer?.invalidate()
    callback([NSNull()])
  }
    
  @objc func startActivityRecognition(_ activityType: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
    if CMMotionActivityManager.isActivityAvailable() {
      motionActivityManager = CMMotionActivityManager()
      motionActivityManager?.startActivityUpdates(to: OperationQueue.main) { activity in
        if let activity = activity {
          if activity.matchesType(activityType) {
              if let bridge = self.bridge {
                  bridge.eventDispatcher().sendAppEvent(withName: "onStartMotion", body: ["activityType": activityType])
              }
          }
        }
      }
      resolver("Motion detection started")
    } else {
      rejecter("MOTION_DETECTION_ERROR", "Motion activity detection is not available on this device", nil)
    }
  }

  @objc func stopActivityRecognition(_ resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    motionActivityManager?.stopActivityUpdates()
    resolve("Motion detection stopped")
  }

  @objc func getActivityTypes(_ resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
    let activityTypes = [
      "walking": "walking",
      "running": "running",
      "automotive": "automotive"
    ]
    resolver(activityTypes)
  }

}

extension CMMotionActivity {
  func matchesType(_ type: String) -> Bool {
    switch type {
    case "walking":
      return self.walking
    case "running":
      return self.running
    case "automotive":
      return self.automotive
    default:
      return false
    }
  }
}

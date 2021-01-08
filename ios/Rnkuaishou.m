#import "Rnkuaishou.h"

#import <React/RCTBridgeDelegate.h>
#import <UIKit/UIKit.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTBridge.h>
#import "KSApi.h"
#import "KSApiObject.h"

@implementation Rnkuaishou

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

// Example method
// See // https://reactnative.dev/docs/native-modules-ios
RCT_REMAP_METHOD(multiply,
                 multiplyWithA:(nonnull NSNumber*)a withB:(nonnull NSNumber*)b
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
  NSNumber *result = @([a floatValue] * [b floatValue]);

  resolve(result);
}

RCT_REMAP_METHOD(registerApp,
                 :(nonnull NSString*)appId
                 :(nonnull NSString*)universalLink
                 :(RCTPromiseResolveBlock)resolve
                 :(RCTPromiseRejectBlock)reject)
{
    BOOL res = [KSApi registerApp:appId universalLink:universalLink delegate:self];
    if(res){
        resolve(@"ok");
    } else {
        resolve(@"fail");
    }
}

RCT_REMAP_METHOD(ksauth,
                 :(nonnull NSString*)loginType
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    Rnkuaishou *thiz = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [UIApplication sharedApplication].keyWindow.rootViewController;
        KSAuthRequest *req = [[KSAuthRequest alloc] init];
        req.scope = @"user_info"; //,user_base";
        req.h5AuthViewController = vc;
        [KSApi sendRequest:req completion:^(BOOL result){
            if(result) {
                resolve(@"ok");
            } else {
                resolve(@"fail");
            }
        }];
    });
}

/// 发送一个request后，收到快手终端的回应
/// @param response 具体的回应内容，回应类型详见KSApiObject.h
- (void)ksApiDidReceiveResponse:(__kindof KSBaseResponse *)response{
    @try{
        KSAuthResponse *r = (KSAuthResponse*) response;
        NSMutableDictionary *body = @{@"type": @"SendAuth.Resp"}.mutableCopy;
        body[@"authCode"] = r.code;
        [self.bridge.eventDispatcher sendDeviceEventWithName:KuaiShouEventName body:body];
    }
    @catch (NSException *exception)
    {
      // Print exception information
      NSLog( @"NSException caught" );
      NSLog( @"Name: %@", exception.name);
      NSLog( @"Reason: %@", exception.reason );
      return;
    }
    @finally
    {
      // Cleanup, in both success and fail cases
      NSLog( @"In finally block");
    }
}
@end

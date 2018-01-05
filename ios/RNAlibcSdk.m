
#import "RNAlibcSdk.h"
#import "AlibcSdkBridge.h"

#import <AlibcTradeSDK/AlibcTradeSDK.h>
#import <AlibabaAuthSDK/ALBBSDK.h>
#import <React/RCTLog.h>


#define NOT_LOGIN (@"not login")

@implementation RNAlibcSdk

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initTae: (RCTResponseSenderBlock)callback)
{
    [[AlibcSdkBridge sharedInstance] initTae:callback];
}

RCT_EXPORT_METHOD(showLogin: (RCTResponseSenderBlock)callback)
{
    [[AlibcSdkBridge sharedInstance] showLogin:callback];
}
RCT_EXPORT_METHOD(isLogin: (RCTResponseSenderBlock)callback)
{
    [[AlibcSdkBridge sharedInstance] isLogin:callback];
}

RCT_EXPORT_METHOD(getUserInfo: (RCTResponseSenderBlock)callback)
{
    [[AlibcSdkBridge sharedInstance] getUserInfo:callback];
}

RCT_EXPORT_METHOD(logout: (RCTResponseSenderBlock)callback)
{
    [[AlibcSdkBridge sharedInstance] logout:callback];
}

RCT_EXPORT_METHOD(show: (NSDictionary *)param callback: (RCTResponseSenderBlock)callback){
    [[AlibcSdkBridge sharedInstance] show:param callback:callback];
}


@end
  

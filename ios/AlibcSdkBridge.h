//
//  AlibcSdkBridge.h
//  RNAlibcSdk
//
//  Created by IORI on 17/4/18.
//  Copyright © 2017年 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <Foundation/Foundation.h>
#import <AlibabaAuthSDK/ALBBSDK.h>
#import <AlibcTradeSDK/AlibcTradeSDK.h>
#import <UIKit/UIKit.h>

@interface AlibcSdkBridge : NSObject
+ (instancetype)sharedInstance;
- (void)initTae: (RCTResponseSenderBlock)callback;
- (void)showLogin: (RCTResponseSenderBlock)callback;
- (void)isLogin: (RCTResponseSenderBlock)callback;
- (void)getUserInfo: (RCTResponseSenderBlock)callback;
- (void)logout: (RCTResponseSenderBlock)callback;
- (void)show: (NSDictionary *)param callback: (RCTResponseSenderBlock)callback;
- (void)showInWebView: (UIWebView *)webView param:(NSDictionary *)param;
@end

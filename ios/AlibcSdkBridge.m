//
//  AlibcSdkBridge.m
//  RNAlibcSdk
//
//  Created by IORI on 17/4/18.
//  Copyright © 2017年 Facebook. All rights reserved.
//

#import "AlibcSdkBridge.h"
#import "AlibcWebView.h"
#import <React/RCTLog.h>

#define NOT_LOGIN (@"not login")

@implementation AlibcSdkBridge {
    AlibcTradeTaokeParams *taokeParams;
    AlibcTradeShowParams *showParams;
}

+ (instancetype) sharedInstance
{
    static AlibcSdkBridge *instance = nil;
    if (!instance) {
        instance = [[AlibcSdkBridge alloc] init];
    }
    return instance;
}

- (void)initTae: (RCTResponseSenderBlock)callback
{
    // 百川平台基础SDK初始化，加载并初始化各个业务能力插件
    [[AlibcTradeSDK sharedInstance] asyncInitWithSuccess:^{
        callback(@[[NSNull null]]);
    } failure:^(NSError *error) {
        NSDictionary *ret = @{@"code": @(error.code), @"msg":error.description};
        callback(@[ret]);
    }];

    // 初始化AlibabaAuthSDK
    [[ALBBSDK sharedInstance] ALBBSDKInit];

    // 开发阶段打开日志开关，方便排查错误信息
    //默认调试模式打开日志,release关闭,可以不调用下面的函数
    [[AlibcTradeSDK sharedInstance] setDebugLogOpen:YES];

    //设置全局的app标识，在电商模块里等同于isv_code
    [[AlibcTradeSDK sharedInstance] setISVCode:@"app"];
    
    // 设置全局配置，是否强制使用h5
    [[AlibcTradeSDK sharedInstance] setIsForceH5:NO];
}

- (void)showLogin: (RCTResponseSenderBlock)callback
{
    [[ALBBSDK sharedInstance] auth:[UIApplication sharedApplication].delegate.window.rootViewController
                   successCallback:^(ALBBSession *session) {
                       ALBBUser *s = [session getUser];
                       NSDictionary *ret = @{@"userNick": s.nick, @"avatarUrl":s.avatarUrl, @"openId":s.openId, @"isLogin":@"true"};
                       callback(@[[NSNull null], ret]);
                   }
                   failureCallback:^(ALBBSession *session, NSError *error) {
                       NSDictionary *ret = @{@"code": @(error.code), @"msg":error.description};
                       callback(@[ret]);
                   }
     ];
}

- (void)isLogin: (RCTResponseSenderBlock)callback
{
    bool isLogin = [[ALBBSession sharedInstance] isLogin];
    callback(@[[NSNull null], [NSNumber numberWithBool: isLogin]]);
}

- (void)getUserInfo: (RCTResponseSenderBlock)callback
{
    if([[ALBBSession sharedInstance] isLogin]){
        ALBBUser *s = [[ALBBSession sharedInstance] getUser];
        NSDictionary *ret = @{@"userNick": s.nick, @"avatarUrl":s.avatarUrl, @"openId":s.openId,  @"isLogin":@"true"};
        callback(@[[NSNull null], ret]);
    } else {
        callback(@[NOT_LOGIN]);
    }
}

- (void)logout: (RCTResponseSenderBlock)callback
{
    [[ALBBSDK sharedInstance] logout];
    callback(@[[NSNull null]]);
}

- (void)show: (NSDictionary *)param callback: (RCTResponseSenderBlock)callback
{
    NSString *type = param[@"type"];
    NSDictionary *payload = (NSDictionary *)param[@"payload"];
    
    id<AlibcTradePage> page;
    if ([type isEqualToString:@"detail"]) {
        page = [AlibcTradePageFactory itemDetailPage:(NSString *)payload[@"itemid"]];
    } else if ([type isEqualToString:@"url"]) {
        page = [AlibcTradePageFactory page:(NSString *)payload[@"url"]];
    } else if ([type isEqualToString:@"shop"]) {
        page = [AlibcTradePageFactory shopPage:(NSString *)payload[@"shopid"]];
    } else if ([type isEqualToString:@"orders"]) {
        page = [AlibcTradePageFactory myOrdersPage:[payload[@"orderStatus"] integerValue] isAllOrder:[payload[@"allOrder"] boolValue]];
    } else if ([type isEqualToString:@"addCard"]) {
        page = [AlibcTradePageFactory addCartPage:(NSString *)payload[@"itemid"]];
    } else if ([type isEqualToString:@"mycard"]) {
        page = [AlibcTradePageFactory myCartsPage];
    } else {
        RCTLog(@"not implement");
        return;
    }
    [self _show:page param:param callback:callback];
}

- (void)_show: (id<AlibcTradePage>)page param:(NSDictionary *)param callback: (RCTResponseSenderBlock)callback
{
    //处理参数
    NSDictionary* result = [self dealParam:param];
    AlibcTradeTaokeParams *taokeParams = [[AlibcTradeTaokeParams alloc] init];
    taokeParams = result[@"taokeParams"];
    AlibcTradeShowParams* showParams = [[AlibcTradeShowParams alloc] init];
    showParams = result[@"showParams"];
    NSDictionary *trackParam = result[@"trackParam"];
    
    id<AlibcTradeService> service = [AlibcTradeSDK sharedInstance].tradeService;
    [service
     show:[UIApplication sharedApplication].delegate.window.rootViewController
     page:page
     showParams:showParams
     taoKeParams:taokeParams
     trackParam:trackParam
     tradeProcessSuccessCallback:^(AlibcTradeResult * _Nullable result) {
         if (result.result == AlibcTradeResultTypeAddCard) {
             NSDictionary *ret = @{@"type": @"card"};
             callback(@[[NSNull null], ret]);
         } else if (result.result == AlibcTradeResultTypePaySuccess) {
             NSDictionary *ret = @{@"type": @"pay", @"orders": result.payResult.paySuccessOrders};
             callback(@[[NSNull null], ret]);
         }
     } tradeProcessFailedCallback:^(NSError * _Nullable error) {
         NSDictionary *ret = @{@"type": @"error", @"code": @(error.code), @"msg":error.description};
         callback(@[ret]);
     }];
}

- (void)showInWebView: (AlibcWebView *)webView param:(NSDictionary *)param
{
    NSString *type = param[@"type"];
    NSDictionary *payload = (NSDictionary *)param[@"payload"];
    
    id<AlibcTradePage> page;
    if ([type isEqualToString:@"detail"]) {
        page = [AlibcTradePageFactory itemDetailPage:(NSString *)payload[@"itemid"]];
    } else if ([type isEqualToString:@"url"]) {
        page = [AlibcTradePageFactory page:(NSString *)payload[@"url"]];
    } else if ([type isEqualToString:@"shop"]) {
        page = [AlibcTradePageFactory shopPage:(NSString *)payload[@"shopid"]];
    } else if ([type isEqualToString:@"orders"]) {
        page = [AlibcTradePageFactory myOrdersPage:[payload[@"orderStatus"] integerValue] isAllOrder:[payload[@"allOrder"] boolValue]];
    } else if ([type isEqualToString:@"addCard"]) {
        page = [AlibcTradePageFactory addCartPage:(NSString *)payload[@"itemid"]];
    } else if ([type isEqualToString:@"mycard"]) {
        page = [AlibcTradePageFactory myCartsPage];
    } else {
        RCTLog(@"not implement");
        return;
    }
    [self _showInWebView:webView page:page param:param];
}

- (void)_showInWebView: (UIWebView *)webView page:(id<AlibcTradePage>)page param:(NSDictionary *)param
{
    //处理参数
    NSDictionary* result = [self dealParam:param];
    AlibcTradeTaokeParams *taokeParams = [[AlibcTradeTaokeParams alloc] init];
    taokeParams = result[@"taokeParams"];
    AlibcTradeShowParams* showParams = [[AlibcTradeShowParams alloc] init];
    showParams = result[@"showParams"];
    NSDictionary *trackParam = result[@"trackParam"];
    
    id<AlibcTradeService> service = [AlibcTradeSDK sharedInstance].tradeService;
    [service
     show:[UIApplication sharedApplication].delegate.window.rootViewController
     webView:webView
     page:page
     showParams:showParams
     taoKeParams:taokeParams
     trackParam:trackParam
     tradeProcessSuccessCallback:^(AlibcTradeResult * _Nullable result) {
         if (result.result == AlibcTradeResultTypeAddCard) {
             ((AlibcWebView *)webView).onTradeResult(@{
                                     @"type": @"card",
                                     });
         } else if (result.result == AlibcTradeResultTypePaySuccess) {
             ((AlibcWebView *)webView).onTradeResult(@{
                                     @"type": @"pay",
                                     @"orders": result.payResult.paySuccessOrders,
                                     });
         }
     } tradeProcessFailedCallback:^(NSError * _Nullable error) {
         ((AlibcWebView *)webView).onTradeResult(@{
                                 @"type": @"error",
                                 @"code": @(error.code),
                                 @"msg": error.description,
                                 });
     }];
}

/****---------------以下是公用方法----------------**/
//公用参数处理
- (NSDictionary *)dealParam:(NSDictionary *)param
{
    NSDictionary *payload = (NSDictionary *)param[@"payload"];
    
    NSString *mmPid = @"mm_23448739_15832573_60538822";
    NSString *isvcode=@"app";
    NSString *adzoneid=@"60538822";
    NSString *tkkey=@"23482513";
    
    AlibcTradeTaokeParams *taokeParam = [[AlibcTradeTaokeParams alloc] init];
    if ((NSString *)payload[@"mmpid"]!=nil) {
        mmPid=(NSString *)payload[@"mmpid"];
    }
    
    if ((NSString *)payload[@"adzoneid"]!=nil) {
        adzoneid=(NSString *)payload[@"adzoneid"];
    }
    
    if ((NSString *)payload[@"tkkey"]!=nil) {
        tkkey=(NSString *)payload[@"tkkey"];
    }
    
    [taokeParam setPid:mmPid];
    [taokeParam setAdzoneId:adzoneid];
    taokeParam.extParams=@{@"taokeAppkey":tkkey};
    
    AlibcTradeShowParams* showParam = [[AlibcTradeShowParams alloc] init];
    if ((NSString *)payload[@"opentype"]!=nil) {
        if([(NSString *)payload[@"opentype"] isEqual:@"html5"]){
            showParam.openType = AlibcOpenTypeH5;
        }else{
            showParam.openType = AlibcOpenTypeNative;
        }
    }else{
        showParam.openType = AlibcOpenTypeAuto;
    }
    //新版加入，防止唤醒手淘app的时候打开h5
    showParam.linkKey=@"taobao";
    
    if ((NSString *)payload[@"isvcode"]!=nil) {
        isvcode=(NSString *)payload[@"isvcode"];
    }
    NSDictionary *trackParam=@{@"isv_code":isvcode};
    //返回处理后的参数
    return @{@"showParams":showParam,@"taokeParams":taokeParam,@"trackParam":trackParam};
}



@end

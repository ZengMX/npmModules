//
//  RCTAliPay.m
//  RCTAliPay
//
//  Created by imall on 16/8/16.
//  Copyright © 2016年 imall. All rights reserved.
//

#import "RCTAliPay.h"
#import "RCTEventDispatcher.h"
#import <UIKit/UIKit.h>
#import <AlipaySDK/AlipaySDK.h>

@implementation RCTAliPay
@synthesize bridge = _bridge;
RCT_EXPORT_MODULE();
RCT_EXPORT_METHOD(pay:(id  __nonnull)map)
{
    [self noticePayResult];
    NSString *orderSpec = nil;
    NSString *signedString = nil;
    NSString *orderId = nil;
    NSString *appScheme = nil;
    //    NSDictionary *dict = map;
    if(map != nil){
        //获得订单字符串结果
        NSDictionary *dictresult = map;
        
        if (dictresult != nil){
            
            orderSpec = [dictresult objectForKey:@"orderInfo"];
            NSLog(@"[INFO] pay.orderInfo:%@",orderSpec);
            signedString = [dictresult objectForKey:@"sign"];
            NSLog(@"[INFO] pay.sign:%@",signedString);
            orderId = [dictresult objectForKey:@"orderId"];
            appScheme = [dictresult objectForKey:@"appScheme"];
            
            _trade_NO=orderId;
            
        }else{
            NSLog(@"[INFO] errorObject=%@",dictresult);
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"提示" message:@"数据错误" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil, nil];
            [alert show];
            //          [self alert:@"提示信息" msg:@"服务器异常"];
            return;
        }
        
    }else{
        //        [self alert:@"提示信息" msg:@"服务器返回错误，未获取到json对象"];
        return;
    }
    
    [NSURL URLWithString:@""];
    if (orderSpec != nil && signedString != nil && [orderSpec length] > 0 && [signedString length] > 0 && _trade_NO != nil) {
        
        signedString = [self urlEncodedString:signedString];
        
        NSLog(@"[INFO] pay.orderString:%@",signedString);
        
        NSString *orderString = [NSString stringWithFormat:@"%@&sign=\"%@\"&sign_type=\"%@\"",
                                 orderSpec, signedString, @"RSA"];
        //NSLog(@"[INFO] pay.orderString:%@",orderString);
        [[AlipaySDK defaultService] payOrder:orderString fromScheme:appScheme callback:^(NSDictionary *resultDic) {
            
            NSLog(@"[INFO] resultDic = %@",resultDic);
            
            //            callback(@[[NSNull null],[self parse:resultDic withorderno:_trade_NO]]);
            [self doEventReminderReceivedWithPayInfo:[self parse:resultDic withorderno:_trade_NO]];
            
        }];
        
    }else{
        NSLog(@"[INFO] 服务器返回订单数据或签名为空");
        //        callback(@[[NSNull null],@{@"error":@"服务器返回订单数据或签名为空"}]);
        [self doEventReminderReceivedWithPayInfo:@{@"error":@"服务器返回订单数据或签名为空"}];
        //      [self alert:@"提示信息" msg:@"签名失败"];
    }
}

- (void)noticePayResult
{
    //获取通知中心单例对象
    NSNotificationCenter * center = [NSNotificationCenter defaultCenter];
    //添加当前类对象为一个观察者，name和object设置为nil，表示接收一切通知
    [center addObserver:self selector:@selector(notice:) name:@"alipay" object:nil];
}

-(void)notice:(NSNotification *)sender{
    //NSLog(@"%@",sender);
    NSURL *url = [NSURL URLWithString:sender.userInfo[@"url"]];
    [[AlipaySDK defaultService] processOrderWithPaymentResult:url standbyCallback:^(NSDictionary *resultDic) {
        [self doEventReminderReceivedWithPayInfo:[self parse:resultDic withorderno:_trade_NO]];
    }];
}

- (void)doEventReminderReceivedWithPayInfo:(NSDictionary *)infos
{
    [self.bridge.eventDispatcher sendAppEventWithName:@"sendPlayResultMessage"
                                                 body:infos];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

//处理结果
- (id)parse:(NSDictionary *)resultD withorderno:(NSString*)order_no{
    
    
    NSString *stringstatus = resultD[@"resultStatus"];
    //NSString *status = resultD[@"resultStatus"];
    //NSLog(@"[INFO] resultStatus:%@",stringstatus);
    
    int status = [resultD[@"resultStatus"] intValue];
    
    NSLog(@"[INFO] status:%d",status);
    
    //NSNumber *number = @([status integerValue]);
    NSString *resultString = resultD[@"result"];
    
    NSLog(@"[INFO] resultString:%@",resultString);
    
    //NSMutableDictionary *result =[self Result:resultString];
    
    //NSLog(@"[INFO] result = %@",result);
    //AlixPayResult* result = [[AlixPayResult alloc] initWithString:resultString];
    
    NSDictionary *ResultStatus = @{ @"9000" : @"支付成功", @"8000" : @"等待支付结果确认",@"4000":@"系统异常",@"4001":@"数据格式不正确",@"4003":@"该用户绑定的支付宝账户被冻结或不允许支付",@"4004":@"该用户已解除绑定",@"4005":@"绑定失败或没有绑定",@"4006":@"订单支付失败",@"4010":@"重新绑定账户",@"6000":@"支付服务正在进行升级操作",@"6001":@"用户中途取消支付操作",@"7001":@"网页支付失败"};
    NSDictionary *events;
    if (status == 9000){
        
        events = [self paySuccess:@"支付成功" withStatus:stringstatus withOrderno:order_no withResult:resultString];
        
    }else if (status == 8000){
        
        events = [self payWait:@"等待支付结果确认" withStatus:stringstatus withOrderno:order_no withResult:resultString];
        
    }else{
        if ([ResultStatus objectForKey:stringstatus]) {
            events = [self payError:ResultStatus[stringstatus] withStatus:stringstatus withOrderno:order_no withResult:resultString];
        }else{
            events = [self payError:@"其他错误" withStatus:stringstatus withOrderno:order_no withResult:resultString];
        }
    }
    
    return events;
    
}


-(NSDictionary *)paySuccess:(id)statustxt withStatus:(NSString*)status withOrderno:(NSString*)orderno withResult:(NSString*)results
{
    //  [bridge.eventDispatcher sendAppEventWithName:@"EventReminder" body:@{@"success": @"success"}];
    
    NSMutableDictionary *event = [NSMutableDictionary dictionary];
    [event setObject:status forKey:@"status"];
    [event setObject:statustxt forKey:@"statustext"];
    [event setObject:orderno forKey:@"order_no"];
    [event setObject:results forKey:@"result"];
    // Fire an event directly to the specified listener (callback)
    return event;
}

-(NSDictionary *)payWait:(id)statustxt withStatus:(NSString*)status withOrderno:(NSString*)orderno withResult:(NSString*)results
{
    //  [bridge.eventDispatcher sendAppEventWithName:@"EventReminder" body:@{@"wait": @"wait"}];
    
    NSMutableDictionary *event = [NSMutableDictionary dictionary];
    [event setObject:status forKey:@"status"];
    [event setObject:statustxt forKey:@"statustext"];
    [event setObject:orderno forKey:@"order_no"];
    [event setObject:results forKey:@"result"];
    // Fire an event directly to the specified listener (callback)
    return event;
    
}

-(NSDictionary *)payError:(id)statustxt withStatus:(NSString*)status withOrderno:(NSString*)orderno withResult:(NSString*)results
{
    //  [bridge.eventDispatcher sendAppEventWithName:@"EventReminder" body:@{@"error": @"error"}];
    
    NSMutableDictionary *event = [NSMutableDictionary dictionary];
    [event setObject:status forKey:@"status"];
    [event setObject:statustxt forKey:@"statustext"];
    [event setObject:orderno forKey:@"order_no"];
    [event setObject:results forKey:@"result"];
    NSLog(@"[INFO] payError = %@",event);
    // Fire an event directly to the specified listener (callback)
    return event;
    
}


//解析返回参数中的字符串
- (NSMutableDictionary *)Result:(NSString *)resultd {
    
    NSMutableDictionary *event  = NULL;
    
    if( nil == resultd || 0 == resultd.length ){
        
        return event;
        
    }
    
    NSRange range = [resultd rangeOfString:@"&sign_type=\""];
    if (range.location == NSNotFound) {
        return event;
    }
    //self.resultString = [resultd substringToIndex:range.location];
    
    range.location += range.length;
    range.length = [resultd length] - range.location;
    NSRange range2 = [resultd rangeOfString:@"\"" options:NSCaseInsensitiveSearch range:range];
    if (range2.location == NSNotFound) {
        return event;
    }
    range.length = range2.location - range.location;
    if (range.length <= 0) {
        return event;
    }
    NSString *signType = [resultd substringWithRange:range];
    
    //
    // 签名字符串
    //
    range = [resultd rangeOfString:@"sign=\""];
    if (range.location == NSNotFound) {
        return event;
    }
    range2.location = 0;
    range2.length = range.location-1;
    NSString *resultString = [resultd substringWithRange:range2];
    
    
    
    //
    // 签名类型
    //
    range2 = [resultd rangeOfString:@"&sign_type=\""];
    
    range = [resultd rangeOfString:@"&sign=\""];
    
    
    range.location += range.length;
    range.length = range2.location - range.location-1;
    
    NSString *signString = [resultd substringWithRange:range];
    
    [event setObject:resultString forKey:@"resultString"];
    [event setObject:signType forKey:@"signType"];
    [event setObject:signString forKey:@"signString"];
    
    return event;
}

- (NSString*)urlEncodedString:(NSString *)string
{
    NSString * encodedString = (__bridge_transfer  NSString*) CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault, (__bridge CFStringRef)string, NULL, (__bridge CFStringRef)@"!*'();:@&=+$,/?%#[]", kCFStringEncodingUTF8 );
    
    return encodedString;
}



- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

@end

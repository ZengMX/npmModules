//
//  RCTAliPay.h
//  RCTAliPay
//
//  Created by imall on 16/8/16.
//  Copyright © 2016年 imall. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTBridgeModule.h"
#import "RCTBridge.h"

@interface RCTAliPay : NSObject<RCTBridgeModule>
{
    NSString *_trade_NO;
    //NSDictionary *sResultStatus;
    RCTBridge *bridge;
}

@property (nonatomic,retain)RCTBridge *bridge;

@end

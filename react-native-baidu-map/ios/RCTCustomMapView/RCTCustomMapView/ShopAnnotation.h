//
//  ShopAnnotation.h
//  RCTCustomMapView
//
//  Created by imall on 16/11/23.
//  Copyright © 2016年 imall. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <BaiduMapAPI_Map/BMKMapComponent.h>

@interface ShopAnnotation : BMKPointAnnotation

@property (nonatomic,assign) NSInteger annotationTag;

@end

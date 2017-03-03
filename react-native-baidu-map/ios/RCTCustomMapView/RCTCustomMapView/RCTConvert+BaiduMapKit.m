//
//  RCTConvert+AMapKit.m
//  RCTAMap
//
//  Created by yiyang on 16/2/29.
//  Copyright © 2016年 creditease. All rights reserved.
//

#import "RCTImageSource.h"

#import "RCTConvert+BaiduMapKit.h"
#import "RCTConvert+CoreLocation.h"

@implementation RCTConvert (BaiduMapKit)

+ (BMKCoordinateSpan)BMKCoordinateSpan:(id)json
{
    json = [self NSDictionary:json];
    return (BMKCoordinateSpan){
        [self CLLocationDegrees:json[@"latitudeDelta"]],
        [self CLLocationDegrees:json[@"longitudeDelta"]]
    };
}

+ (BMKCoordinateRegion)BMKCoordinateRegion:(id)json
{
    return (BMKCoordinateRegion) {
        [self CLLocationCoordinate2D:json],
        [self BMKCoordinateSpan:json]
    };
}

RCT_ENUM_CONVERTER(BMKMapType, (@{
                                 @"standard": @(BMKMapTypeStandard),
                                 @"satellite": @(BMKMapTypeSatellite),
                                 }), BMKMapTypeStandard, integerValue)

+ (BMKLocationViewDisplayParam *)RCTBaiduMapLocationViewDisplayParam:(id)json
{
    json = [self NSDictionary:json];
    BMKLocationViewDisplayParam *param = [BMKLocationViewDisplayParam new];
    param.locationViewOffsetX = [self float:json[@"offsetX"]];
    param.locationViewOffsetY = [self float:json[@"offsetY"]];
    param.isAccuracyCircleShow = [self BOOL:json[@"showAccuracyCircle"]];
    param.accuracyCircleFillColor = [self UIColor:json[@"accuracyCircleFillColor"]];
    param.accuracyCircleStrokeColor = [self UIColor:json[@"accuracyCircleStrokeColor"]];
    param.isRotateAngleValid = [self BOOL:json[@"rotateAngleValid"]];
//    param.locationViewImgName = [self NSString:json[@"image"]];
    RCTImageSource *imageSource = [self RCTImageSource:json[@"image"]];
    if (imageSource != nil) {
        NSString *imgName = imageSource.imageURL.lastPathComponent;
        if (imgName != nil) {
            imgName = [imgName stringByDeletingPathExtension];
            param.locationViewImgName = imgName;
        }
    }
    
    return param;
}

@end

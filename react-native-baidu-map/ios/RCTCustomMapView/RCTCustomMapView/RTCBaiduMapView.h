//
//  RTCBaiduMapView.h
//  RCTCustomMapView
//
//  Created by imall on 16/8/6.
//  Copyright © 2016年 imall. All rights reserved.
//
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import "RCTComponent.h"

@interface RTCBaiduMapView : BMKMapView

@property (nonatomic, copy) RCTBubblingEventBlock onChange;
@property (nonatomic, copy) RCTBubblingEventBlock onPress;
@property (nonatomic, copy) RCTBubblingEventBlock onAnnotationDragStateChange;
@property (nonatomic, copy) RCTBubblingEventBlock onAnnotationFocus;
@property (nonatomic, copy) RCTBubblingEventBlock onAnnotationBlur;
@property (nonatomic) CLLocationCoordinate2D shopCoordinate;

- (void)setAnnotations:(id)location;

@end

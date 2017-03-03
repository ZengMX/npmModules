//
//  RCTCustomMapView.m
//  RCTCustomMapView
//
//  Created by imall on 16/8/6.
//  Copyright © 2016年 imall. All rights reserved.
//
#import "RTCBaiduMapView.h"
#import "RCTCustomMapView.h"
#import "RCTConvert.h"
#import "RCTConvert+CoreLocation.h"
#import "UIImage+Rotate.h"
#import "CustomCell.h"
#import "RGBForm16Radix.h"
#import "ShopAnnotation.h"
#import <MapKit/MapKit.h>
//icon_center_point
#define MYBUNDLE_NAME @ "mapapi.bundle"
#define MYBUNDLE_PATH [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent: MYBUNDLE_NAME]
#define MYBUNDLE [NSBundle bundleWithPath: MYBUNDLE_PATH]
#define SCREEN_WIDTH ([[UIScreen mainScreen] bounds].size.width)
#define SCREEN_HEIGHT ([[UIScreen mainScreen] bounds].size.height-64)
@interface SHBtn : UIButton

@property (nonatomic) BOOL selected;
@end

@implementation SHBtn

@synthesize selected = _selected;
@end

@interface RouteAnnotation : BMKPointAnnotation
{
    int _type; ///<0:起点 1：终点 2：公交 3：地铁 4:驾乘 5:途经点
    int _degree;
}

@property (nonatomic) int type;
@property (nonatomic) int degree;
@end

@implementation RouteAnnotation

@synthesize type = _type;
@synthesize degree = _degree;
@end

@interface UserAnnotation : BMKPointAnnotation
{
    int _degree;
}

@property (nonatomic) int degree;
@end

@implementation UserAnnotation

@synthesize degree = _degree;
@end

@implementation RCTCustomMapView

typedef enum
{
    ROUTE_USE = 0,
    LOCATION_USE = 1,
} MapUseType;

RCT_EXPORT_MODULE()

//RCT_EXPORT_VIEW_PROPERTY(showsUserLocation, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showsPointsOfInterest, BOOL)
RCT_EXPORT_VIEW_PROPERTY(followUserLocation, BOOL)
RCT_EXPORT_VIEW_PROPERTY(autoZoomToSpan, BOOL)
RCT_EXPORT_VIEW_PROPERTY(zoomEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(rotateEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(pitchEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(scrollEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(zoomLevel, float)
RCT_EXPORT_VIEW_PROPERTY(maxDelta, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(minDelta, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(legalLabelInsets, UIEdgeInsets)
RCT_EXPORT_VIEW_PROPERTY(mapType, BMKMapType)
RCT_EXPORT_VIEW_PROPERTY(overlays, NSArray<RCTBaiduMapOverlay *>)
RCT_EXPORT_VIEW_PROPERTY(onAnnotationDragStateChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAnnotationFocus, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAnnotationBlur, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)
RCT_CUSTOM_VIEW_PROPERTY(googleLocation, NSDictionary,RTCBaiduMapView)
{
    getCoor.latitude = [[json objectForKey:@"latitude"] floatValue];
    getCoor.longitude = [[json objectForKey:@"longitude"] floatValue];
}
RCT_CUSTOM_VIEW_PROPERTY(mapUseType, MapUseType,RTCBaiduMapView)
{
    [self loadMapWithType:json];
}
RCT_CUSTOM_VIEW_PROPERTY(showsUserLocation, BOOL, RTCBaiduMapView)
{
    [self startUserLocation:json];
}
RCT_CUSTOM_VIEW_PROPERTY(center, NSDictionary, RTCBaiduMapView)
{
    [self setCenter:json];
}
RCT_CUSTOM_VIEW_PROPERTY(annotations, NSArray<BMKPointAnnotation *>,RTCBaiduMapView)
{
    [self startUserLocation:@1];
    if (((NSArray *)json).count==1) {
        coorT.latitude = [[[((NSArray *)json) objectAtIndex:0] objectForKey:@"latitude"] floatValue];
        coorT.longitude = [[[((NSArray *)json) objectAtIndex:0] objectForKey:@"longitude"] floatValue];
        _shopName = [[((NSArray *)json) objectAtIndex:0] objectForKey:@"title"];
        _addrName = [[((NSArray *)json) objectAtIndex:0] objectForKey:@"subtitle"];
    }
    _annotationArr = json;
    [view setAnnotations:json];
}
RCT_CUSTOM_VIEW_PROPERTY(region, BMKCoordinateRegion, RTCBaiduMapView)
{
    [view setRegion:json ? [self BMKCoordinateRegion:json] : defaultView.region animated:YES];
}
RCT_CUSTOM_VIEW_PROPERTY(userLocationViewParams, BMKLocationViewDisplayParam, RTCBaiduMapView)
{
    if (json) {
//        [view setUserLocationViewParams:[RCTConvert RCTBaiduMapLocationViewDisplayParam:json]];
    }
}

RCT_ENUM_CONVERTER(BMKMapType, (@{
                                  @"standard": @(BMKMapTypeStandard),
                                  @"satellite": @(BMKMapTypeSatellite),
                                  }), BMKMapTypeStandard, integerValue)

- (void)startUserLocation:(id)json
{
    if([json intValue]==1){
        NSLog(@"开始定位");
        [_locService startUserLocationService];
    } else {
        NSLog(@"停止定位");
        [_locService stopUserLocationService];
    }
}

- (void)setCenter:(id)json
{
    NSArray *arr = (NSArray *)json ;
    if(arr.count>0){
        float latitude = [[json objectForKey:@"latitude"]floatValue];
        float longitude = [[json objectForKey:@"longitude"]floatValue];
        CLLocationCoordinate2D coor ;
        coor.latitude = latitude;
        coor.longitude = longitude;
        
        _mapView.centerCoordinate = coor;
        
        BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
        reverseGeocodeSearchOption.reverseGeoPoint = coor;
        BOOL flag = [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
        if(flag)
        {
            NSLog(@"反geo检索发送成功");
        }
        else
        {
            NSLog(@"反geo检索发送失败");
        }
    }
}

- (void)loadMapWithType:(id)json
{
    if([json intValue]==ROUTE_USE){
        routeStyle = -1;
        tab = [[UITableView alloc] initWithFrame:CGRectMake(0, SCREEN_HEIGHT-120, SCREEN_WIDTH, 250) style:UITableViewStylePlain];
        tab.scrollEnabled = NO;
        tab.delegate = self;
        tab.dataSource = self;
        tab.separatorStyle = UITableViewCellSeparatorStyleNone;
        routes = [NSMutableArray array];
        [_mapView addSubview:tab];
        
//        UIButton *backBtn = [UIButton buttonWithType:UIButtonTypeCustom];
//        backBtn.frame = CGRectMake(10, 20, 40, 40);
//        backBtn.layer.shadowColor = [UIColor grayColor].CGColor;
//        backBtn.layer.shadowOpacity = 0.2;
//        backBtn.backgroundColor = [UIColor whiteColor];
//        backBtn.layer.cornerRadius = 3;
//        backBtn.alpha = .85;
//        [backBtn setImage:[UIImage imageNamed:@"backimg"] forState:UIControlStateNormal];
//        [backBtn addTarget:self action:@selector(returnBack) forControlEvents:UIControlEventTouchUpInside];
//        [_mapView addSubview:backBtn];
    } else if([json intValue]==LOCATION_USE) {
        
    }
}


#pragma mark - LocationDelegate

/**
 *在地图View将要启动定位时，会调用此函数
 *@param mapView 地图View
 */
- (void)willStartLocatingUser
{
    NSLog(@"start locate");
}

/**
 *用户方向更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateUserHeading:(BMKUserLocation *)userLocation
{
    [_mapView updateLocationData:userLocation];
    NSLog(@"heading is %@",userLocation.heading);
}

/**
 *用户位置更新后，会调用此函数
 *@param userLocation 新的用户位置
 */
- (void)didUpdateBMKUserLocation:(BMKUserLocation *)userLocation
{
    
    [_mapView updateLocationData:userLocation];
    userLoc = userLocation;
    
    if(userLocation != nil) {
        [_mapView updateLocationData:userLocation];
        
        
        CLLocationCoordinate2D coord;
        coord.latitude=userLocation.location.coordinate.latitude;
        coord.longitude=userLocation.location.coordinate.longitude;
        
        BMKCoordinateRegion region ;//表示范围的结构体
        
        region.center = coord;//指定地图中心点
        region.span.latitudeDelta = 0.1;//经度范围（设置为0.1表示显示范围为0.2的纬度范围）
        region.span.longitudeDelta = 0.1;//纬度范围
        [_mapView setRegion:region animated:YES];
        [self startUserLocation:0];
        BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
        reverseGeocodeSearchOption.reverseGeoPoint = userLocation.location.coordinate;
        BOOL flag = [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
        if(flag)
        {
            NSLog(@"反geo检索发送成功");
        }
        else
        {
            NSLog(@"反geo检索发送失败");
        }
    }
    
//    [self FireEventWithLocation:@[@"INFOS1",@"INFOS2"]];
    
    
//    _mapView.centerCoordinate = userLocation.location.coordinate;
    
    
//    [_mapView setRegion:BMKCoordinateRegionMake(userLocation.location.coordinate, BMKCoordinateSpanMake(userLocation.location.coordinate.latitude, userLocation.location.coordinate.longitude)) animated:YES];
    
}

/**
 *在地图View停止定位后，会调用此函数
 *@param mapView 地图View
 */
- (void)didStopLocatingUser
{
    NSLog(@"stop locate");
}

/**
 *定位失败后，会调用此函数
 *@param mapView 地图View
 *@param error 错误号，参考CLError.h中定义的错误号
 */
- (void)didFailToLocateUserWithError:(NSError *)error
{
    [_locService stopUserLocationService];
    NSLog(@"location error");
}


- (UIView *)view
{
    _poiListArray = [NSMutableArray array];
    ann = [[BMKPointAnnotation alloc]init];
    _mapView = [[RTCBaiduMapView alloc] init];
    _geocodesearch = [[BMKGeoCodeSearch alloc]init];
    _geocodesearch.delegate = self;
    _mapView.delegate = self;
    
    _locService = [[BMKLocationService alloc]init];
    _locService.delegate = self;
    
    _mapView.userTrackingMode = BMKUserTrackingModeFollow;
    
    //测试用
//    [self loadMapWithType:@0];
    
//    [tab.searchRouteBtn addTarget:self action:@selector(searchLine) forControlEvents:UIControlEventTouchUpInside];
    
    return _mapView;
}

- (void)returnBack
{
    if(routeStyle != -1){
        routeStyle = -1;
        tab.frame = CGRectMake(0, SCREEN_HEIGHT-120, SCREEN_WIDTH, 250);
        routes = [NSMutableArray array];
        [tab reloadData];
        
        [_mapView removeOverlays:_mapView.overlays];
        [_mapView removeAnnotations:_mapView.annotations];
        [self searchLine];
    }
}

- (void)dealloc {
    if (_geocodesearch != nil) {
        _geocodesearch = nil;
        _geocodesearch.delegate = nil;
    }
    if(_locService != nil) {
        _locService = nil;
        _locService.delegate = nil;
    }
    if (_mapView) {
        _mapView = nil;
        _mapView.delegate = nil;
    }
    if(ann){
        ann = nil;
    }
    if(_poiListArray != nil){
        _poiListArray = nil;
    }
}

- (BMKCoordinateRegion)BMKCoordinateRegion:(id)json
{
    return (BMKCoordinateRegion) {
        [self CLLocationCoordinate2D:json],
        [self BMKCoordinateSpan:json]
    };
}

- (CLLocationCoordinate2D)CLLocationCoordinate2D:(id)json
{
    json = [RCTConvert NSDictionary:json];
    return (CLLocationCoordinate2D){
        [RCTConvert CLLocationDegrees:json[@"latitude"]],
        [RCTConvert CLLocationDegrees:json[@"longitude"]]
    };
}

- (BMKCoordinateSpan)BMKCoordinateSpan:(id)json
{
    json = [RCTConvert NSDictionary:json];
    return (BMKCoordinateSpan){
        [RCTConvert CLLocationDegrees:json[@"latitudeDelta"]],
        [RCTConvert CLLocationDegrees:json[@"longitudeDelta"]]
    };
}

#pragma mark - BMKGeoCodeSearchDelegate反地理编码的代理
-(void) onGetReverseGeoCodeResult:(BMKGeoCodeSearch *)searcher result:(BMKReverseGeoCodeResult *)result errorCode:(BMKSearchErrorCode)error
{
    if (error == 0) {
        [_poiListArray removeAllObjects];
        for(BMKPoiInfo *poiInfo in result.poiList)
        {
            NSString *lat = [NSString stringWithFormat:@"%f",poiInfo.pt.latitude];
            NSString *lon = [NSString stringWithFormat:@"%f",poiInfo.pt.longitude];
            [_poiListArray addObject:@{@"address":poiInfo.address,@"name":poiInfo.name,@"pt":@{@"lat":lat,@"lon":lon}}];
        }
        
        [self FireEventWithLocation:_poiListArray
                         andAddress:result.address
                           province:result.addressDetail.province
                               city:result.addressDetail.city
                           district:result.addressDetail.district
                         streetName:result.addressDetail.streetName
                       streetNumber:result.addressDetail.streetNumber];
    }
}

-(void)FireEventWithLocation:(id)infos
                  andAddress:(NSString *)address
                    province:(NSString *)province
                        city:(NSString *)city
                    district:(NSString *)district
                  streetName:(NSString *)streetName
                streetNumber:(NSString *)streetNumber
{
    [self.bridge.eventDispatcher sendAppEventWithName:@"EventReminderMap"
                                                 body:@{@"polists":infos,
                                                        @"address":address,
                                                        @"province":province,
                                                        @"city":city,
                                                        @"district":district,
                                                        @"streetName":streetName,
                                                        @"streetNumber":streetNumber}];
}

- (void)FireEventWithClickAction:(UIButton *)sender
{
    //    NSLog(@"fsdddddddddd");
    [self FireClick:sender.tag];
}

- (void)FireClick:(NSInteger)index
{
    NSDictionary *anntationObj;
    if(_annotationArr.count>0){
        anntationObj = [[_annotationArr objectAtIndex:index] objectForKey:@"object"];
    }
    [self.bridge.eventDispatcher sendAppEventWithName:@"DetailAction"
                                                 body:anntationObj];
}

#pragma mark - Annotation和Overlay BMKMapViewDelegate定义
- (void)mapView:(BMKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    BMKReverseGeoCodeOption *reverseGeocodeSearchOption = [[BMKReverseGeoCodeOption alloc]init];
    reverseGeocodeSearchOption.reverseGeoPoint = mapView.centerCoordinate;
    BOOL flag = [_geocodesearch reverseGeoCode:reverseGeocodeSearchOption];
    if(flag)
    {
        NSLog(@"反geo检索发送成功");
    }
    else
    {
        NSLog(@"反geo检索发送失败");
    }
}

// 根据anntation生成对应的View
- (BMKAnnotationView *)mapView:(BMKMapView *)mapView viewForAnnotation:(id <BMKAnnotation>)annotation
{
    if([annotation isKindOfClass:[UserAnnotation class]]){
        BMKPinAnnotationView *annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:@"User"];
        annotationView.image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_center_point.png"]];
        return annotationView;
        
    }
    if ([annotation isKindOfClass:[RouteAnnotation class]]) {
        return [self getRouteAnnotationView:mapView viewForAnnotation:(RouteAnnotation*)annotation];
    } else {
    
        UIView *popView = [[UIView alloc]initWithFrame:CGRectMake(0, 0, 220, 60)];
        popView.backgroundColor = [UIColor whiteColor];
        [popView.layer setMasksToBounds:YES];
        [popView.layer setCornerRadius:3.0];
        popView.alpha = 0.9;
        
        //自定义气泡的内容，添加子控件在popView上
        UILabel *driverName = [[UILabel alloc]initWithFrame:CGRectMake(8, 4, 150, 30)];
        driverName.text = annotation.title;
        driverName.numberOfLines = 0;
        driverName.backgroundColor = [UIColor clearColor];
        driverName.font = [UIFont systemFontOfSize:15];
        driverName.textColor = [UIColor blackColor];
        driverName.textAlignment = NSTextAlignmentLeft;
        [popView addSubview:driverName];
        
        UIImageView *searchImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"thismap"]];
        searchImage.frame = CGRectMake(0, 9, 10, 11);
        
        UILabel *carName = [[UILabel alloc]initWithFrame:CGRectMake(10, 0, 150, 30)];
        carName.text = annotation.subtitle;
        carName.backgroundColor = [UIColor clearColor];
        carName.font = [UIFont systemFontOfSize:11];
        carName.textColor = [UIColor lightGrayColor];
        carName.textAlignment = NSTextAlignmentLeft;
        
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        btn.frame = CGRectMake(8, 30, 160, 30);
        [btn addSubview:searchImage];
        [btn addSubview:carName];
        [btn addTarget:self action:@selector(showMapRouteList) forControlEvents:UIControlEventTouchUpInside];
        [popView addSubview:btn];
        
        UIButton *detailBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        detailBtn.frame = CGRectMake(168, 0, 52, 60);
        detailBtn.backgroundColor = [UIColor colorWithRed:47/255.0 green:45/255.0 blue:58/255.0 alpha:1.0];
        [detailBtn setTitle:@"详情" forState:UIControlStateNormal];
        
        if([annotation isKindOfClass:[ShopAnnotation class]]){
            ShopAnnotation *shopAnn = (ShopAnnotation *)annotation;
            if([[_annotationArr objectAtIndex:shopAnn.annotationTag] objectForKey:@"buttonStyle"]!=nil){
                NSDictionary *btnstyle = [[_annotationArr objectAtIndex:shopAnn.annotationTag] objectForKey:@"buttonStyle"];
                if([btnstyle objectForKey:@"isVisible"]){
                    [popView addSubview:detailBtn];
                }
                UIColor *txtColor = [RGBForm16Radix colorWithHexString:[btnstyle objectForKey:@"textColor"] alpha:1.0];
                [detailBtn setTitleColor:txtColor forState:UIControlStateNormal];
                [detailBtn setTitle:[btnstyle objectForKey:@"text"] forState:UIControlStateNormal];
            }
            detailBtn.tag = shopAnn.annotationTag;
        }
        [detailBtn addTarget:self action:@selector(FireEventWithClickAction:) forControlEvents:UIControlEventTouchUpInside];
        
        NSString *AnnotationViewID = @"renameMark";
        BMKPinAnnotationView *annotationView = (BMKPinAnnotationView *)[mapView dequeueReusableAnnotationViewWithIdentifier:AnnotationViewID];
        if (annotationView == nil) {
            annotationView = [[BMKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:AnnotationViewID];
            // 设置颜色
            annotationView.pinColor = BMKPinAnnotationColorPurple;
            // 设置可拖拽
//            annotationView.draggable = YES;
            
            annotationView.image = [UIImage imageNamed:@"map_annotation.png"];
            
            annotationView.paopaoView = nil;
            
            annotationView.paopaoView = [[BMKActionPaopaoView alloc] initWithCustomView:popView];
        }
        return annotationView;
    }
    
}


- (void)showMapRouteList
{
    [self searchLine];
}

- (NSString*)getMyBundlePath1:(NSString *)filename
{
    
    NSBundle * libBundle = MYBUNDLE ;
    if ( libBundle && filename ){
        NSString * s=[[libBundle resourcePath ] stringByAppendingPathComponent : filename];
        return s;
    }
    return nil ;
}


- (BMKAnnotationView*)getRouteAnnotationView:(BMKMapView *)mapview viewForAnnotation:(RouteAnnotation*)routeAnnotation
{
    BMKAnnotationView* view = nil;
    switch (routeAnnotation.type) {
        case 0:
        {
            view = [mapview dequeueReusableAnnotationViewWithIdentifier:@"start_node"];
            if (view == nil) {
                view = [[BMKAnnotationView alloc]initWithAnnotation:routeAnnotation reuseIdentifier:@"start_node"];
                view.image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_nav_start.png"]];
                view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                view.canShowCallout = TRUE;
            }
            view.annotation = routeAnnotation;
        }
            break;
        case 1:
        {
            view = [mapview dequeueReusableAnnotationViewWithIdentifier:@"end_node"];
            if (view == nil) {
                view = [[BMKAnnotationView alloc]initWithAnnotation:routeAnnotation reuseIdentifier:@"end_node"];
                view.image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_nav_end.png"]];
                view.centerOffset = CGPointMake(0, -(view.frame.size.height * 0.5));
                view.canShowCallout = TRUE;
            }
            view.annotation = routeAnnotation;
        }
            break;
        case 2:
        {
            view = [mapview dequeueReusableAnnotationViewWithIdentifier:@"bus_node"];
            if (view == nil) {
                view = [[BMKAnnotationView alloc]initWithAnnotation:routeAnnotation reuseIdentifier:@"bus_node"];
                view.image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_nav_bus.png"]];
                view.canShowCallout = TRUE;
            }
            view.annotation = routeAnnotation;
        }
            break;
        case 3:
        {
            view = [mapview dequeueReusableAnnotationViewWithIdentifier:@"rail_node"];
            if (view == nil) {
                view = [[BMKAnnotationView alloc]initWithAnnotation:routeAnnotation reuseIdentifier:@"rail_node"];
                view.image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_nav_rail.png"]];
                view.canShowCallout = TRUE;
            }
            view.annotation = routeAnnotation;
        }
            break;
        case 4:
        {
            view = [mapview dequeueReusableAnnotationViewWithIdentifier:@"route_node"];
            if (view == nil) {
                view = [[BMKAnnotationView alloc]initWithAnnotation:routeAnnotation reuseIdentifier:@"route_node"];
                view.canShowCallout = TRUE;
            } else {
                [view setNeedsDisplay];
            }
            
            UIImage* image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_direction.png"]];
            view.image = [image imageRotatedByDegrees:routeAnnotation.degree];
            view.annotation = routeAnnotation;
            
        }
            break;
        case 5:
        {
            view = [mapview dequeueReusableAnnotationViewWithIdentifier:@"waypoint_node"];
            if (view == nil) {
                view = [[BMKAnnotationView alloc]initWithAnnotation:routeAnnotation reuseIdentifier:@"waypoint_node"];
                view.canShowCallout = TRUE;
            } else {
                [view setNeedsDisplay];
            }
            
            UIImage* image = [UIImage imageWithContentsOfFile:[self getMyBundlePath1:@"images/icon_nav_waypoint.png"]];
            view.image = [image imageRotatedByDegrees:routeAnnotation.degree];
            view.annotation = routeAnnotation;
        }
            break;
        default:
            break;
    }
    
    return view;
}


- (void)searchLine
{
    SelectRouteViewController *selectRoute = [[SelectRouteViewController alloc] init];
    selectRoute.RouteDelegate = self;
    selectRoute.targetLocation = coorT;
    selectRoute.getLocation = getCoor;
    selectRoute.targetName = _shopName?_shopName:@"";
    if(userLoc){
        CLGeocoder *geocoder = [[CLGeocoder alloc] init];
        [geocoder reverseGeocodeLocation:userLoc.location completionHandler:^(NSArray<CLPlacemark *> * _Nullable placemarks, NSError * _Nullable error) {
            if (placemarks.count > 0) {
                
                CLPlacemark *placemark = [placemarks objectAtIndex:0];
                
                if (placemark != nil) {
                    
                    NSString *city = placemark.locality;
                    selectRoute.startLocation = userLoc.location.coordinate;
                    selectRoute.location = userLoc?userLoc:nil;
                    selectRoute.cityName = city;
                    [[UIApplication sharedApplication].delegate.window.rootViewController presentViewController:selectRoute animated:YES completion:^{
                        
                    }];
                }
            }
        }];
        
    }else {
        UIAlertController *alertCtrl = [UIAlertController alertControllerWithTitle:@"提示" message:@"请先打开定位，退出本页面重新打开地图" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil];
        [alertCtrl addAction:action];
        [[[[[UIApplication sharedApplication] delegate] window] rootViewController] presentViewController:alertCtrl animated:YES completion:nil];
    }
}

- (void)mapView:(BMKMapView *)mapView didSelectAnnotationView:(BMKAnnotationView *)view
{
    // 主要是这句
    if(_annotationArr.count>1) {
        coorT = view.annotation.coordinate;
        routeStyle = -1;
        _shopName = view.annotation.title;
        _addrName = view.annotation.subtitle;
        [self loadMapWithType:@0];
    }
    [_mapView setCenterCoordinate:view.annotation.coordinate animated:YES];
}

- (BMKOverlayView*)mapView:(BMKMapView *)map viewForOverlay:(id<BMKOverlay>)overlay
{
    if ([overlay isKindOfClass:[BMKPolyline class]]) {
        BMKPolylineView* polylineView = [[BMKPolylineView alloc] initWithOverlay:overlay];
        polylineView.fillColor = [[UIColor alloc] initWithRed:0 green:1 blue:1 alpha:1];
        polylineView.strokeColor = [[UIColor alloc] initWithRed:0 green:0 blue:1 alpha:0.7];
        polylineView.lineWidth = 8.0;
        return polylineView;
    }
    return nil;
}

//根据polyline设置地图范围
- (void)mapViewFitPolyLine:(BMKPolyline *) polyLine {
    CGFloat ltX, ltY, rbX, rbY;
    if (polyLine.pointCount < 1) {
        return;
    }
    BMKMapPoint pt = polyLine.points[0];
    ltX = pt.x, ltY = pt.y;
    rbX = pt.x, rbY = pt.y;
    for (int i = 1; i < polyLine.pointCount; i++) {
        BMKMapPoint pt = polyLine.points[i];
        if (pt.x < ltX) {
            ltX = pt.x;
        }
        if (pt.x > rbX) {
            rbX = pt.x;
        }
        if (pt.y > ltY) {
            ltY = pt.y;
        }
        if (pt.y < rbY) {
            rbY = pt.y;
        }
    }
    BMKMapRect rect;
    rect.origin = BMKMapPointMake(ltX , ltY);
    rect.size = BMKMapSizeMake(rbX - ltX, rbY - ltY);
    [_mapView setVisibleMapRect:rect];
    _mapView.zoomLevel = _mapView.zoomLevel - 0.3;
}

#pragma mark - resetUI

- (void)reloadBottomViewsWithType:(RouteStyle)style andSelectRouteLine:(BMKRouteLine *)line
{
//    DisplayTable *displayList = [[DisplayTable alloc] initWithFrame:CGRectMake(0, 0, tab.frame.size.width, 170) style:UITableViewStylePlain];
//    displayList.delegate = self;
//    displayList.dataSource = self;
    selectLine = line;
    [tab reloadData];
    
    tab.frame = CGRectMake(0, SCREEN_HEIGHT-80, SCREEN_WIDTH, 250);
        
//    [tab reloadViewWith:displayList];
}

#pragma mark - TableViewDelegate   TableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return routes.count;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"route"];
    if(!cell){
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"route"];
    }
    
    for (UIView *view in cell.contentView.subviews) {
        [view removeFromSuperview];
    }
    
    NSString *imagName;
    NSString *stepInfo;
    if([routes[indexPath.row] isKindOfClass:[BMKTransitStep class]])
    {
        BMKTransitStep *step = routes[indexPath.row];
        if(step.stepType==2) {
            imagName = @"walk";
        } else {
            imagName = @"bus";
        }
        stepInfo = step.instruction;
        
    } else if([routes[indexPath.row] isKindOfClass:[BMKDrivingStep class]]){
        imagName = @"driver";
        BMKDrivingStep *step = routes[indexPath.row];
        stepInfo = step.instruction;
    } else if([routes[indexPath.row] isKindOfClass:[BMKWalkingStep class]]){
        imagName = @"walk";
        BMKWalkingStep *step = routes[indexPath.row];
        stepInfo = step.instruction;
    }
    
    UIImageView *image = [[UIImageView alloc] initWithImage:[UIImage imageNamed:imagName]];
    image.frame = CGRectMake(10, 20, 20, 20);
    
    UILabel *detailStep = [[UILabel alloc] init];
    detailStep.text = stepInfo;
    detailStep.font = [UIFont systemFontOfSize:14];
    detailStep.numberOfLines = 2;
    detailStep.lineBreakMode = NSLineBreakByTruncatingTail;
    CGSize maximumLabelSize = CGSizeMake(SCREEN_WIDTH-50, 40);
    CGSize expectSize = [detailStep sizeThatFits:maximumLabelSize];
    detailStep.textColor = [UIColor grayColor];
    detailStep.frame = CGRectMake(40, 20, expectSize.width, expectSize.height);
    [cell.contentView addSubview:detailStep];
    [cell.contentView addSubview:image];
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 55;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    float height = routeStyle == -1?110:80;
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, height)];
    view.backgroundColor = [UIColor whiteColor];
    UIView *line = [[UIView alloc] initWithFrame:CGRectMake(12, 75, SCREEN_WIDTH-24, 0.5)];
    line.backgroundColor = [UIColor lightGrayColor];
    if(routeStyle == -1) {
        UILabel *shopName = [[UILabel alloc] initWithFrame:CGRectMake(12, 18, SCREEN_WIDTH-40, 25)];
        shopName.text = _shopName;
        UILabel *addressName = [[UILabel alloc] initWithFrame:CGRectMake(12, 43, SCREEN_WIDTH-40, 25)];
        addressName.text = _addrName;
        addressName.textColor = [UIColor grayColor];
        shopName.font = [UIFont systemFontOfSize:15];
        addressName.font = [UIFont systemFontOfSize:12];
        
        UIButton *searchRouteBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        searchRouteBtn.frame = CGRectMake(0, 76, SCREEN_WIDTH/2, 44);
        [searchRouteBtn setTitle:@"查看路线" forState:UIControlStateNormal];
        [searchRouteBtn setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [searchRouteBtn setImage:[UIImage imageNamed:@"searchroute"] forState:UIControlStateNormal];
        searchRouteBtn.imageEdgeInsets = UIEdgeInsetsMake(15,30,15,85);
        
        UIButton *selfMapBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        selfMapBtn.frame = CGRectMake(SCREEN_WIDTH/2, 76, SCREEN_WIDTH/2, 44);
        [selfMapBtn setTitle:@"本机地图" forState:UIControlStateNormal];
        [selfMapBtn setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
        [selfMapBtn setImage:[UIImage imageNamed:@"thismap"] forState:UIControlStateNormal];
        selfMapBtn.imageEdgeInsets = UIEdgeInsetsMake(15,30,15,85);
        
        searchRouteBtn.titleLabel.font = selfMapBtn.titleLabel.font = [UIFont systemFontOfSize:14];
        [searchRouteBtn addTarget:self action:@selector(searchLine) forControlEvents:UIControlEventTouchUpInside];
        [selfMapBtn addTarget:self action:@selector(doNavgatorInMap) forControlEvents:UIControlEventTouchUpInside];
        
        [view addSubview:shopName];
        [view addSubview:addressName];
        [view addSubview:searchRouteBtn];
        [view addSubview:selfMapBtn];
        [view addSubview:line];
    } else {
        SHBtn *hideBtn = [SHBtn buttonWithType:UIButtonTypeCustom];
        hideBtn.selected = NO;
        hideBtn.frame = CGRectMake((SCREEN_WIDTH-60)/2, 0, 60, 30);
        [hideBtn setImage:[UIImage imageNamed:@"uparrow"] forState:UIControlStateNormal];
        [hideBtn addTarget:self action:@selector(showOrHideList:) forControlEvents:UIControlEventTouchUpInside];
        UILabel *TrainName = [[UILabel alloc] initWithFrame:CGRectMake(20, 25, SCREEN_WIDTH-60, 25)];
        TrainName.font = [UIFont boldSystemFontOfSize:15];
        
        UILabel *TrainInfo = [[UILabel alloc] initWithFrame:CGRectMake(20, 50, SCREEN_WIDTH-60, 25)];
        TrainInfo.font = [UIFont systemFontOfSize:12];
        TrainInfo.textColor = [UIColor grayColor];
        
        if(routeStyle == BUS_STYLE){
            BMKTransitRouteLine *line = selectLine;
            NSMutableArray *transites = [NSMutableArray array];
            NSString *transitName;
            NSString *distance = [NSString stringWithFormat:@"%.1f公里",line.distance/1000.0];
            BMKTime *time = line.duration; //用时
            NSString *payTime = @"";
            int walkdistance = 0;
            NSString *walkLength = @"";
            
            if(time.dates!=0){
                payTime = [NSString stringWithFormat:@"%d天",time.dates];
            }
            if(time.hours!=0){
                payTime = [NSString stringWithFormat:@"%@%d小时",payTime,time.hours];
            }
            if(time.minutes!=0){
                payTime = [NSString stringWithFormat:@"%@%d分钟",payTime,time.minutes];
            }
            for (BMKTransitStep *step in line.steps) {
                
                if(step.stepType==0){
                    [transites addObject:step.vehicleInfo.title];
                }
                if(step.stepType==1){
                    [transites addObject:step.vehicleInfo.title];
                }
                if(step.stepType==2){
                    walkdistance += step.distance;
                }
            }
            
            walkLength = [NSString stringWithFormat:@"步行%d米",walkdistance];
            
            if(transites.count==1){
                transitName = transites[0];
            }
            if(transites.count>1) {
                for (int i=0;i<transites.count;i++) {
                    if(i==0){
                        transitName = [NSString stringWithFormat:@"%@",transites[0]];
                    } else {
                        transitName = [NSString stringWithFormat:@"%@-%@",transitName,transites[i]];
                    }
                }
            }
            
            TrainName.text = transitName;
            TrainInfo.text = [NSString stringWithFormat:@"%@  |  %@  |  %@",payTime,distance,walkLength];
        } else if(routeStyle == DRIVE_STYLE) {
            BMKDrivingRouteLine *line = selectLine;
            NSString *driveName = @"驾车路线";
            BMKTime *time = line.duration; //用时
            
            NSString *distance = [NSString stringWithFormat:@"%.1f公里",line.distance/1000.0];
            NSString *payTime = @"";
            if(time.dates!=0){
                payTime = [NSString stringWithFormat:@"%d天",time.dates];
            }
            if(time.hours!=0){
                payTime = [NSString stringWithFormat:@"%@%d小时",payTime,time.hours];
            }
            if(time.minutes!=0){
                payTime = [NSString stringWithFormat:@"%@%d分钟",payTime,time.minutes];
            }
            
            TrainName.text = driveName;
            TrainInfo.text = [NSString stringWithFormat:@"%@  |  %@",payTime,distance];
        } else {
            BMKWalkingRouteLine *line = selectLine;
            NSString *driveName = @"步行路线";
            BMKTime *time = line.duration; //用时
            NSString *distance = [NSString stringWithFormat:@"%.1f公里",line.distance/1000.0];
            NSString *payTime = @"";
            if(time.dates!=0){
                payTime = [NSString stringWithFormat:@"%d天",time.dates];
            }
            if(time.hours!=0){
                payTime = [NSString stringWithFormat:@"%@%d小时",payTime,time.hours];
            }
            if(time.minutes!=0){
                payTime = [NSString stringWithFormat:@"%@%d分钟",payTime,time.minutes];
            }
            TrainName.text = driveName;
            TrainInfo.text = [NSString stringWithFormat:@"%@  |  %@",payTime,distance];
            
        }
        [view addSubview:hideBtn];
        [view addSubview:TrainInfo];
        [view addSubview:TrainName];
    }
    [view addSubview:line];
    return view;
}

- (void)showOrHideList:(SHBtn *)sender
{
    sender.selected = !sender.selected;
    if(sender.selected){
        [sender setImage:[UIImage imageNamed:@"downarrow"] forState:UIControlStateNormal];
        [UIView animateWithDuration:0.5 animations:^{
            tab.frame = CGRectMake(0, SCREEN_HEIGHT-250, SCREEN_WIDTH, 250);
        }];
        tab.scrollEnabled = YES;
    } else {
        [sender setImage:[UIImage imageNamed:@"uparrow"] forState:UIControlStateNormal];
        [UIView animateWithDuration:0.5 animations:^{
            tab.frame = CGRectMake(0, SCREEN_HEIGHT-80, SCREEN_WIDTH, 250);
        }];
        tab.scrollEnabled = NO;
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if(routeStyle != -1){
        return 80;
    } else {
        return 110;
    }
}

#pragma mark - searchRoutDelegate

- (void)searchTheRouteWithType:(RouteStyle)style andSelectRouteLine:(BMKRouteLine *)line
{
    routeStyle = style;
    routes = [NSMutableArray arrayWithArray:line.steps];
    [self reloadBottomViewsWithType:style andSelectRouteLine:line];
    NSArray* array = [NSArray arrayWithArray:_mapView.annotations];
    [_mapView removeAnnotations:array];
    array = [NSArray arrayWithArray:_mapView.overlays];
    [_mapView removeOverlays:array];
    BMKMapPoint * temppoints;
    BMKPolyline* polyLine;
    int planPointCounts = 0;
    if(style == BUS_STYLE){
        BMKTransitRouteLine* plan = (BMKTransitRouteLine*)line;
        // 计算路线方案中的路段数目
        NSInteger size = [plan.steps count];
        for (int i = 0; i < size; i++) {
            BMKTransitStep* transitStep = [plan.steps objectAtIndex:i];
            if(i==0){
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item.coordinate = plan.starting.location;
                item.title = @"起点";
                item.type = 0;
                [_mapView addAnnotation:item]; // 添加起点标注
                
            }else if(i==size-1){
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item.coordinate = plan.terminal.location;
                item.title = @"终点";
                item.type = 1;
                [_mapView addAnnotation:item]; // 添加起点标注
            }
            RouteAnnotation* item = [[RouteAnnotation alloc]init];
            item.coordinate = transitStep.entrace.location;
            item.title = transitStep.instruction;
            item.type = 3;
            [_mapView addAnnotation:item];
            
            //轨迹点总数累计
            planPointCounts += transitStep.pointsCount;
        }
        
        //轨迹点
        temppoints = new BMKMapPoint[planPointCounts];
        int i = 0;
        for (int j = 0; j < size; j++) {
            BMKTransitStep* transitStep = [plan.steps objectAtIndex:j];
            int k=0;
            for(k=0;k<transitStep.pointsCount;k++) {
                temppoints[i].x = transitStep.points[k].x;
                temppoints[i].y = transitStep.points[k].y;
                i++;
            }
            
        }
    } else if(style == DRIVE_STYLE) {
        BMKDrivingRouteLine* plan = (BMKDrivingRouteLine*)line;
        // 计算路线方案中的路段数目
        NSInteger size = [plan.steps count];
        for (int i = 0; i < size; i++) {
            BMKDrivingStep* transitStep = [plan.steps objectAtIndex:i];
            if(i==0){
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item.coordinate = plan.starting.location;
                item.title = @"起点";
                item.type = 0;
                [_mapView addAnnotation:item]; // 添加起点标注
                
            }else if(i==size-1){
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item.coordinate = plan.terminal.location;
                item.title = @"终点";
                item.type = 1;
                [_mapView addAnnotation:item]; // 添加起点标注
            }
            //添加annotation节点
            RouteAnnotation* item = [[RouteAnnotation alloc]init];
            item.coordinate = transitStep.entrace.location;
            item.title = transitStep.entraceInstruction;
            item.degree = transitStep.direction * 30;
            item.type = 4;
            [_mapView addAnnotation:item];
            
            //轨迹点总数累计
            planPointCounts += transitStep.pointsCount;
        }
        // 添加途经点
        if (plan.wayPoints) {
            for (BMKPlanNode* tempNode in plan.wayPoints) {
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item = [[RouteAnnotation alloc]init];
                item.coordinate = tempNode.pt;
                item.type = 5;
                item.title = tempNode.name;
                [_mapView addAnnotation:item];
            }
        }
        //轨迹点
        temppoints = new BMKMapPoint[planPointCounts];
        int i = 0;
        for (int j = 0; j < size; j++) {
            BMKDrivingStep* transitStep = [plan.steps objectAtIndex:j];
            int k=0;
            for(k=0;k<transitStep.pointsCount;k++) {
                temppoints[i].x = transitStep.points[k].x;
                temppoints[i].y = transitStep.points[k].y;
                i++;
            }
            
        }
    } else {
        BMKWalkingRouteLine* plan = (BMKWalkingRouteLine*)line;
        NSInteger size = [plan.steps count];
        for (int i = 0; i < size; i++) {
            BMKWalkingStep* transitStep = [plan.steps objectAtIndex:i];
            if(i==0){
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item.coordinate = plan.starting.location;
                item.title = @"起点";
                item.type = 0;
                [_mapView addAnnotation:item]; // 添加起点标注
                
            }else if(i==size-1){
                RouteAnnotation* item = [[RouteAnnotation alloc]init];
                item.coordinate = plan.terminal.location;
                item.title = @"终点";
                item.type = 1;
                [_mapView addAnnotation:item]; // 添加起点标注
            }
            //添加annotation节点
            RouteAnnotation* item = [[RouteAnnotation alloc]init];
            item.coordinate = transitStep.entrace.location;
            item.title = transitStep.entraceInstruction;
            item.degree = transitStep.direction * 30;
            item.type = 4;
            [_mapView addAnnotation:item];
            
            //轨迹点总数累计
            planPointCounts += transitStep.pointsCount;
        }
        
        //轨迹点
        temppoints = new BMKMapPoint[planPointCounts];
        int i = 0;
        for (int j = 0; j < size; j++) {
            BMKWalkingStep* transitStep = [plan.steps objectAtIndex:j];
            int k=0;
            for(k=0;k<transitStep.pointsCount;k++) {
                temppoints[i].x = transitStep.points[k].x;
                temppoints[i].y = transitStep.points[k].y;
                i++;
            }
            
        }
    }
    
    // 通过points构建BMKPolyline
    polyLine = [BMKPolyline polylineWithPoints:temppoints count:planPointCounts];
    [_mapView addOverlay:polyLine]; // 添加路线overlay
    delete []temppoints;
    [self mapViewFitPolyLine:polyLine];
    
}

- (void)doNavgatorInMap
{
    
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"选择地图" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    
    //apple自带的地图不需要判断
    UIAlertAction *action = [UIAlertAction actionWithTitle:@"苹果地图" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        
        MKMapItem *currentLocation = [MKMapItem mapItemForCurrentLocation];
        CLLocationCoordinate2D coordinate = getCoor;
        MKMapItem *toLocation = [[MKMapItem alloc] initWithPlacemark:[[MKPlacemark alloc] initWithCoordinate:coordinate addressDictionary:nil]];
        
        [MKMapItem openMapsWithItems:@[currentLocation, toLocation]                          launchOptions:@{
                                                                                                             
                                                                                                             MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving,MKLaunchOptionsShowsTrafficKey: [NSNumber numberWithBool:YES]}];
        
    }];
    
    [alert addAction:action];
    
    //判断百度地图
    
    if ( [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"baidumap://"]])
        
    {        UIAlertAction *action = [UIAlertAction actionWithTitle:@"百度地图" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        CLLocationCoordinate2D coordinate = getCoor;
        NSString *urlString = [[NSString stringWithFormat:@"baidumap://map/direction?origin={{我的位置}}&destination=latlng:%f,%f|name=目的地&mode=driving&coord_type=gcj02",coordinate.latitude, coordinate.longitude] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
        
    }];
        
        [alert addAction:action];
        
    }
    
    //判断高德地图
    NSString *appName = @"org.reactjs.native.example.ShowLoading";
    NSString *urlScheme = @"iosMap";
    CLLocationCoordinate2D coordinate = getCoor;
    if ( [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"iosamap://"]])
        
    {
        
        UIAlertAction *action = [UIAlertAction actionWithTitle:@"高德地图" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            NSString *urlString = [[NSString stringWithFormat:@"iosamap://navi?sourceApplication=%@&backScheme=%@&lat=%f&lon=%f&dev=0&style=2",appName,urlScheme,coordinate.latitude, coordinate.longitude] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
            
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];                    }];
        
        [alert addAction:action];
        
    }
    
    //判断谷歌地图
    
    if ( [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"comgooglemaps://"]])    {
        
        UIAlertAction *action = [UIAlertAction actionWithTitle:@"谷歌地图" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
            
            NSString *urlString = [[NSString stringWithFormat:@"comgooglemaps://?x-source=%@&x-success=%@&saddr=&daddr=%f,%f&directionsmode=driving",appName,urlScheme,coordinate.latitude, coordinate.longitude] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];                      [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
            
        }];
        
        [alert addAction:action];
        
    }
    
    action = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];    [alert addAction:action];
    
    [[UIApplication sharedApplication].delegate.window.rootViewController presentViewController:alert animated:YES completion:^{
        
    }];
    
}

@end

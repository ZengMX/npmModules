//
//  SelectRouteViewController.h
//  RCTCustomMapView
//
//  Created by imall on 16/9/12.
//  Copyright © 2016年 imall. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
typedef enum
{
    BUS_STYLE                 = 0,///公交
    DRIVE_STYLE               = 1,///驾车
    WALK_STYLE                = 2,///步行
}RouteStyle;

@protocol searchRouteDelegate <NSObject>

- (void)searchTheRouteWithType:(RouteStyle)style andSelectRouteLine:(BMKRouteLine *)line;

@end
@interface SelectRouteViewController : UIViewController<BMKRouteSearchDelegate>
{
    BMKRouteSearch* _routesearch;
}

@property (nonatomic) BMKUserLocation *location;//当前位置
@property (nonatomic) NSString *cityName;//当前城市
@property (nonatomic) CLLocationCoordinate2D targetLocation;//目的地位置
@property (nonatomic) CLLocationCoordinate2D startLocation;//起始点位置
@property (nonatomic) CLLocationCoordinate2D getLocation;//服务器目的地位置
@property (nonatomic,retain) NSString *targetName;//目的地名字

@property (assign,nonatomic) id<searchRouteDelegate> RouteDelegate;

@end

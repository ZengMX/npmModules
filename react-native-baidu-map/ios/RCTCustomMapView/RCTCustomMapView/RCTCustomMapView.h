//
//  RCTCustomMapView.h
//  RCTCustomMapView
//
//  Created by imall on 16/8/6.
//  Copyright © 2016年 imall. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RCTBridge.h"
#import <BaiduMapAPI_Utils/BMKUtilsComponent.h>
#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <BaiduMapAPI_Search/BMKSearchComponent.h>
#import <BaiduMapAPI_Location/BMKLocationComponent.h>
#import "RCTViewManager.h"
#import "SelectRouteViewController.h"

@interface RCTCustomMapView : RCTViewManager<BMKMapViewDelegate,BMKGeoCodeSearchDelegate,BMKLocationServiceDelegate,searchRouteDelegate,UITableViewDelegate,UITableViewDataSource>
{
    BMKMapView* _mapView;
    BMKGeoCodeSearch* _geocodesearch;
    UIView *tabView;
    BMKLocationService *_locService;
    BMKUserLocation *userLoc;
    BMKPointAnnotation *ann;
    UITableView *tab;
    int routeStyle;
    NSMutableArray *routes;
    BMKRouteLine *selectLine;
    CLLocationCoordinate2D coorT;
    CLLocationCoordinate2D getCoor;//后台请求到的地理位置
    NSString *_shopName,*_addrName;
    NSMutableArray *_poiListArray;
    NSArray *_annotationArr;
}

@end

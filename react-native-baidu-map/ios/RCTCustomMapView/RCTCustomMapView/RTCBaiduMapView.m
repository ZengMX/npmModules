//
//  RTCBaiduMapView.m
//  RCTCustomMapView
//
//  Created by imall on 16/8/6.
//  Copyright © 2016年 imall. All rights reserved.
//

#import "RTCBaiduMapView.h"
#define SCREEN_HEIGHT ([[UIScreen mainScreen] bounds].size.height)

@implementation RTCBaiduMapView

-(id)init
{
    self = [super init];
    if(self){
        
        self.userTrackingMode = BMKUserTrackingModeFollow;//538/2BMKUserTrackingModeNone,BMKUserTrackingModeFollow,BMKUserTrackingModeFollowWithHeading
        UIView *levelView = [[UIView alloc] initWithFrame:CGRectMake(10, SCREEN_HEIGHT-270, 40, 81)];
        levelView.alpha = .85;
        levelView.backgroundColor = [UIColor whiteColor];
        levelView.layer.cornerRadius = 2;
        levelView.layer.shadowColor = [UIColor grayColor].CGColor;
        levelView.layer.shadowOpacity = .3;
        levelView.layer.shadowOffset = CGSizeMake(0, 0);
        levelView.layer.shadowRadius = 3;
        
        UIButton *addBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        UIButton *minusBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        addBtn.frame = CGRectMake(0, 0, 40, 40);
        minusBtn.frame = CGRectMake(0, 41, 40, 40);
        
        [addBtn setImage:[UIImage imageNamed:@"addbg"] forState:UIControlStateNormal];
        [minusBtn setImage:[UIImage imageNamed:@"minusbg"] forState:UIControlStateNormal];
        
        [addBtn addTarget:self action:@selector(addLevel) forControlEvents:UIControlEventTouchUpInside];
        [minusBtn addTarget:self action:@selector(minusLevel) forControlEvents:UIControlEventTouchUpInside];
        
        UIView *line = [[UIView alloc] initWithFrame:CGRectMake(5, 40, 30, 0.5)];
        line.backgroundColor = [UIColor lightGrayColor];
        
        [levelView addSubview:line];
        [levelView addSubview:addBtn];
        [levelView addSubview:minusBtn];
        [self addSubview:levelView];
    }
    return self;
}

- (void)addLevel
{
    if(self.zoomLevel < 21)
    self.zoomLevel = self.zoomLevel + 1;
}

- (void)minusLevel
{
    if(self.zoomLevel > 3)
    self.zoomLevel = self.zoomLevel - 1;
}

- (void)setRegion:(BMKCoordinateRegion)region animated:(BOOL)animated
{
    if (!CLLocationCoordinate2DIsValid(region.center)) {
        return;
    }
    
    if (!region.span.latitudeDelta) {
        region.span.latitudeDelta = self.region.span.latitudeDelta;
    }
    if (!region.span.longitudeDelta) {
        region.span.longitudeDelta = self.region.span.longitudeDelta;
    }
    
    [super setRegion:region animated:animated];
}

- (void)setAnnotations:(id)locations
{
    NSArray *arr = (NSArray *)locations ;
    if(arr.count>0){
        if(arr.count == 1){
            CLLocationCoordinate2D coor;
            coor.latitude = [[[arr objectAtIndex:0] objectForKey:@"latitude"] floatValue];
            coor.longitude = [[[arr objectAtIndex:0] objectForKey:@"longitude"] floatValue];
            self.shopCoordinate = coor;
        }
        NSMutableArray *annotations = [NSMutableArray array];
        for (int i=0; i<arr.count; i++) {
            BMKPointAnnotation *annotation = [[BMKPointAnnotation alloc]init];
            CLLocationCoordinate2D coor;
            coor.latitude = [[[arr objectAtIndex:i] objectForKey:@"latitude"] floatValue];
            coor.longitude = [[[arr objectAtIndex:i] objectForKey:@"longitude"] floatValue];
            annotation.coordinate = coor;
            [annotations addObject:annotation];
            //设置弹出的AnnotationView的标题和内容
            annotation.title = [[arr objectAtIndex:i] objectForKey:@"title"]?[[arr objectAtIndex:i] objectForKey:@"title"]:@"店名";
            annotation.subtitle = [[arr objectAtIndex:i] objectForKey:@"subtitle"]?[[arr objectAtIndex:i] objectForKey:@"subtitle"]:@"地址";
            
        }
        [self addAnnotations:annotations];
        
        [self showAnnotations:annotations animated:YES];
        [self setZoomLevel:14];
    }
    
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end

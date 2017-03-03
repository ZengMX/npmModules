//
//  SelectRouteViewController.m
//  RCTCustomMapView
//
//  Created by imall on 16/9/12.
//  Copyright © 2016年 imall. All rights reserved.
//

#import "SelectRouteViewController.h"
#import "CustomCell.h"
#import <MapKit/MapKit.h>

#define SCREEN_WIDTH ([[UIScreen mainScreen] bounds].size.width)
#define SCREEN_HEIGHT ([[UIScreen mainScreen] bounds].size.height)

@interface SelectRouteViewController ()<UITableViewDelegate,UITableViewDataSource>
{
    NSMutableArray *routes;
    UITableView *routesList;
    RouteStyle *routeStyle;
    UIImageView *animateImage;
    UIButton *carItem,*busItem,*walkItem,*navRouteBtn,*startBtn;
    UILabel *endLab;
}

@property (nonatomic)NSMutableArray *availableMaps;

@end

@implementation SelectRouteViewController
//113.464857,23.11194
- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    CLLocationCoordinate2D startCoords = self.startLocation;
    CLLocationCoordinate2D endCoords = self.targetLocation;
    
    BMKPlanNode* start = [[BMKPlanNode alloc]init];
    start.pt = startCoords;
    BMKPlanNode* end = [[BMKPlanNode alloc]init];
    end.pt = endCoords;

    BMKTransitRoutePlanOption *transitRouteSearchOption = [[BMKTransitRoutePlanOption alloc]init];
    transitRouteSearchOption.city= self.cityName;
    transitRouteSearchOption.from = start;
    transitRouteSearchOption.to = end;
    BOOL flag = [_routesearch transitSearch:transitRouteSearchOption];
    if(flag)
    {
        NSLog(@"bus检索发送成功");
    }
    else
    {
        NSLog(@"bus检索发送失败");
    }

}
//113.332463,23.125421
- (void)searchRouteWithType:(int)type
{
    CLLocationCoordinate2D startCoords = self.startLocation;
    CLLocationCoordinate2D endCoords = self.targetLocation;
    BMKPlanNode* start = [[BMKPlanNode alloc]init];
    start.pt = startCoords;
    start.cityName = self.cityName;
    BMKPlanNode* end = [[BMKPlanNode alloc]init];
    end.pt = endCoords;
    //end.cityName = @"Guangzhou";
    switch (type) {
        case 0:{//公交路线  黄埔区政府
            BMKTransitRoutePlanOption *transitRouteSearchOption = [[BMKTransitRoutePlanOption alloc]init];
            transitRouteSearchOption.city= self.cityName;
            transitRouteSearchOption.from = start;
            transitRouteSearchOption.to = end;
            BOOL flag = [_routesearch transitSearch:transitRouteSearchOption];
            if(flag)
            {
                NSLog(@"bus检索发送成功");
            }
            else
            {
                NSLog(@"bus检索发送失败");
            }
        }break;
        case 1:{//驾车路线
            BMKDrivingRoutePlanOption *drivingRouteSearchOption = [[BMKDrivingRoutePlanOption alloc]init];
            drivingRouteSearchOption.drivingPolicy = 0;//0用时最短路线  1最短路程
            drivingRouteSearchOption.from = start;
            drivingRouteSearchOption.to = end;
            drivingRouteSearchOption.drivingRequestTrafficType = BMK_DRIVING_REQUEST_TRAFFICE_TYPE_NONE;//不获取路况信息
            BOOL flag = [_routesearch drivingSearch:drivingRouteSearchOption];
            if(flag)
            {
                NSLog(@"car检索发送成功");
            }
            else
            {
                NSLog(@"car检索发送失败");
            }
        }break;
        case 2:{//步行路线
            BMKWalkingRoutePlanOption *walkingRouteSearchOption = [[BMKWalkingRoutePlanOption alloc]init];
            walkingRouteSearchOption.from = start;
            walkingRouteSearchOption.to = end;
            BOOL flag = [_routesearch walkingSearch:walkingRouteSearchOption];
            if(flag)
            {
                NSLog(@"walk检索发送成功");
            }
            else
            {
                NSLog(@"walk检索发送失败");
            }
        }break;
        case 3:{//骑行路线
            BMKRidingRoutePlanOption *option = [[BMKRidingRoutePlanOption alloc]init];
            option.from = start;
            option.to = end;
            BOOL flag = [_routesearch ridingSearch:option];
            if (flag)
            {
                NSLog(@"骑行规划检索发送成功");
            }
            else
            {
                NSLog(@"骑行规划检索发送失败");
            }
        }break;
                
        default:
            break;
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    _availableMaps = [NSMutableArray array];
    routes = [NSMutableArray array];
    self.view.backgroundColor = [UIColor colorWithRed:248.0/255 green:248.0/255 blue:248.0/255 alpha:1.0];
    UIView *topBar = [[UIView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 64)];
    topBar.backgroundColor = [UIColor whiteColor];
    
    UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
    btn.frame = CGRectMake(0, 20, 60, 40);

    btn.titleLabel.font = [UIFont systemFontOfSize:14];
    [btn setImage:[UIImage imageNamed:@"backbtn"] forState:UIControlStateNormal];
    [btn addTarget:self action:@selector(goBack) forControlEvents:UIControlEventTouchUpInside];
    
    UILabel *titleLab = [[UILabel alloc] initWithFrame:CGRectMake((SCREEN_WIDTH-80)/2, 30, 80, 20)];
    titleLab.text = @"查看路线";
    titleLab.textColor = [UIColor grayColor];
    titleLab.font = [UIFont boldSystemFontOfSize:18];
    titleLab.textAlignment = NSTextAlignmentCenter;
    [topBar addSubview:titleLab];
    
    [topBar addSubview:btn];
    [self.view addSubview:topBar];
    [self initShopLocation];
    [self initStartPostionAndFinalPositionInfosView];
    
    // Do any additional setup after loading the view.
}

- (void)initStartPostionAndFinalPositionInfosView
{
    animateImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"underline"]];
    animateImage.frame = CGRectMake((SCREEN_WIDTH-180)/4.0+30, 37, 40, 3);
    UIView *line = [[UIView alloc] initWithFrame:CGRectMake(0, 39, SCREEN_WIDTH, 0.5)];
    UIView *spline = [[UIView alloc] initWithFrame:CGRectMake(40, 89, SCREEN_WIDTH-80, 0.5)];
    
    spline.backgroundColor = line.backgroundColor = [UIColor lightGrayColor];
    UIView *positionInfoView = [[UIView alloc] initWithFrame:CGRectMake(0, 64, SCREEN_WIDTH, 140)];
    positionInfoView.backgroundColor = [UIColor whiteColor];
    
    carItem = [UIButton buttonWithType:UIButtonTypeCustom];
    busItem = [UIButton buttonWithType:UIButtonTypeCustom];
    walkItem = [UIButton buttonWithType:UIButtonTypeCustom];
    
    busItem.frame = CGRectMake((SCREEN_WIDTH-180)/4.0+30, 0, 40, 36);
    carItem.frame = CGRectMake(2*(SCREEN_WIDTH-180)/4.0+70, 0, 40, 36);
    walkItem.frame = CGRectMake(3*(SCREEN_WIDTH-180)/4.0+110, 0, 40, 36);
    
    busItem.titleLabel.font = carItem.titleLabel.font = walkItem.titleLabel.font = [UIFont systemFontOfSize: 15.0];
    
    [busItem setTitle:@"公交" forState:UIControlStateNormal];
    [carItem setTitle:@"驾车" forState:UIControlStateNormal];
    [walkItem setTitle:@"步行" forState:UIControlStateNormal];
    
    [busItem setTitleColor:[UIColor colorWithRed:41.0/255 green:181.0/255 blue:193.0/255 alpha:1.0] forState:UIControlStateNormal];
    [carItem setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    [walkItem setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    
    busItem.tag = 10;
    carItem.tag = 11;
    walkItem.tag = 12;
    
    [busItem addTarget:self action:@selector(doSearchRoutes:) forControlEvents:UIControlEventTouchUpInside];
    [carItem addTarget:self action:@selector(doSearchRoutes:) forControlEvents:UIControlEventTouchUpInside];
    [walkItem addTarget:self action:@selector(doSearchRoutes:) forControlEvents:UIControlEventTouchUpInside];
    
    startBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    startBtn.frame = CGRectMake(40, 40, SCREEN_WIDTH-80, 49);
    [startBtn setTitle:@"我的位置" forState:UIControlStateNormal];
    [startBtn setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    [startBtn setContentHorizontalAlignment:UIControlContentHorizontalAlignmentLeft];
    startBtn.titleLabel.font = [UIFont systemFontOfSize: 15.0];
    UIImageView *stintPic = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"landmark"]];
    stintPic.frame = CGRectMake(12, 56, 18, 18);
    [positionInfoView addSubview:stintPic];
    [positionInfoView addSubview:startBtn];
    
    endLab = [[UILabel alloc] initWithFrame:CGRectMake(40, 90, SCREEN_WIDTH-60, 50)];
    endLab.font = [UIFont systemFontOfSize: 15.0];
    [endLab setText:_targetName];
    [endLab setTextColor:[UIColor grayColor]];
    UIImageView *etintPic = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"landmarkend"]];
    etintPic.frame = CGRectMake(12, 106, 18, 18);
    [positionInfoView addSubview:etintPic];
    [positionInfoView addSubview:endLab];
    
    UIButton *swapBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    swapBtn.frame = CGRectMake(SCREEN_WIDTH-40, 40, 40, 100);
    [swapBtn setImage:[UIImage imageNamed:@"swapimage"] forState:UIControlStateNormal];
    [swapBtn addTarget:self action:@selector(swapCoordate) forControlEvents:UIControlEventTouchUpInside];
    
    [positionInfoView addSubview:busItem];
    [positionInfoView addSubview:carItem];
    [positionInfoView addSubview:walkItem];
    [positionInfoView addSubview:animateImage];
    [positionInfoView addSubview:line];
    [positionInfoView addSubview:spline];
    [positionInfoView addSubview:swapBtn];
    
    routesList = [[UITableView alloc] initWithFrame:CGRectMake(0, 214, SCREEN_WIDTH, SCREEN_HEIGHT-184) style:UITableViewStylePlain];
    
    routesList.delegate = self;
    routesList.dataSource = self;
    routesList.separatorStyle = UITableViewCellSeparatorStyleNone;
    routesList.backgroundColor = [UIColor clearColor];
    
    [self.view addSubview:routesList];
    
    [self.view addSubview:positionInfoView];
}

//交换起始地理位置坐标
- (void)swapCoordate
{
    CLLocationCoordinate2D coor = self.startLocation;
    self.startLocation = self.targetLocation;
    self.targetLocation = coor;
    NSString *title = startBtn.titleLabel.text;
    [startBtn setTitle:endLab.text forState:UIControlStateNormal];
    [endLab setText:title];
    
    [self searchRouteWithType:routeStyle];
}

- (void)doSearchRoutes:(UIButton *)sender
{
    [busItem setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    [carItem setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    [walkItem setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    
    routeStyle = sender.tag-10;
    __block int tag = sender.tag-10;
    
    [sender setTitleColor:[UIColor colorWithRed:41.0/255 green:181.0/255 blue:193.0/255 alpha:1.0] forState:UIControlStateNormal];
    
    [UIView animateWithDuration:0.2 animations:^{
        animateImage.frame = CGRectMake((tag+1)*(SCREEN_WIDTH-180)/4.0+tag*40+30, 37, 40, 2);
    }];
    
    if(routeStyle != 1){
        if(navRouteBtn){
            [navRouteBtn removeFromSuperview];
        }
    }
    
    [self searchRouteWithType:routeStyle];
}

- (void)initShopLocation
{
    _routesearch = [[BMKRouteSearch alloc] init];
    _routesearch.delegate = self;
}

- (void)goBack
{
    [self dismissViewControllerAnimated:YES completion:^{
        
    }];
}

#pragma mark - BMKRouteSearchDelegate
//公交检索结果回调
- (void)onGetTransitRouteResult:(BMKRouteSearch*)searcher result:(BMKTransitRouteResult*)result errorCode:(BMKSearchErrorCode)error
{
    routes = [NSMutableArray arrayWithArray:result.routes];
    if(routes.count == 0){
        UIAlertController *alertCtrl = [UIAlertController alertControllerWithTitle:@"提示" message:@"无公交查询结果" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil];
        [alertCtrl addAction:action];
        [self presentViewController:alertCtrl animated:YES completion:nil];
    }
    
    [routesList reloadData];
}
//自驾检索结果回调
- (void)onGetDrivingRouteResult:(BMKRouteSearch*)searcher result:(BMKDrivingRouteResult*)result errorCode:(BMKSearchErrorCode)error
{
    BMKDrivingRouteLine *line = result.routes[0];
    routes = [NSMutableArray arrayWithArray:@[line]];
    if(routes.count == 0){
        UIAlertController *alertCtrl = [UIAlertController alertControllerWithTitle:@"提示" message:@"驾车路线查询失败" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil];
        [alertCtrl addAction:action];
        [self presentViewController:alertCtrl animated:YES completion:nil];
    }
    [routesList reloadData];
    
    if(!navRouteBtn) {
        navRouteBtn = [UIButton buttonWithType:UIButtonTypeCustom];
        navRouteBtn.frame = CGRectMake((SCREEN_WIDTH-160)/2, 319, 160, 35);
        navRouteBtn.backgroundColor = [UIColor whiteColor];
        [navRouteBtn setTitle:@"使用本机地图导航" forState:UIControlStateNormal];
        [navRouteBtn setTitleColor:[UIColor colorWithRed:41.0/255 green:181.0/255 blue:193.0/255 alpha:1.0] forState:UIControlStateNormal];
        navRouteBtn.titleLabel.font = [UIFont systemFontOfSize:13];
        [navRouteBtn addTarget:self action:@selector(doNavgatorInMap) forControlEvents:UIControlEventTouchUpInside];
    }
    
    [self.view addSubview:navRouteBtn];

}

- (void)doNavgatorInMap
{
    
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"选择地图" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    
    
    //apple自带的地图不需要判断
    UIAlertAction *action = [UIAlertAction actionWithTitle:@"苹果地图" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        
        MKMapItem *currentLocation = [MKMapItem mapItemForCurrentLocation];
        CLLocationCoordinate2D coordinate = self.getLocation;
        MKMapItem *toLocation = [[MKMapItem alloc] initWithPlacemark:[[MKPlacemark alloc] initWithCoordinate:coordinate addressDictionary:nil]];
        
        [MKMapItem openMapsWithItems:@[currentLocation, toLocation]                          launchOptions:@{
                                                                                                             
                                                                                                             MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving,MKLaunchOptionsShowsTrafficKey: [NSNumber numberWithBool:YES]}];
        
    }];
    
    [alert addAction:action];
    
    //判断百度地图
    
    if ( [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"baidumap://"]])
        
    {        UIAlertAction *action = [UIAlertAction actionWithTitle:@"百度地图" style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        CLLocationCoordinate2D coordinate = self.getLocation;
        NSString *urlString = [[NSString stringWithFormat:@"baidumap://map/direction?origin={{我的位置}}&destination=latlng:%f,%f|name=目的地&mode=driving&coord_type=gcj02",coordinate.latitude, coordinate.longitude] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
        
    }];
        
        [alert addAction:action];
        
    }
    
    //判断高德地图
    NSString *appName = @"org.reactjs.native.example.ShowLoading";
    NSString *urlScheme = @"iosMap";
    CLLocationCoordinate2D coordinate = self.getLocation;
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
    
    [self presentViewController:alert animated:YES completion:^{          
        
    }];
    
}
//步行检索结果回调
- (void)onGetWalkingRouteResult:(BMKRouteSearch*)searcher result:(BMKWalkingRouteResult*)result errorCode:(BMKSearchErrorCode)error
{
    BMKWalkingRouteLine *line = result.routes[0];
    routes = [NSMutableArray arrayWithArray:@[line]];
    if(routes.count == 0){
        UIAlertController *alertCtrl = [UIAlertController alertControllerWithTitle:@"提示" message:@"步行路线查询失败" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleCancel handler:nil];
        [alertCtrl addAction:action];
        [self presentViewController:alertCtrl animated:YES completion:nil];
    }
    [routesList reloadData];
}

#pragma mark - TableViewDelegate   TableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return routes.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    CustomCell *cell = [tableView dequeueReusableCellWithIdentifier:@"routeCell"];
    if(!cell){
        cell = [[CustomCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"routeCell"];
        
    }
    
    if(indexPath.row>0)
    {
        UIView *line = [[UIView alloc] initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, 0.3)];
        line.backgroundColor = [UIColor lightGrayColor];
        [cell addSubview:line];
    }
    
    if(routeStyle == BUS_STYLE){
        BMKTransitRouteLine *line = routes[indexPath.row];
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
        
        cell.TrainName.text = transitName;
        cell.TrainInfo.text = [NSString stringWithFormat:@"%@  |  %@  |  %@",payTime,distance,walkLength];
    } else if(routeStyle == DRIVE_STYLE) {
        BMKDrivingRouteLine *line = routes[indexPath.row];
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
//        cell.textLabel.text = [NSString stringWithFormat:@"用时最短 %@ 距离：%@",payTime,distance];
        cell.TrainName.text = driveName;
        cell.TrainInfo.text = [NSString stringWithFormat:@"%@  |  %@",payTime,distance];
    } else {
        BMKWalkingRouteLine *line = routes[indexPath.row];
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
        cell.TrainName.text = driveName;
        cell.TrainInfo.text = [NSString stringWithFormat:@"%@  |  %@",payTime,distance];
        
    }
    
    cell.textLabel.font = [UIFont systemFontOfSize:14];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 70;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.RouteDelegate && [self.RouteDelegate respondsToSelector:@selector(searchTheRouteWithType:andSelectRouteLine:)]) {
        [self.RouteDelegate searchTheRouteWithType:routeStyle andSelectRouteLine:routes[indexPath.row]];
    }
    
    [self dismissViewControllerAnimated:YES completion:^{
        
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end

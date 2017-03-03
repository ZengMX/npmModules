//
//  MapTabView.m
//  RCTCustomMapView
//
//  Created by imall on 16/9/9.
//  Copyright © 2016年 imall. All rights reserved.
//

#import "MapTabView.h"

#define SCREEN_WIDTH ([[UIScreen mainScreen] bounds].size.width)
#define SCREEN_HEIGHT ([[UIScreen mainScreen] bounds].size.height)

@implementation MapTabView

- (id)init
{
    self = [super init];
    if(self){
//        [self initView];
        self.currentIndex = 0;
        self.backgroundColor = [UIColor whiteColor];
    }
    
    return self;
}

- (void)initView
{
    self.frame = CGRectMake(0, SCREEN_HEIGHT-120, SCREEN_WIDTH, 170);
    
    UILabel *shopName = [[UILabel alloc] initWithFrame:CGRectMake(12, 18, SCREEN_WIDTH-40, 25)];
    shopName.text = @"北京同仁堂（车陂店）";
    UILabel *addressName = [[UILabel alloc] initWithFrame:CGRectMake(12, 43, SCREEN_WIDTH-40, 25)];
    addressName.text = @"天河区车陂龙口大街11号";
    addressName.textColor = [UIColor grayColor];
    shopName.font = [UIFont systemFontOfSize:15];
    addressName.font = [UIFont systemFontOfSize:12];
    UIView *line = [[UIView alloc] initWithFrame:CGRectMake(12, 75, SCREEN_WIDTH-24, 0.5)];
    line.backgroundColor = [UIColor lightGrayColor];
    
    self.searchRouteBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.searchRouteBtn.frame = CGRectMake(0, 76, SCREEN_WIDTH/2, 44);
    [self.searchRouteBtn setTitle:@"查看路线" forState:UIControlStateNormal];
    [self.searchRouteBtn setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    
    self.selfMapBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.selfMapBtn.frame = CGRectMake(SCREEN_WIDTH/2, 76, SCREEN_WIDTH/2, 44);
    [self.selfMapBtn setTitle:@"本机地图" forState:UIControlStateNormal];
    [self.selfMapBtn setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
    
    self.searchRouteBtn.titleLabel.font = self.selfMapBtn.titleLabel.font = [UIFont systemFontOfSize:14];
    
    [self addSubview:shopName];
    [self addSubview:addressName];
    [self addSubview:self.searchRouteBtn];
    [self addSubview:self.selfMapBtn];
    [self addSubview:line];
//    [self addSubview:self.errorBtn];
    
}

- (void)reloadViewWith:(UIView *)contentView
{
    for(UIView *view in self.subviews) {
        [view removeFromSuperview];
    }
    
    [self addSubview:contentView];
}

- (void)tabItemsSelect:(UIButton *)sender
{
    self.currentIndex = sender.tag-10;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end

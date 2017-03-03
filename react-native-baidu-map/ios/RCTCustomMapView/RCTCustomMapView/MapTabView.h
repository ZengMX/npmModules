//
//  MapTabView.h
//  RCTCustomMapView
//
//  Created by imall on 16/9/9.
//  Copyright © 2016年 imall. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MapTabView : UITableView

@property (assign) int currentIndex;

@property (nonatomic,retain)UIButton *searchRouteBtn;
@property (nonatomic,retain)UIButton *selfMapBtn;
@property (nonatomic,retain)UIButton *errorBtn;

- (void)reloadViewWith:(UIView *)contentView;

@end

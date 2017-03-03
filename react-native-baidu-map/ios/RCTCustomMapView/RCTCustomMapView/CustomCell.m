//
//  CustomCell.m
//  RCTCustomMapView
//
//  Created by imall on 16/9/14.
//  Copyright © 2016年 imall. All rights reserved.
//

#import "CustomCell.h"
#define SCREEN_WIDTH ([[UIScreen mainScreen] bounds].size.width)

@implementation CustomCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:nil];
    if(self){
        [self initContentView];
    }
    
    return self;
}

- (void)initContentView
{
    self.backgroundColor = [UIColor whiteColor];
    self.TrainName = [[UILabel alloc] initWithFrame:CGRectMake(20, 10, self.frame.size.width-60, 25)];
    self.TrainName.font = [UIFont boldSystemFontOfSize:15];
    
    self.TrainInfo = [[UILabel alloc] initWithFrame:CGRectMake(20, 35, self.frame.size.width-60, 25)];
    self.TrainInfo.font = [UIFont systemFontOfSize:13];
    self.TrainInfo.textColor = [UIColor grayColor];
    
//    UIView *line = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, 0.3)];
//    line.backgroundColor = [UIColor lightGrayColor];
    
    UIImageView *rightImage = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rightarrow"]];
    rightImage.frame = CGRectMake(SCREEN_WIDTH-22, 29, 6, 11);
    
    UIView *leftView = [[UIView alloc] initWithFrame:CGRectMake(0, 15, 3, 15)];
    leftView.backgroundColor = [UIColor colorWithRed:41.0/255 green:181.0/255 blue:193.0/255 alpha:1.0];
    
    [self addSubview:leftView];
    [self addSubview:rightImage];
    [self addSubview:self.TrainInfo];
    [self addSubview:self.TrainName];
//    [self addSubview:line];
}

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end

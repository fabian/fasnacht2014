//
//  ViewController.h
//  Fasnacht
//
//  Created by Fabian on 11.12.13.
//  Copyright (c) 2013 Fabian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>

@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UISlider *sliderB;
@property (weak, nonatomic) IBOutlet UISlider *sliderC;
@property (weak, nonatomic) IBOutlet UISlider *sliderD;
@property (weak, nonatomic) IBOutlet UISlider *sliderE;

@property double thetaChannel;
@property double thetaBrightness;

@property int channel;

@end

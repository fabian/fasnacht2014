//
//  ViewController.h
//  Fasnacht
//
//  Created by Fabian on 11.12.13.
//  Copyright (c) 2013 Fabian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>
#import <AVFoundation/AVFoundation.h>
#import <CoreLocation/CoreLocation.h>

@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UISwitch *sliderB;
@property (weak, nonatomic) IBOutlet UISwitch *sliderD;
@property (weak, nonatomic) IBOutlet UISwitch *sliderE;
@property (weak, nonatomic) IBOutlet UISwitch *sliderH;

@property (strong, nonatomic) CLBeaconRegion *beaconRegion;
@property (strong, nonatomic) CLLocationManager *locationManager;

@property double thetaLeft;
@property double thetaRight;

@property int channel;

@end

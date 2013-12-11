//
//  ViewController.m
//  Fasnacht
//
//  Created by Fabian on 11.12.13.
//  Copyright (c) 2013 Fabian. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@property (weak, nonatomic) IBOutlet UISlider *sliderB;
@property (weak, nonatomic) IBOutlet UISlider *sliderC;
@property (weak, nonatomic) IBOutlet UISlider *sliderD;
@property (weak, nonatomic) IBOutlet UISlider *sliderE;

@property (weak, nonatomic) IBOutlet UILabel *labelB;
@property (weak, nonatomic) IBOutlet UILabel *labelC;
@property (weak, nonatomic) IBOutlet UILabel *labelD;
@property (weak, nonatomic) IBOutlet UILabel *labelE;

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)sliderChangedB:(UISlider *)slider {
	self.labelB.text = [NSString stringWithFormat:@"%.0f Hz", slider.value];
}

- (IBAction)sliderChangedC:(UISlider *)slider {
	self.labelC.text = [NSString stringWithFormat:@"%.0f Hz", slider.value];
}

- (IBAction)sliderChangedD:(UISlider *)slider {
	self.labelD.text = [NSString stringWithFormat:@"%.0f Hz", slider.value];
}

- (IBAction)sliderChangedE:(UISlider *)slider {
	self.labelE.text = [NSString stringWithFormat:@"%.0f Hz", slider.value];
}


@end

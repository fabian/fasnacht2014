//
//  ViewController.m
//  Fasnacht
//
//  Created by Fabian on 11.12.13.
//  Copyright (c) 2013 Fabian. All rights reserved.
//

#import "ViewController.h"

const double sampleRate = 44100;
const double amplitude = 0.75;

OSStatus RenderTone(    void *inRefCon,
                        AudioUnitRenderActionFlags 	*ioActionFlags,
                        const AudioTimeStamp 		*inTimeStamp,
                        UInt32 						inBusNumber,
                        UInt32 						inNumberFrames,
                        AudioBufferList 			*ioData)
{
	ViewController *viewController = (__bridge ViewController *)inRefCon;
    //NSLog(@"Frames %i, %i", inNumberFrames, viewController.modeCounter * 1024 / 256);
    
    int i = (viewController.modeCounter * 1024 / inNumberFrames);
    int speed = 32;
    if (i % speed == 0) {
        
        NSLog(@"Time Interval: %f", [[NSDate date] timeIntervalSince1970] - viewController.lastTime);
        viewController.lastTime = [[NSDate date] timeIntervalSince1970];
        
        if ([viewController.mode isEqualToString:@"Random"]) {
            
            int randomB = (arc4random() % ((unsigned)RAND_MAX + 1));
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderB.on = (randomB % 5 == 0); } );
            int randomD = (arc4random() % ((unsigned)RAND_MAX + 1));
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderD.on = (randomD % 5 == 0); } );
            int randomE = (arc4random() % ((unsigned)RAND_MAX + 1));
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderE.on = (randomE % 5 == 0); } );
            int randomH = (arc4random() % ((unsigned)RAND_MAX + 1));
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderH.on = (randomH % 5 == 0); } );
            
        } else if ([viewController.mode isEqualToString:@"Loop"]) {
            
            int max = speed * 4;
            BOOL onB = i % max == speed * 0;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderB.on = onB; } );
            BOOL onD = i % max == speed * 1;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderD.on = onD; } );
            BOOL onE = i % max == speed * 2;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderE.on = onE; } );
            BOOL onH = i % max == speed * 3;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderH.on = onH; } );
            
        } else if ([viewController.mode isEqualToString:@"Chain"]) {
            
            int max = speed * viewController.stepperTotal.value;
            BOOL on = i % max == speed * viewController.stepperPosition.value;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderB.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderD.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderE.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderH.on = on; } );
            
        } else if ([viewController.mode isEqualToString:@"Blink"]) {
            
            int max = speed * 2;
            BOOL on = i % max == 0;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderB.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderD.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderE.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderH.on = on; } );
            
        } else if ([viewController.mode isEqualToString:@"Switch"]) {
            
            int max = speed * 2;
            BOOL on = i % max == 0;
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderB.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderD.on = on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderE.on = !on; } );
            dispatch_async(dispatch_get_main_queue(), ^{ viewController.sliderH.on = !on; } );
            
        } else {
            
        }
        
    }

    viewController.modeCounter = (viewController.modeCounter + 1) % 8192;

    // default channel B and D
    int leftFrequency = 0;
    if (!viewController.sliderB.on && !viewController.sliderD.on) {
        leftFrequency = 150;
    } else if (viewController.sliderB.on && !viewController.sliderD.on) {
        leftFrequency = 250;
    } else if (!viewController.sliderB.on && viewController.sliderD.on) {
        leftFrequency = 350;
    } else {
        leftFrequency = 450;
    }

    // channel E and H
    int rightFrequency = 0;
    if (!viewController.sliderE.on && !viewController.sliderH.on) {
        rightFrequency = 150;
    } else if (viewController.sliderE.on && !viewController.sliderH.on) {
        rightFrequency = 250;
    } else if (!viewController.sliderE.on && viewController.sliderH.on) {
        rightFrequency = 350;
    } else {
        rightFrequency = 450;
    }
    
    double thetaLeft = viewController.thetaLeft;
	double thetaLeftIncrement = 2.0 * M_PI * leftFrequency / sampleRate;
    
	// Left audio channel
	int channel = 0;
	Float32 *buffer = (Float32 *)ioData->mBuffers[channel].mData;

	for (UInt32 frame = 0; frame < inNumberFrames; frame++)
	{
		buffer[frame] = sin(thetaLeft) * amplitude;
		
		thetaLeft += thetaLeftIncrement;
		if (thetaLeft > 2.0 * M_PI)
		{
			thetaLeft -= 2.0 * M_PI;
		}
	}
    
	// Store the theta back in the view controller
	viewController.thetaLeft = thetaLeft;

    // Right audio channel
    channel = 1;
	buffer = (Float32 *)ioData->mBuffers[channel].mData;
    
    double thetaRight = viewController.thetaRight;
	double thetaRightIncrement = 2.0 * M_PI * rightFrequency / sampleRate;
    
	// Generate the samples
	for (UInt32 frame = 0; frame < inNumberFrames; frame++)
	{
		buffer[frame] = sin(thetaRight) * amplitude;
		
		thetaRight += thetaRightIncrement;
		if (thetaRight > 2.0 * M_PI)
		{
			thetaRight -= 2.0 * M_PI;
		}
	}

	// Store the theta back in the view controller
	viewController.thetaRight = thetaRight;

    // next channel next time
    viewController.channel = (viewController.channel + 1) % 40;
    
	return noErr;
}

@interface ViewController () <UIActionSheetDelegate>

@property (weak, nonatomic) IBOutlet UISwitch *labelB;
@property (weak, nonatomic) IBOutlet UISwitch *labelD;
@property (weak, nonatomic) IBOutlet UISwitch *labelE;
@property (weak, nonatomic) IBOutlet UISwitch *labelH;

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.mode = @"Manual";
    
    NSError *error = nil;
    
    AVAudioSession *session = [AVAudioSession sharedInstance];
    [session setCategory: AVAudioSessionCategoryPlayback error:&error];
    if (error)
        NSLog(@"error setting up audio session: %@", [error localizedDescription]);
    
    [session setActive:YES error:&error];
    if (error)
        NSLog(@"error activating audio session: %@", [error localizedDescription]);
    
    AudioComponentInstance toneUnit;
    
	// Configure the search parameters to find the default playback output unit
	// (called the kAudioUnitSubType_RemoteIO on iOS but
	// kAudioUnitSubType_DefaultOutput on Mac OS X)
	AudioComponentDescription defaultOutputDescription;
	defaultOutputDescription.componentType = kAudioUnitType_Output;
	defaultOutputDescription.componentSubType = kAudioUnitSubType_RemoteIO;
	defaultOutputDescription.componentManufacturer = kAudioUnitManufacturer_Apple;
	defaultOutputDescription.componentFlags = 0;
	defaultOutputDescription.componentFlagsMask = 0;
	
	// Get the default playback output unit
	AudioComponent defaultOutput = AudioComponentFindNext(NULL, &defaultOutputDescription);
	NSAssert(defaultOutput, @"Can't find default output");
	
	// Create a new unit based on this that we'll use for output
	OSErr err = AudioComponentInstanceNew(defaultOutput, &toneUnit);
	NSAssert1(toneUnit, @"Error creating unit: %ld", err);
	
	// Set our tone rendering function on the unit
	AURenderCallbackStruct input;
	input.inputProc = RenderTone;
	input.inputProcRefCon = (__bridge void *)(self);
	err = AudioUnitSetProperty(toneUnit,
                               kAudioUnitProperty_SetRenderCallback,
                               kAudioUnitScope_Input,
                               0,
                               &input,
                               sizeof(input));
	NSAssert1(err == noErr, @"Error setting callback: %ld", err);
	
	// Set the format to 32 bit, single channel, floating point, linear PCM
	const int four_bytes_per_float = 4;
	const int eight_bits_per_byte = 8;
	AudioStreamBasicDescription streamFormat;
	streamFormat.mSampleRate = sampleRate;
	streamFormat.mFormatID = kAudioFormatLinearPCM;
	streamFormat.mFormatFlags =
    kAudioFormatFlagsNativeFloatPacked | kAudioFormatFlagIsNonInterleaved;
	streamFormat.mBytesPerPacket = four_bytes_per_float;
	streamFormat.mFramesPerPacket = 1;
	streamFormat.mBytesPerFrame = four_bytes_per_float;
	streamFormat.mChannelsPerFrame = 2;
	streamFormat.mBitsPerChannel = four_bytes_per_float * eight_bits_per_byte;
	err = AudioUnitSetProperty (toneUnit,
                                kAudioUnitProperty_StreamFormat,
                                kAudioUnitScope_Input,
                                0,
                                &streamFormat,
                                sizeof(AudioStreamBasicDescription));
	NSAssert1(err == noErr, @"Error setting stream format: %ld", err);
    
    // required for background
    UInt32 maximumFramesPerSlice = 4096;
    
    AudioUnitSetProperty (
                          toneUnit,
                          kAudioUnitProperty_MaximumFramesPerSlice,
                          kAudioUnitScope_Global,
                          0,                        // global scope always uses element 0
                          &maximumFramesPerSlice,
                          sizeof (maximumFramesPerSlice)
                          );
    
    // Stop changing parameters on the unit
    err = AudioUnitInitialize(toneUnit);
    NSAssert1(err == noErr, @"Error initializing unit: %ld", err);
    
    // Start playback
    err = AudioOutputUnitStart(toneUnit);
    NSAssert1(err == noErr, @"Error starting unit: %ld", err);
    
    BOOL audioSessionActivated = [self setupAudioSession];
    NSAssert (audioSessionActivated == YES, @"Unable to set up audio session.");
    
    // start iBeacons scan
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    
    NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:@"B9407F30-F5F8-466E-AFF9-25556B57FE6D"];
    self.beaconRegion = [[CLBeaconRegion alloc] initWithProximityUUID:uuid major:21023 minor:64576 identifier:@"homebase.Estimote"];
    self.beaconRegion.notifyEntryStateOnDisplay = YES;
    [self.locationManager startMonitoringForRegion:self.beaconRegion];
}



// Set up the audio session for this app.
- (BOOL) setupAudioSession {
    
    AVAudioSession *mySession = [AVAudioSession sharedInstance];
    
    // Specify that this object is the delegate of the audio session, so that
    //    this object's endInterruption method will be invoked when needed.
    [mySession setDelegate: self];
    
    // Assign the Playback category to the audio session. This category supports
    //    audio output with the Ring/Silent switch in the Silent position.
    NSError *audioSessionError = nil;
    [mySession setCategory: AVAudioSessionCategoryPlayback error: &audioSessionError];
    if (audioSessionError != nil) {NSLog (@"Error setting audio session category."); return NO;}
    
    // Activate the audio session
    [mySession setActive: YES error: &audioSessionError];
    if (audioSessionError != nil) {NSLog (@"Error activating the audio session."); return NO;}
    
    return YES;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)showDisplayModes:(id)sender {
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Manual", @"Chain", @"Blink", @"Loop", @"Switch", @"Random", nil];
    
    [actionSheet showInView:self.view];
}

- (IBAction)toggleAll:(id)sender {
    [self.sliderB setOn:self.switchAll.on animated:YES];
    [self.sliderD setOn:self.switchAll.on animated:YES];
    [self.sliderE setOn:self.switchAll.on animated:YES];
    [self.sliderH setOn:self.switchAll.on animated:YES];
}

- (IBAction)totalChanged:(id)sender {
    self.labelTotal.text = [NSString stringWithFormat:@"Total: %.f", self.stepperTotal.value];
}
- (IBAction)positionChanged:(id)sender {
    self.labelPosition.text = [NSString stringWithFormat:@"Position: %.f", self.stepperPosition.value];
}


- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    NSString *mode = [actionSheet buttonTitleAtIndex:buttonIndex];
    if (![mode isEqualToString:@"Cancel"]) {
        self.mode = [actionSheet buttonTitleAtIndex:buttonIndex];
    }
    [self.buttonMode setTitle:[NSString stringWithFormat:@"Display Mode: %@", self.mode] forState:UIControlStateNormal];
}

- (void)locationManager:(CLLocationManager *)manager didDetermineState:(CLRegionState)state forRegion:(CLRegion *)region
{
    CLBeaconRegion *beaconRegion = (CLBeaconRegion *) region;
    
    if (state == CLRegionStateInside) {
        [self.locationManager startRangingBeaconsInRegion:beaconRegion];
    } else {
        [self.locationManager stopRangingBeaconsInRegion:beaconRegion];
        
        //self.sliderB.on = false;
    }
}

-(void)locationManager:(CLLocationManager *)manager didRangeBeacons:(NSArray *)beacons inRegion:(CLBeaconRegion *)region
{
    NSString * const proximities[] = {
        [CLProximityFar] = @"far",
        [CLProximityImmediate] = @"immediate",
        [CLProximityNear] = @"near",
        [CLProximityUnknown] = @"unknown"
    };
    
    for (CLBeacon *eachBeacon in beacons) {
        
        if (eachBeacon.proximity == CLProximityNear || eachBeacon.proximity == CLProximityImmediate) {
            //self.sliderB.on = true;
        } else {
            //self.sliderB.on = false;
        }
    }
}


@end

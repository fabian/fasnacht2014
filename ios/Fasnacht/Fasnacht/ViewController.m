//
//  ViewController.m
//  Fasnacht
//
//  Created by Fabian on 11.12.13.
//  Copyright (c) 2013 Fabian. All rights reserved.
//

#import "ViewController.h"

const double sampleRate = 44100;
const double amplitude = 0.25;

OSStatus RenderTone(    void *inRefCon,
                        AudioUnitRenderActionFlags 	*ioActionFlags,
                        const AudioTimeStamp 		*inTimeStamp,
                        UInt32 						inBusNumber,
                        UInt32 						inNumberFrames,
                        AudioBufferList 			*ioData)
{
	ViewController *viewController = (__bridge ViewController *)inRefCon;
    
    // default channel B
    int channelFrequency = 150;
    int brightnessFrequency = viewController.sliderB.value;

    if (viewController.channel > 30) {
        // channel E
        channelFrequency = 450;
        brightnessFrequency = viewController.sliderE.value;
    } else if (viewController.channel > 20) {
        // channel D
        channelFrequency = 350;
        brightnessFrequency = viewController.sliderD.value;
    } else if (viewController.channel > 10) {
        // channel C
        channelFrequency = 250;
        brightnessFrequency = viewController.sliderC.value;
    }
    
    double thetaChannel = viewController.thetaChannel;
	double thetaChannelIncrement = 2.0 * M_PI * channelFrequency / sampleRate;
    
	// Left audio channel (which channel)
	int channel = 0;
	Float32 *buffer = (Float32 *)ioData->mBuffers[channel].mData;

	for (UInt32 frame = 0; frame < inNumberFrames; frame++)
	{
		buffer[frame] = sin(thetaChannel) * amplitude;
		
		thetaChannel += thetaChannelIncrement;
		if (thetaChannel > 2.0 * M_PI)
		{
			thetaChannel -= 2.0 * M_PI;
		}
	}
    
	// Store the theta back in the view controller
	viewController.thetaChannel = thetaChannel;

    // Right audio channel (light brightness)
    channel = 1;
	buffer = (Float32 *)ioData->mBuffers[channel].mData;
    
    double thetaBrightness = viewController.thetaBrightness;
	double thetaBrightnessIncrement = 2.0 * M_PI * brightnessFrequency / sampleRate;
    
	// Generate the samples
	for (UInt32 frame = 0; frame < inNumberFrames; frame++)
	{
		buffer[frame] = sin(thetaBrightness) * amplitude;
		
		thetaBrightness += thetaBrightnessIncrement;
		if (thetaBrightness > 2.0 * M_PI)
		{
			thetaBrightness -= 2.0 * M_PI;
		}
	}

	// Store the theta back in the view controller
	viewController.thetaBrightness = thetaBrightness;

    // next channel next time
    viewController.channel = (viewController.channel + 1) % 40;
    
	return noErr;
}

@interface ViewController ()

@property (weak, nonatomic) IBOutlet UILabel *labelB;
@property (weak, nonatomic) IBOutlet UILabel *labelC;
@property (weak, nonatomic) IBOutlet UILabel *labelD;
@property (weak, nonatomic) IBOutlet UILabel *labelE;

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
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
    
    // Stop changing parameters on the unit
    err = AudioUnitInitialize(toneUnit);
    NSAssert1(err == noErr, @"Error initializing unit: %ld", err);
    
    // Start playback
    err = AudioOutputUnitStart(toneUnit);
    NSAssert1(err == noErr, @"Error starting unit: %ld", err);
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

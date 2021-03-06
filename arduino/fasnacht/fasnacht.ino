// Written for SparkFun EL Sequencer
// https://www.sparkfun.com/products/11323

int my_putc( char c, FILE *t) {
    Serial.write( c );
}

void setup() {
    // The EL channels are on pins 2 through 9
    // Initialize the pins as outputs
    pinMode(0, INPUT);
    pinMode(1, INPUT);
    pinMode(2, OUTPUT);    // channel A    
    pinMode(3, OUTPUT);    // channel B     
    pinMode(4, OUTPUT);    // channel C
    pinMode(5, OUTPUT);    // channel D        
    pinMode(6, OUTPUT);    // channel E
    pinMode(7, OUTPUT);    // channel F
    pinMode(8, OUTPUT);    // channel G
    pinMode(9, OUTPUT);    // channel H
    // We also have two status LEDs, pin 10 on the Escudo, 
    // and pin 13 on the Arduino itself
    pinMode(10, OUTPUT);
    pinMode(13, OUTPUT);
    Serial.begin(9600);
    Serial.println("Fasnacht 2014");
    
    fdevopen( &my_putc, 0);
}

void loop() 
{
    unsigned long start,        //time when sampling started
                  finish,       //time when sampling ended
                  time,         //sampling time
                  ValueCount,   //counts Value average crossings
                  ChannelCount, //counts Channel average crossings
                  ValueFrq,     //Final Input freq of Value data
                  ChannelFrq;   //Final Input freq of Channel data
                  
    unsigned int Ctr,           //counter
                 ValueState,    //set if Value is bigger than its Average
                 ChannelState,  //set if Channel is bigger than its Average
                 ValueLast,     //last state of ValuePassed   
                 ChannelLast,   //last state of ChannelPassed 
                 Value,         //analog input of data
                 Channel = 0,       //analog input of channel
                 ValueAvg,      //voltage average
                 ChannelAvg,    //voltage average 
                 ValueHighest = 0,     //peak value
                 ChannelHighest = 0,   //peak value
                 ValueLowest = 1023,   //peak value
                 ChannelLowest = 1023; //peak value
                 
    int brightness; //brighness data

    digitalWrite(13, 1);

    start = millis();  //get arduino uptimes (unsigned long): max 50days :P
    ValueCount = 0;
    ChannelCount = 0;

    for (Ctr = 0; Ctr < 200; Ctr++) {
        //get input voltage
        Value = analogRead(3);   // 2 = Audio left -> data
        Channel = analogRead(2); // 2 = Audio right -> channel

        //save peak values
        if (Value > ValueHighest) ValueHighest = Value;
        if (Value < ValueLowest)  ValueLowest = Value;
        if (Channel > ChannelHighest) ChannelHighest = Channel;
        if (Channel < ChannelLowest)  ChannelLowest = Channel;
         
        //calculate voltage average   
        ValueAvg = (ValueHighest + ValueLowest) / 2;
        ChannelAvg = (ChannelHighest   + ChannelLowest) / 2;
        
        //check if input is higher or lower than its average
        if (Value > ValueAvg) ValueState = 1;
        else ValueState = 0;
        if (Channel > ChannelAvg) ChannelState = 1;
        else ChannelState = 0;
        
        //count number of average crossings
        if (ValueState != ValueLast) ValueCount++;
        if (ChannelState != ChannelLast) ChannelCount++;
        
        //save state
        ValueLast = ValueState;
        ChannelLast = ChannelState;
    }

    //get sampling time
    finish = millis();
    time = finish - start;
    
    //calculate freq
    ValueFrq = ValueCount * 100 / (time / 10) / 2;
    ChannelFrq = ChannelCount * 100 / (time / 10) / 2;
    
    //get channel data and write output LEFT AUIO CHANNEL
    //printf("Left Max:%4i Min:%4i Count:%4i | Right Max:%4i Min:%4i Count:%4i | Frq: ", ValueHighest, ValueLowest, ValueCount, ChannelHighest, ChannelLowest, ChannelCount);
    //Serial.print(ValueFrq);
    //Serial.print(" ");
    //Serial.print(ChannelFrq);
    //Serial.println(" ");
    if( (ValueFrq >= 100) && (ValueFrq <= 199) ) {
      digitalWrite(3, 1); //B
      digitalWrite(5, 0); //D
      //Serial.println("B OFF D OFF ");
    } else if( (ValueFrq >= 200) && (ValueFrq <= 299) ) {
      digitalWrite(3, 0); //B
      digitalWrite(5, 0); //D
      //Serial.println("B ON  D OFF ");
    } else if(((ValueFrq >= 300) && (ValueFrq <= 399)) || ValueFrq > 499) {
      digitalWrite(3, 1); //B
      digitalWrite(5, 1); //D
      //Serial.println("B OFF D ON  ");
    } else if( (ValueFrq >= 400) && (ValueFrq <= 499) ) {
      digitalWrite(3, 0); //B
      digitalWrite(5, 1); //D
      //Serial.println("B ON  D ON  "); 
    } else {
      //Serial.println(" UNKNOWN B / D");
    }

    //get channel data and write output RIGHT AUIO CHANNEL
    if( (ChannelFrq >= 100) && (ChannelFrq <= 199) ) {
      digitalWrite(6, 1); //E
      digitalWrite(9, 0); //H
      //Serial.println("E OFF H OFF ");
    } else if( (ChannelFrq >= 240) && (ChannelFrq <= 299) ) {
      digitalWrite(6, 0); //E
      digitalWrite(9, 0); //H
      //Serial.println("E ON  H OFF ");
    } else if(((ChannelFrq >= 300) && (ChannelFrq <= 399)) || ChannelFrq > 499) {
      digitalWrite(6, 1); //E
      digitalWrite(9, 1); //H
      //Serial.println("E OFF H ON  ");
    } else if( (ChannelFrq >= 400) && (ChannelFrq <= 499) ) {
      digitalWrite(6, 0); //E
      digitalWrite(9, 1); //H
      //Serial.println("E ON  H ON  "); 
    } else {
      //Serial.println(" UNKNOWN E / H");
    }

    /*
    Serial.println("");
    //e
    Serial.print("Channel: ");
    Serial.println(ChannelFrq);
    Serial.print("Value: ");
    Serial.println(ValueFrq);
    Serial.print("Brightness: ");
    Serial.println(brightness);
    */
    //Serial.println("---------------");
    

//    analogWrite(3, brightness);
//    analogWrite(5, brightness);  

    digitalWrite(13, 0);
}


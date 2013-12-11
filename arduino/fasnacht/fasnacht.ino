// Written for SparkFun EL Sequencer
// https://www.sparkfun.com/products/11323

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
}

void loop() 
{
    unsigned long start, count, finish, time, frq;
    unsigned int x, b, last, value, avg, highest = 0, lowest = 1023;
    int brightness;

    digitalWrite(13, 1);

    start = millis();
    count = 0;

    for (x = 0; x < 500; x++) {
        value = analogRead(3);
        if (value > highest) {
            highest = value;
        }
        if (value < lowest) {
            lowest = value;
        }
        avg = (highest + lowest) / 2;
        
        if (value > avg) {
          b = 1;
        } else {
          b = 0;
        }
        if (b != last) {
            count++;
        }
        last = b;
    }

    finish = millis();
    time = finish - start;
    frq = count * 100 / time / 2 * 10;
    brightness = (int) (frq - 100) / 2;

    if (brightness >= 255) {
        brightness = 255;
    }
    if (brightness < 0) {
        brightness = 0;
    }

    Serial.print("Frequenz: ");
    Serial.println(frq);
    Serial.print("Brightness: ");
    Serial.println(brightness);

    analogWrite(3, brightness);
    analogWrite(5, brightness);

    digitalWrite(13, 0);
}


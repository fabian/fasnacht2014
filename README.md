Fasnacht 2014
=============

Mode 1: ("dimmer")
-------

| Channel | Freq. thresholds |
| -------- | ---------------- |
| Channel B | 140Hz - 160Hz |
| Channel D | 240Hz - 260Hz |
| Channel E | 340Hz - 360Hz |
| Channel H | 440Hz - 460Hz |

Mode 2: ("quad speed")
-------

| Channel B / E | Channel D / H | Freq. thresholds |
| ------------- | ------------- | ---------------- |
|           off |           off |    140Hz - 160Hz |
|            on |           off |    240Hz - 260Hz |
|           off |            on |    340Hz - 360Hz |
|            on |            on |    440Hz - 460Hz |

Channel B und D werden über den linken Audio-Kanal, Channel E und H über den rechten Audio-Kanal gesteuert. Im vergleich zum Mode 1 kann ein Kanal 4 mal schneller in bzw. ausgeschaltet werden, dafür wird auf die Möglichkeit vom "dimmen" verzichtet.

Channel location:
--------------

| Channel | Location |
| ------- | -------- |
|       B | Beine    |
|       D | De Frack |
|       E | Ermel    |
|       H | Hut      |

Arduino IDE settings:
=============

Board: Arduino Pro or Pro Mini (3.3V, 8MHz) w/ ATmega328

Programmer: USBAsp

Use 'Upload using Programmer'

# Wearable Braille

**UNDER DEVELOPMENT**

Wearable Braille input prototypes with different interaction methods. This project holds Braille Input layouts to be tested on a Smartwatch. Only Tech Items are working so far.

So far **User Study** and **Demo Apps** sessions are not implemented. In **Just Try** session, 4 input methods can be found:
 - _Touch_: Simple buttons layout, similar to [BraillÉcran](https://github.com/mateuswetah/BrailleEcran)'s logic.
 - _Swipe_: Directional implementation. Swiping from any point of the screen to a certain direction activates the Button arranged into that direction.
 - _Connect_: Based on IPPITSU and [SwiftBraille](http://en.swiftbraille.com/). Allows connecting dots to compose the braille cell on finger release.
 - _Pressure_: [WatchMI](https://github.com/tcboy88/WatchMI)'s Pressure, adapted for activating a button once the blue line enters the button region. 
 - _Serial_: Adapted from the concept of [TypeInBraille](http://www.everywaretechnologies.com/apps/typeinbraille). Instead of the three-finger click for skiping a line or confirming the character, a Swype to Top is used here.
 -_Perkins_: Similar to Serial, but with column-by-column insertion, accessing user's perkins keyboard habilities. Three dots completion is performed by swipe up (activate) or swipe down (deactivate).

Forked from [WatchMI](https://github.com/tcboy88/WatchMI) project.

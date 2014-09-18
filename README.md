objectivve # HciClient
—— Extend the Sensory Ability of Desktop Computer using Mobile Device (Client Side)

## Description

This project is the client side of my coursework for Human Computer Interaction class.

The project, made up of server side and client side, intends to extend the sensory ability of desktop computer, by using mobile devices like an Android Phone. For instance, user can shake the phone in a specific orientation to control the volume of playing movie on computor up and down, or rollover the phone to pause the playing movie.

The reasons for coming up with this project consist of these aspects as follows:

 * Desktop computer is short of sensory ability. Microphone, camera and touchpad are not standard equipped.
 * The sensors of mobile devices are abundant and are often standard epuipped.
 * The way to combine desktop computer and mobile device is convenient.

This project is a prototype of sensor combination between desktop computer browser and mobile phone. It combines the two by using Internet and QR code. User can use gesture effected on mobile phone to control the behaviour of movie playing in the computer.

The figure blow illustrates the framework of this project (both server side and client side)

## Technical Detail

The client side is now implemented as an Android App developed on Android API 17. It uses gravity acceleration sensor on Android phone to detect user's gesture. By defining a series of gestures ahead of time, a behaviour translator translates the gesture to a spectific meaning command. At last, an HTTP request will be triggered to send out the command.

### Gravity Acceleration Sensor

I use gravity sensor to get acceleration value of gravity from 3 orientation: X, Y, Z. Z stands for the vertical direction gravity acceleration and is 9.8m/s^2 as often. Its positive and negative states represent the position putting the phone. X represents the level of inclination holding the phone. I use this value to control volume up or down.

The project only use gravity acceleration sensor here to detect gestures, since I choose the series of gestures that are easy to detect and that's enough to distinguish them from each other. If complex gestures are to detect, other sensors on mobile phone should be included into the program, e.g. orientation acceleration sensor, vibration sensor.

But I have left the interfaces in the code to add new sensors in. It should be convenient to modify. The code below selects the gravity sensor from all sensors available in device. Only repleacing the name of "Gravity" can select other sensors.

```java
	List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
	List<SensorLabel> labels = new ArrayList<SensorLabel>(sensors.size());
	Log.i(getClass().getCanonicalName(), "Sensors: " + sensors.size());
	for (Sensor s : sensors) {
		if(s.getName().equals("Gravity")) {
			Log.i(getClass().getCanonicalName(), "Sensor: " + s.getName());
			labels.add(new SensorLabel(s));
		}
	}
```
### Gesture Detector

Gesture detector is a dozen of code judging the gesture performing by the user according to values extracted from the sensor. In this project, I used a simplest method to generate results, since the objective of this coursework is to build a platform to combine computer and mobile sensor, not to produce an app available for market sale.

The detailed logic of judgement is described below:

 * when the Z component of gravity acceleration sensor is positive, we assume the phone is face up now.
 * when the Z component of gravity acceleration sensor is negative, we assume the phone is face down now.
 * when the X component of gravity acceleration sensor reaches +5m/s^2, we assume the phone is inclined to right.
 * when the X component of gravity acceleration sensor reaches -5m/s^2, we assume the phone is inclined to left.
 
I left interfaces in the code as well and new logic could be easy to insert.

### Behavior Translator

The component of behavior translator here is responsible for translating the gestures judged by gesture detector to playing control command. The congruent relationship below is uesd in this app.

 * face up -> play
 * face down -> pause
 * inclined to right -> volume up
 * incliend to left -> volume down
 
And besides, when the user enters the app, a play & fullscreen command will be generated. Correspondingly, when left, a pause command will be generated.

### HTTP Request Trigger

The component sends different HTTP signals to back end of server side. For play/pause command, every different command will triggered once. But for volume up/volume down, every command will triggered at a frequency of 1 second.

In this project, I use different URLs to represent different kinds of behaviour, that's because it's easy to implement. But for larger engineering, I suppose to use JSON format data to store information and transfered to the same URL, since it's easy for code to extend and organize.

Since it is only a prototype, the server and client are communicating inside a LAN. It is assumed that the server side is deplyed on IP address: 192.168.200.167. But I am quite sure it can be used throuth Internet, just by modifying the IP address to Internet one's. 

## Steps of Use

 1. Git clone and build this repository, or simply  download the apk of this repository from [here]().
 * Install the app on Android phone. The version of Android system should be newer than 2.2.
 * Prepare and mobile browser which has the functionality of QR code scanning (e.g. QQ Browser or UC Browser) on your mobile phone.
 * Run the server side on server.
 * On a desktop computer, open a browser and visit the server side website at http://192.168.200.167:5000
 * Open the browser and scan the QR code displayed on desktop computer browser. I comfirm page will be loaded in mobile phone browser.
 * Touch the button to make sure to combine the mobile phone to this page. The app will be started automaticly and the movie should start itself at once.
 * Incline the phone to right to see the volume up, and incline the phone to left to see the volume down. Turn the phone upside down to see the movie pause.
 * If you left the app to see an SMS or takes a phone call, the movie will be paused automaticly.
 

## Reference:

 * [Sensor Demo](https://bitbucket.org/kryszt/sensorsdemo/) : A demo using different kinds of sensors
 * [HMKCode](http://hmkcode.com/android-internet-connection-using-http-get-httpclient/) Android Internet Connection Using HTTP GET (HttpClient)
 * [Nielsen’s Ten Usability Heuristics](http://faculty.kutztown.edu/rieksts/385/topics/hci/nielsen-summary.html)

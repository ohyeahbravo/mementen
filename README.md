# Mementen
A desktop application for Mementen, a narrative memory player.

## What is Mementen?
<img src="https://github.com/ohyeahbravo/mementen/blob/master/mementen_after.png" width="500" />
Mementen is a audio-visual device, designed to helps users to recall their stories behind objects. It records and plays a user’s voice telling a story about an object placed on the platform. The narration is supported by visual materials related to the story being told, picked by the user. The materials may include photos, drawings, or videos. Our intention was to integrate the tangibility of a physical object with the abstractness of a story to create a tactile memory. The presented prototype does not yet have the functionality to record memories at the moment. The technique for object recognition should be improved as well as the development of an app to record and save memories. More information can be found in our <a href="https://github.com/ohyeahbravo/mementen/blob/master/documentation.pdf">project documentation</a>.

## Demo Video
<iframe src="https://player.vimeo.com/video/488990664" width="640" height="564" frameborder="0" allow="autoplay; fullscreen" allowfullscreen></iframe>

## Implementation
This app was implemented as a desktop app, using JavaFX. Additional modules used were arduino, jSerialComm, and RXTXComm. arduino and jSerialComm together comprise the Java-Arduino Communication Library to provide the Arduino class as well as serial read and write functions. RXTX library was added to enable serial and parallel communication for Java Development Toolkit(JDK). Especially, WeightSensorApp class in arduino library was used to specify the connection and communicate with the weight sensor. The followings are the flowchart of the program and the detailed explanations on each step.
<br/><br/><img src="https://github.com/ohyeahbravo/mementen/blob/master/flowchart.png" width="600" /><br/>

### Sensor Connection
When the app starts, it finds the port where arduino is connected. The serial port number is then used to start the WeightSensorApp class and open the connection. The thread waits for 1000 milliseconds to ensure the connection is done. The opened connection is maintained until the app is terminated. We initially designed it to be reconnected each time the app changes the scene, but later found it to slow down the process.

### Value Correction
The app originally reads the value from the weight sensor as a string. Since one incoming string comes with one or more line feeds, it splits the input and takes only the first one. This value is then parsed as double so that it can be calculated. Any exceptions when reading the value would result in the weight value to be zero.

### Weight Detection
Since the values from the weight sensor isn’t always consistent, they should be processed accordingly. For the both modes of the app - object on and off - the sensor is tared; it becomes zero. This is because the baseline of the weight sensor changes slightly without taring for a long time. At the same time, the first value is discarded. After that, the weight is detected differently according to the mode. When the object isn’t placed and the screen is black, it checks whether the absolute weight is more than 20g, and the difference between the current and the previous value is less than 5.
If this condition is met twice in a row, the value is passed to find the item and play the corresponding memory. When the object is already placed and the memory is playing, the condition for the weight is less than -20. This is because after taring, the value becomes negative when the object is put off. In this case, especially, when the difference of the current and previous value is more than 20, it detects that something is put, and tare again when nothing is detected next time.

### Playing Memory
As soon as the app detects and recognizes the object, it plays the audio and a slideshow of photos. The audio and slideshow are auto-replayed until the object is put off. The files were already saved locally and simply retrieved each time the object is specified. One problem we found was that the retrieval time of photos gets noticeably long, especially when they are in high resolution and of relative large quantity from around 10. For this reason, we limited the number of photos for each memory, for the presentation of the prototype.

package sample;

import arduino.Arduino;

public class WeightSensorApp {

    String port = "COM8";
    int baud_rate = 9600;

    // create connection to arduino
    private static Arduino conn = null;
    boolean isOn = false; // weight sensor on?
    String[] commands = {"turnOn", "turnOff"};  // commands for arduino

    public WeightSensorApp(String inPort, int inRate) {

        this.port = inPort;
        this.baud_rate = inRate;

        if(conn == null) {
            conn = new Arduino(port, baud_rate);
        }
    }

    public Arduino getConn() {
        return this.conn;
    }

}

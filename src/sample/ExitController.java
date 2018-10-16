package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

class ExitController {

    private Scene scene;

    private String port;
    private boolean empty = false;
    private double weight = 0;

    void getWeight(String in_port) {
        //System.out.println("ExitController");
        this.port = in_port;
        Task listenWeight = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                WeightSensorApp wApp = new WeightSensorApp(port, 9600);
                wApp.getConn().openConnection();
                // wait for connection to be done
                Thread.sleep(1000);

                int initialCount = 0;
                double previous = 0;
                int detected = 0;
                boolean sthPut = false;

                while (!empty) {

                    // read weight sensor & format
                    String input = wApp.getConn().serialRead();
                    String[] parts = input.split("\n");
                    String input2 = parts[0];
                    try {
                        weight = Double.parseDouble(input2);
                        //System.out.println(weight);
                    } catch (NumberFormatException e) {
                        weight = 0;
                        continue;
                    }

                  /* Weight Correction
                     weight sensor often gets weird values.. */

                    if(initialCount < 2) // first value is discarded
                        initialCount++;
                    else {
                        if(weight - previous > 30) {
                            sthPut = true;
                        }
                        // similar values in a row that is over 40g are detected
                        else if (weight < -30 && Math.abs(weight - previous) < 5) {
                            detected++;
                        } else if (detected > 0) { // not consistent -> start over counting
                            detected = 0;
                        } else if (sthPut) {
                            wApp.getConn().serialWrite('t');
                            sthPut = false;
                        }

                        if (detected == 2) {   // detected 3 times
                            empty = true;
                        }
                        previous = weight;
                    }
                }
                wApp.getConn().closeConnection();
                // run later in the main thread
                Platform.runLater(() -> {
                    try {
                        showStartScreen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return null;
            }
        };

        Thread th = new Thread(listenWeight);
        th.setDaemon(true);
        th.start();
    }

    private void showStartScreen() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("startLayout.fxml"));
        Parent root = fxmlLoader.load();
        StartController controller = fxmlLoader.getController();
        controller.getWeight(port);
        Thread.sleep(1500); // weight for the weight sensor to be zero
        controller.setFullScreen(scene);
        scene.setRoot(root);
    }

    void setFullScreen(Scene in_scene) {
        this.scene = in_scene;
    }
}

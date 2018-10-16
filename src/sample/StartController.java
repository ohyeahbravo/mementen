package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class StartController {

    private String port;
    private Scene scene;
    private boolean empty = true;
    private double weight = 0;

    void getWeight(String in_port) {
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
                weight = 0;

                while (empty) {

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
                        if(weight - previous < -30) {
                            wApp.getConn().serialWrite('t');
                        }
                        // similar values in a row that is over 40g are detected
                        else if (weight > 30 && (weight - previous) < 5) {
                            detected++;
                        } else if (detected > 0) { // not consistent -> start over counting
                            detected = 0;
                        }

                        if (detected == 2) {   // detected 3 times
                            empty = false;
                        }
                        previous = weight;
                    }
                }

                wApp.getConn().closeConnection();
                // run later in the main thread
                Platform.runLater(() -> {
                    try {
                        System.out.println(weight);
                        showPlayScreen(searchItem(weight));
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

    private int searchItem(double w) {
        if (w < 50) {   // ruler 32
            return 1;
        } else if (w < 80) {  // perfume 62
            return 2;
        } else  // phone 437
            return 3;
    }

    private void showPlayScreen(int n) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("playLayout.fxml"));
        Parent root = fxmlLoader.load();
        PlayController controller = fxmlLoader.getController();
        controller.getWeight(port);
        Thread.sleep(1000); // weight for the weight sensor to be zero
        controller.setFullScreen(scene);
        controller.setItem(n);
        controller.playMemory();
        scene.setRoot(root);
    }

    void setFullScreen(Scene in_scene) {
        this.scene = in_scene;
    }

}

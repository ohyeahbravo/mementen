package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;

public class PlayController {

    @FXML
    private ImageView imgView;
    private ArrayList<Image> imgs = new ArrayList<>();

    private Scene scene;
    private String port;
    private int item;
    private boolean empty = false;
    private double weight = 0;

    private Media audio;
    private MediaPlayer mediaPlayer;
    private int imgCount = 0;
    private double mediaLength = 0;
    private double slideshowLength = 5000;  // millisec
    private int slideshowCount = 0;
    private Task slideShow;
    private Thread slideThread;

    void playMemory() {

        audio = new Media(new File("src/mem/mem" + item + ".mp3").toURI().toString());
        mediaPlayer = new MediaPlayer(audio);
        getImgs();

        mediaPlayer.setOnReady(() -> {
            mediaLength = audio.getDuration().toMillis();
            slideshowCount = (int) Math.round(mediaLength / slideshowLength);
            try {
                showImgs(imgs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            try {
                showExitScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }) ;

        mediaPlayer.play();
    }

    private void getImgs() {
        String imgDir = "src/imgs/" + item;
        File dir = new File(imgDir);
        File[] files = dir.listFiles();
        if(files != null) {
            for(File file : files) {
                Image img = new Image("file:" + file.getPath());
                imgs.add(img);
            }
        } else {
            Image img = new Image("img/item0.png");
            imgs.add(img);
        }
    }

    private void showImgs(ArrayList<Image> imgs) {

        slideShow = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                for (int i = 0; i < slideshowCount; i++) {

                    Platform.runLater(() -> {
                        Image img = imgs.get(imgCount++);

                        imgView.setImage(img);
                        if(imgCount >= imgs.size()) {
                            imgCount = 0;
                        }
                    });
                    Thread.sleep((long)slideshowLength);
                }
                return null;
            }
        };
        slideThread = new Thread(slideShow);
        slideThread.setDaemon(true);
        slideThread.start();
    }

    void getWeight(String in_port) {
        this.port = in_port;
        //System.out.println("PlayController");
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
                        System.out.println(weight);
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

    private void showExitScreen() throws Exception {
        /* these lines sometimes creates NullPointerException... why tho.
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        */
        if(slideShow != null)
            slideShow.cancel();
        if(slideThread != null)
            slideThread.interrupt();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("exitLayout.fxml"));
        Parent root = fxmlLoader.load();
        ExitController controller = fxmlLoader.getController();
        controller.getWeight(port);
        Thread.sleep(1500); // weight for the weight sensor to be zero
        controller.setFullScreen(scene);
        scene.setRoot(root);
    }

    private void showStartScreen() throws Exception {

        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        if(slideShow != null)
            slideShow.cancel();
        if(slideThread != null)
            slideThread.interrupt();

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
        imgView.setPreserveRatio(true);
        imgView.fitWidthProperty().bind(scene.widthProperty());
        imgView.fitHeightProperty().bind(scene.heightProperty());
    }

    void setItem(int n) {
        this.item = n;
    }
}

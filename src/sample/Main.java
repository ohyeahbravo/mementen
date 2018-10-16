package sample;

import gnu.io.CommPortIdentifier;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Enumeration;

public class Main extends Application {

    private String port;
    private WeightSensorApp wApp;

    @Override
    public void start(Stage primaryStage) throws Exception{

        // Find Port where arduino is connected
        CommPortIdentifier serialPortId;
        Enumeration enumComm = CommPortIdentifier.getPortIdentifiers();
        while(enumComm.hasMoreElements()) {
            serialPortId = (CommPortIdentifier) enumComm.nextElement();
            if (serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)
                port = serialPortId.getName();
        }

        // Start with StartLayout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("startLayout.fxml"));
        Parent root = loader.load();
        StartController controller = loader.getController();

        // pass the weight sensor app
        wApp = new WeightSensorApp(port, 9600);
        wApp.getConn().openConnection();
        // wait for connection to be done
        Thread.sleep(1000);

        controller.getWeight(wApp);
        primaryStage.setTitle("Mementen");
        primaryStage.setFullScreen(true);
        Scene scene = new Scene(root, 640, 400);
        controller.setFullScreen(scene);
        primaryStage.setScene(scene);
        primaryStage.show();

}

    public static void main(String[] args) {
        launch(args);
    }
}

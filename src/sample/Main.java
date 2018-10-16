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
        controller.getWeight(port);
        Thread.sleep(2000); // weight for the weight sensor to be zero
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fourvideoinstallation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author simonkenny
 */
public class FourVideoInstallation extends Application {
    
    private GridPane videoGrid;
    private MediaPlayer []mediaPlayer;
    private Group root = new Group();
    
    Rectangle2D bounds;
    
    private final KeyCode []hotKeys = { 
        KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D
    };
    
    private final Point2D []gridLoc = {
        new Point2D(0,0),
        new Point2D(1,0),
        new Point2D(0,1),
        new Point2D(1,1)
    };
    
    /*
    private final String []mediaUrls = {
        "file:/Users/simonkenny/Documents/rnd/oow2010-2-0.flv",
        "file:/Users/simonkenny/Documents/rnd/oow2010-2-1.flv",
        "file:/Users/simonkenny/Documents/rnd/oow2010-2-2.flv",
        "file:/Users/simonkenny/Documents/rnd/oow2010-2-3.flv"
    };
    */
    
    private final String []mediaUrls = {
        "file:/Users/simonkenny/Downloads/trueDetective/true.detective.s01e04.hdtv.x264-2hd.mp4",
        "file:/Users/simonkenny/Downloads/trueDetective/true.detective.s01e05.hdtv.x264-killers.mp4",
        "file:/Users/simonkenny/Downloads/trueDetective/true.detective.s01e06.hdtv.x264-2hd.mp4",
        "file:/Users/simonkenny/Downloads/trueDetective/true.detective.s01e07.hdtv.x264-killers.mp4"
    };
    
    //private final String vid = "file:/Users/simonkenny/Downloads/North.m4v";
    //private final String []mediaUrls = { vid, vid, vid, vid };
    
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        
        /*
        VBox vBox = new VBox();
        vBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        vBox.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        vBox.setFillWidth(true);
        HBox hBox = new HBox();
        hBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        hBox.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        hBox.setFillHeight(true);
        vBox.getChildren().add(hBox);
        */
        videoGrid = new GridPane();
        videoGrid.setAlignment(Pos.CENTER);
        //videoGrid.setGridLinesVisible(true);
        videoGrid.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //hBox.getChildren().add(videoGrid);
        mediaPlayer = new MediaPlayer[4];
        for( int i = 0 ; i < 4 ; i++ ) {
            Media media = new Media(mediaUrls[i]);
            mediaPlayer[i] = new MediaPlayer(media);
            mediaPlayer[i].setAutoPlay(true);
            MediaView mediaView = new MediaView(mediaPlayer[i]);
            //mediaView.setPreserveRatio(false);
            mediaView.setFitWidth(600);
            FlowPane flowPane = new FlowPane(mediaView);
            GridPane.setHgrow(flowPane, Priority.ALWAYS);
            GridPane.setVgrow(flowPane, Priority.ALWAYS);
            flowPane.setAlignment(Pos.CENTER);
            //GridPane.setValignment(mediaView, VPos.CENTER);
            //GridPane.setHalignment(mediaView, HPos.CENTER);
            videoGrid.add(flowPane, (int)gridLoc[i].getX(), (int)gridLoc[i].getY());
            System.out.println("Adding media view to grid ("+(i%2)+","+((int)i/2)+")");
        }
        //final Scene scene = new Scene(vBox);
        //root.getChildren().add(videoGrid);
        
        
        // resize
        Screen screen = Screen.getPrimary();
        bounds = screen.getVisualBounds();
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        videoGrid.setPrefSize(bounds.getWidth(), bounds.getHeight());
        /*
        for( Node node : videoGrid.getChildren() ) {
            if( node instanceof FlowPane ) {
                MediaView mediaView = (MediaView)node;
                mediaView.setFitWidth(bounds.getWidth()/2);
                mediaView.setFitHeight(bounds.getHeight()/2);
            }
        }
        */
        //final Scene scene = new Scene(root);
        final Scene scene = new Scene(videoGrid);
        
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
        
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if( t.getCode() == KeyCode.ESCAPE )
                {
                    for( MediaPlayer player : mediaPlayer ) {
                        player.stop();
                    }
                    stage.close();
                } else if( t.getCode() == KeyCode.SPACE ) {
                    printMediaPlayerInfo();
                } else if( t.getCode() == KeyCode.Z ) {
                    Media media = new Media(mediaUrls[0]);
                    mediaPlayer[0] = new MediaPlayer(media);
                    mediaPlayer[0].setAutoPlay(true);
                    MediaView mediaView = new MediaView(mediaPlayer[0]);
                    //mediaView.setPreserveRatio(false);
                    mediaView.setFitWidth(1200);
                    FlowPane fullScreenPane = new FlowPane(mediaView);
                    fullScreenPane.setAlignment(Pos.CENTER);
                    fullScreenPane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    root.getChildren().add(fullScreenPane);
                    //scene.getChildren().add(fullScreenPane);
                } else {
                    int count = 0;
                    for( MediaPlayer player : mediaPlayer ) {
                        if( t.getCode() == hotKeys[count++] ) {
                            player.play();
                        } else {
                            player.stop();
                        }
                    }
                }
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private void printMediaPlayerInfo() {
        int count = 0;
            for( MediaPlayer player : mediaPlayer ) {
                MediaPlayer.Status status = player.getStatus();
                String statusStr = "Undefined";
                if( status == MediaPlayer.Status.DISPOSED ) {
                    statusStr = "Disposed";
                } else if( status == MediaPlayer.Status.HALTED ) {
                    statusStr = "Halted";
                } else if( status == MediaPlayer.Status.PAUSED ) {
                    statusStr = "Paused";
                } else if( status == MediaPlayer.Status.PLAYING ) {
                    statusStr = "Playing";
                } else if( status == MediaPlayer.Status.READY ) {
                    statusStr = "Ready";
                } else if( status == MediaPlayer.Status.STALLED ) {
                    statusStr = "Stalled";
                } else if( status == MediaPlayer.Status.STOPPED ) {
                    statusStr = "Stopped";
                } else if( status == MediaPlayer.Status.UNKNOWN ) {
                    statusStr = "Unknown";
                }
                System.out.println("MediaPlayer "+(count++)+" : "+statusStr);
            }
    }
}

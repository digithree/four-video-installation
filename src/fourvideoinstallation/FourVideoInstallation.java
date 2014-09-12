/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fourvideoinstallation;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author simonkenny
 */
public class FourVideoInstallation extends Application {
    
    private final String PREF_PATH = "FVI_PATH";
    
    private Pane fourVideoPane;
    private MediaView []thumbMediaViews = new MediaView[4];
    private MediaView mainMediaView = null;
    private Group root = new Group();
    
    Rectangle2D screenBounds;
    
    private final KeyCode []hotKeys = { 
        KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D
    };
    
    private float ANIMATION_DURATION = 500.f;
    
    private int videoSelected = -1;
    
    private final long BUTTON_FREEZE_WAIT_EXPAND = 2000; //millis
    private final long BUTTON_FREEZE_WAIT_BACK = 2000; //millis
    private boolean animationFinished = true;
    
    String path = null;
    
    private final String []mediaUrls = {
        "1.mp4",
        "2.mp4",
        "3.mp4",
        "4.mp4"
    };
    
    private final String []mediaThumbUrls = {
        "1-thumb.mp4",
        "2-thumb.mp4",
        "3-thumb.mp4",
        "4.mp4"
    };
    
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        Preferences prefs = Preferences.userNodeForPackage(FourVideoInstallation.class);
        //prefs.remove(PREF_PATH);
        path = prefs.get(PREF_PATH, null);
        if( path == null ) {
            // do first time stuff
            Optional<String> response = Dialogs.create()
                    .owner(stage)
                    .title("First time setup")
                    .masthead("Please locate the path where the videos are stored and enter")
                    .message("Path:")
                    .showTextInput();
            // One way to get the response value.
            if (response.isPresent()) {
                path = response.get();
            }
            if( path == null ) {
                // error
                Dialogs.create()
                        .owner(null)
                        .title("Error")
                        .masthead("Invalid path")
                        .message("Couldn't add path, please restart and re-enter")
                        .showError();
                Platform.exit();
                return;
            }
            prefs.put(PREF_PATH, path);
        }
        
        this.stage = stage;
        
        fourVideoPane = new Pane();
        fourVideoPane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fourVideoPane.setCursor(Cursor.NONE);
        
        // resize
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        screenBounds = new Rectangle2D(0,0,dim.getWidth(),dim.getHeight());
        //Screen screen = Screen.getPrimary();
        //screenBounds = screen.getVisualBounds();
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        
        // create media, media player and media view for thumbs
        for( int i = 0 ; i < 4 ; i++ ) {
            /*
            Media media = new Media(FourVideoInstallation.class
                    .getResource(mediaUrls[i]).toExternalForm());
            */
            Media media = null;
            try {
                media = new Media(new File(path+mediaThumbUrls[i]).toURI().toURL().toExternalForm());
            } catch (MalformedURLException ex) {
                Logger.getLogger(FourVideoInstallation.class.getName()).log(Level.SEVERE, null, ex);
            }
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setMute(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setOnError(new Runnable() {    
                @Override
                public void run() {
                    System.out.println(mediaPlayer.errorProperty().get().getMessage());
                }
            });
            thumbMediaViews[i] = new MediaView(mediaPlayer);
            //mediaView.setPreserveRatio(false);
            thumbMediaViews[i].setFitWidth(screenBounds.getWidth()/2);
            thumbMediaViews[i].relocate((screenBounds.getWidth()/2)*((int)i%2), 
                    (screenBounds.getHeight()/2)*((int)i/2));
            fourVideoPane.getChildren().add(thumbMediaViews[i]);
        }
        fourVideoPane.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());

        final Scene scene = new Scene(fourVideoPane);
        
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
        
        /*
        Platform.runLater(new Runnable() {
            @Override public void run() {
                // debug width output
                System.out.println("Screen dimensions: "+screenBounds.getWidth()+" x "+screenBounds.getHeight());
                System.out.println("Screen should be:");
                System.out.println("\t"+(screenBounds.getWidth()-(VIDEO_WIDTH_SMALL*2))
                        +"\t"+VIDEO_WIDTH_SMALL
                );
                System.out.println("Screen is:");
                System.out.println("\t"+(screenBounds.getWidth()-(VIDEO_WIDTH_SMALL*2))
                        +"\t"+thumbMediaViews[0].getBoundsInLocal().getWidth()
                );
                System.out.println("Coordinates of MediaViews:");
                int count = 0;
                for( MediaView mv : thumbMediaViews ) {
                    System.out.println("\t"+mv.getBoundsInParent().getMinX()
                            +" x "+mv.getBoundsInParent().getMinY());
                    smallBounds[count++] = mv.getBoundsInParent();
                }
                // set large size and scale factor from small to large
                VIDEO_WIDTH_LARGE = (int)(smallBounds[3].getMaxX() - smallBounds[0].getMinX());
                VIDEO_LARGE_SCALE_FACTOR = VIDEO_WIDTH_LARGE / VIDEO_WIDTH_SMALL;
                // calculate the extra translation need to make full screen videos centered
                //extraTranslateX = (smallBounds[0].getMaxX()-smallBounds[0].getMinX()) / 2;
                //extraTranslateY = (smallBounds[0].getMaxY()-smallBounds[0].getMinY());
                extraTranslateX = thumbMediaViews[0].getBoundsInLocal().getWidth()/2;
                extraTranslateY = videoHeightRatio * extraTranslateX;
                System.out.println("extraTranslate: "+extraTranslateX+", "+extraTranslateY);
                //extraTranslateY = 1000;
            }
        });
        */
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                int videoSelect = -1;
                if( t.getCode() == KeyCode.ESCAPE )
                {
                    for( MediaView mv : thumbMediaViews ) {
                        if( mv.getMediaPlayer() != null ) {
                            mv.getMediaPlayer().stop();
                            mv.getMediaPlayer().dispose();
                        }
                    }
                    stage.close();
                } else if( t.getCode() == KeyCode.SPACE ) {
                    printMediaPlayerInfo();
                } else if( t.getCode() == KeyCode.W ) {
                    videoSelect = 0;
                } else if( t.getCode() == KeyCode.E && videoSelected == -1 ) {
                    videoSelect = 1;
                } else if( t.getCode() == KeyCode.S && videoSelected == -1 ) {
                    videoSelect = 2;
                } //else if( t.getCode() == KeyCode.D && videoSelected == -1 ) {
                  //  videoSelect = 3;
                //}
                // TODO : enable fourth video when given to us
                // apply transition
                if( videoSelect >= 0 && animationFinished ) {
                    /*
                    Task<Void> task = new Task<Void>() {
                        @Override protected Void call() throws Exception {
                            animationFinished = false;
                            try {
                                Thread.sleep(BUTTON_FREEZE_WAIT_EXPAND);
                            } catch (InterruptedException interrupted) {
                                if (isCancelled()) {
                                    System.out.println("Sleep thread cancelled!");
                                    return null;
                                }
                            }
                            animationFinished = true;
                            return null;
                        }
                    };
                    */
                    //new Thread(task).start();
                    if( videoSelected != -1 ) {
                        backToFourVideos();
                    } else {
                        expandAndPlayVideo(videoSelect);
                    }
                }
            }
        });
        
        try {
            // finally, move mouse
            (new Robot()).mouseMove(0, (int) (screenBounds.getHeight()-1));
        } catch (AWTException ex) {
            Logger.getLogger(FourVideoInstallation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private void backToFourVideos() {
        // setup thumb video
        //if( thumbMediaViews[videoSelected].getMediaPlayer() != null ) {
        //    thumbMediaViews[videoSelected].getMediaPlayer().stop();
        //}
        fourVideoPane.getChildren().remove(mainMediaView);
        mainMediaView.getMediaPlayer().dispose();
        mainMediaView = null;
        for( MediaView mv : thumbMediaViews ) {
            mv.getMediaPlayer().play();
            mv.setBlendMode(BlendMode.SRC_OVER);
        }
        // back to seeing all video thumbnail clips
        moveVideos(true).play();
        videoSelected = -1;
        
    }
    
    private void expandAndPlayVideo(int vidNum) {
        // show video
        //mediaView[videoSelect].toFront();
        thumbMediaViews[vidNum].setBlendMode(BlendMode.BLUE);
        
        videoSelected = vidNum;
        Media media = null;
        try {
            media = new Media(new File(path+mediaUrls[videoSelected]).toURI().toURL().toExternalForm());
        } catch (MalformedURLException ex) {
            Logger.getLogger(FourVideoInstallation.class.getName()).log(Level.SEVERE, null, ex);
        }
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(0);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                System.out.println("video finished");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("back to four videos");
                        backToFourVideos();
                    }
                });
            }
        });
        //mediaPlayer.setAutoPlay(true);
        mainMediaView = new MediaView(mediaPlayer);
        mainMediaView.setFitWidth(screenBounds.getWidth());
        mainMediaView.relocate(0, 0);
        //fourVideoPane.getChildren().add(mainMediaView);
        Transition transition = moveVideos(false);
        transition.setOnFinished(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent arg0) {
                System.out.println("transition finished");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //pause videos
                        for( MediaView mv : thumbMediaViews ) {
                            mv.getMediaPlayer().pause();
                        }
                        //start selected video
                        fourVideoPane.getChildren().add(mainMediaView);
                        mediaPlayer.play();
                    }
                });
            }
        });
        transition.play();
    }
    
    private Transition moveVideos(boolean offScreen) {
        SequentialTransition seqTrans = new SequentialTransition();
        if( !offScreen ) {
            seqTrans.getChildren().add(
                    new PauseTransition(Duration.millis(1000))
            );
        }
        TranslateTransition []move = new TranslateTransition[4];
        for( int i = 0 ; i < 4 ; i++ ) {
            move[i] = new TranslateTransition(Duration.millis(ANIMATION_DURATION),
                        thumbMediaViews[i]);
            move[i].setByX((offScreen?-1:1)*screenBounds.getWidth());
        }
        //System.out.println("Moving videos off screen");
        // do it
        ParallelTransition moveTransitions = new ParallelTransition();
        moveTransitions.getChildren().addAll(
                move[0],
                move[1],
                move[2],
                move[3]
        );
        moveTransitions.setCycleCount(1);
        seqTrans.getChildren().add(moveTransitions);
        return seqTrans;
    }
    
    private void printMediaPlayerInfo() {
        int count = 0;
            for( MediaView mv : thumbMediaViews ) {
                MediaPlayer.Status status = mv.getMediaPlayer().getStatus();
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

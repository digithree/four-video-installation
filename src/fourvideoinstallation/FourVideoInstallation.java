/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fourvideoinstallation;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
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
    
    private FlowPane fourVideoPane;
    private MediaView []mediaView = new MediaView[4];
    private Group root = new Group();
    
    Rectangle2D screenBounds;
    
    private final KeyCode []hotKeys = { 
        KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D
    };
    
    private final Duration []videoPreviewMarkers = {
        Duration.seconds(150), Duration.seconds(161),
        Duration.seconds(100), Duration.seconds(113),
        Duration.seconds(200), Duration.seconds(217),
        Duration.seconds(10), Duration.seconds(33)
    };
    
    private int VIDEO_WIDTH_SMALL = 600;
    private int VIDEO_WIDTH_LARGE = 600;
    private float ANIMATION_DURATION_LONG = 1000.f;
    private float ANIMATION_DURATION_SHORT = 500.f;
    private double VIDEO_LARGE_SCALE_FACTOR = 2;
    
    // videoHeightRatio calculated by dividing height by width
    private double videoHeightRatio = 0.5625;
    
    private int videoSelected = -1;
    
    private ParallelTransition scaleTransitions;
    
    private Bounds []smallBounds = new Bounds[4];
    private double extraTranslateX = 0;
    private double extraTranslateY = 0;
    
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
        
        fourVideoPane = new FlowPane();
        fourVideoPane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fourVideoPane.setAlignment(Pos.CENTER);
        fourVideoPane.setCursor(Cursor.NONE);
        
        // resize
        Screen screen = Screen.getPrimary();
        screenBounds = screen.getVisualBounds();
        stage.setX(0);
        stage.setY(0);
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        VIDEO_WIDTH_SMALL = (int)(screenBounds.getWidth()/2.f);
        VIDEO_WIDTH_LARGE = VIDEO_WIDTH_SMALL;
        
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
            //mediaPlayer[i].setStartTime(videoPreviewMarkers[(i*2)]);
            //mediaPlayer[i].setStopTime(videoPreviewMarkers[(i*2)+1]);
            mediaPlayer.setMute(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setOnError(new Runnable() {    
                @Override
                public void run() {
                    System.out.println(mediaPlayer.errorProperty().get().getMessage());
                }
            });
            mediaView[i] = new MediaView(mediaPlayer);
            //mediaView.setPreserveRatio(false);
            mediaView[i].setFitWidth(VIDEO_WIDTH_SMALL);
            
            fourVideoPane.getChildren().add(mediaView[i]);
        }
        fourVideoPane.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());

        final Scene scene = new Scene(fourVideoPane);
        
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
        
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
                        +"\t"+mediaView[0].getBoundsInLocal().getWidth()
                );
                System.out.println("Coordinates of MediaViews:");
                int count = 0;
                for( MediaView mv : mediaView ) {
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
                extraTranslateX = mediaView[0].getBoundsInLocal().getWidth()/2;
                extraTranslateY = videoHeightRatio * extraTranslateX;
                System.out.println("extraTranslate: "+extraTranslateX+", "+extraTranslateY);
                //extraTranslateY = 1000;
            }
        });
        
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                int videoSelect = -1;
                if( t.getCode() == KeyCode.ESCAPE )
                {
                    for( MediaView mv : mediaView ) {
                        mv.getMediaPlayer().stop();
                        mv.getMediaPlayer().dispose();
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
                            //System.out.println("intertrial wait over, moving to next trial");
                            //Platform.runLater(new Runnable() {
                            //    @Override public void run() {
                                    animationFinished = true;
                                    if( videoSelected != -1 ) {
                                        System.out.println("Setting video "+videoSelected
                                                +" for fullscreen playback");
                                        mediaView[videoSelected].getMediaPlayer().stop();
                                        //mediaView[videoSelected].setMediaPlayer(null);
                                        // set new media for player
                                        Media media = null;
                                        try {
                                            media = new Media(new File(path+mediaUrls[videoSelected]).toURI().toURL().toExternalForm());
                                        } catch (MalformedURLException ex) {
                                            Logger.getLogger(FourVideoInstallation.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        MediaPlayer mediaPlayer = new MediaPlayer(media);
                                        mediaView[videoSelected].setMediaPlayer(mediaPlayer);
                                        // setup up start
                                        //mediaPlayer[videoSelected].setStartTime(Duration.seconds(0));
                                        //mediaPlayer[videoSelected].setStopTime(
                                        //        videoEndMarkers[videoSelected]);
                                        //mediaPlayer[videoSelected].setStopTime(Duration.seconds(20));
                                        //mediaPlayer[videoSelected].seek(Duration.seconds(0));
                                        mediaPlayer.setOnEndOfMedia(new Runnable() {
                                            @Override public void run() {
                                                System.out.println("end of playback");
                                                animationFinished = false;
                                                Platform.runLater(new Runnable() {
                                                    @Override public void run() {
                                                        Media media = null;
                                                        try {
                                                            media = new Media(new File(path+mediaThumbUrls[videoSelected]).toURI().toURL().toExternalForm());
                                                        } catch (MalformedURLException ex) {
                                                            Logger.getLogger(FourVideoInstallation.class.getName()).log(Level.SEVERE, null, ex);
                                                        }
                                                        MediaPlayer mediaPlayer = new MediaPlayer(media);
                                                        mediaPlayer.setMute(true);
                                                        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                                                        mediaPlayer.setAutoPlay(true);
                                                        mediaView[videoSelected].setMediaPlayer(mediaPlayer);
                                                        backToFourVideos();
                                                        try {
                                                            Thread.sleep(BUTTON_FREEZE_WAIT_BACK);
                                                        } catch (InterruptedException interrupted) {
                                                            if (isCancelled()) {
                                                                System.out.println("Sleep thread cancelled!");
                                                            }
                                                        }
                                                        animationFinished = true;
                                                    }
                                                });
                                            }
                                        });
                                        //mediaPlayer[videoSelected].setMute(false);
                                        mediaPlayer.setCycleCount(0);
                                        mediaPlayer.play();
                                    }
                            //    }
                            //});
                            return null;
                        }
                    };
                    new Thread(task).start();
                    if( videoSelected != -1 ) {
                        backToFourVideos();
                    } else {
                        expandAndPlayVideo(videoSelect);
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
    
    private void backToFourVideos() {
        // back to seeing all video thumbnail clips
        ScaleTransition shrink = null;
        ScaleTransition []grow = new ScaleTransition[3];
        TranslateTransition move = 
                new TranslateTransition(Duration.millis(ANIMATION_DURATION_SHORT),
                        mediaView[videoSelected]);
        double byX = extraTranslateX*(videoSelected%2==1?1:-1);
        double byY = extraTranslateY*(videoSelected>1?1:-1);
        move.setByX(byX);
        move.setByY(byY);
        System.out.println("Translating "+videoSelected+" by "+byX+" ,"+byY);
        int count = 0;
        int growCount = 0;
        for( MediaView mv : mediaView ) {
            if( (count++) == videoSelected ) {
                shrink = new ScaleTransition(Duration.millis(ANIMATION_DURATION_SHORT),
                        mv);
                shrink.setToX(1.f);
                shrink.setToY(1.f);
            } else {
                grow[growCount] = new ScaleTransition(Duration
                        .millis(ANIMATION_DURATION_LONG), mv);
                grow[growCount].setToX(1.f);
                grow[growCount].setToY(1.f);
                grow[growCount].setDelay(Duration.millis(ANIMATION_DURATION_SHORT));
                growCount++;
            }
        }
        scaleTransitions = new ParallelTransition();
        scaleTransitions.getChildren().addAll(
                grow[0],
                grow[1],
                grow[2],
                shrink,
                move
        );
        scaleTransitions.setCycleCount(1);
        scaleTransitions.play();
        videoSelected = -1;
    }
    
    private void expandAndPlayVideo(int vidNum) {
        // show video
        //mediaView[videoSelect].toFront();
        ScaleTransition []shrink = new ScaleTransition[3];
        ScaleTransition grow = null;
        TranslateTransition move = 
                new TranslateTransition(Duration.millis(ANIMATION_DURATION_LONG),
                        mediaView[vidNum]);
        move.setDelay(Duration.millis(ANIMATION_DURATION_SHORT));
        double byX = extraTranslateX*(vidNum%2==1?-1:1);
        double byY = extraTranslateY*(vidNum>1?-1:1);
        move.setByX(byX);
        move.setByY(byY);
        System.out.println("Translating "+vidNum+" by "+byX+" ,"+byY);
        int skrinkCount = 0;
        for( int i = 0 ; i < 4 ; i++ ) {
            if( i == vidNum ) {
                grow = new ScaleTransition(Duration.millis(ANIMATION_DURATION_LONG),
                        mediaView[i]);
                grow.setToX(VIDEO_LARGE_SCALE_FACTOR);
                grow.setToY(VIDEO_LARGE_SCALE_FACTOR);
                grow.setDelay(Duration.millis(ANIMATION_DURATION_SHORT));
                System.out.println("growing "+i);
            } else {
                shrink[skrinkCount] = new ScaleTransition(Duration
                        .millis(ANIMATION_DURATION_SHORT),mediaView[i]);
                shrink[skrinkCount].setToX(0.f);
                shrink[skrinkCount].setToY(0.f);
                skrinkCount++;
            }
        }
        scaleTransitions = new ParallelTransition();
        scaleTransitions.getChildren().addAll(
                shrink[0],
                shrink[1],
                shrink[2],
                grow,
                move
        );
        scaleTransitions.setCycleCount(1);
        scaleTransitions.play();
        videoSelected = vidNum;
    }
    
    private void printMediaPlayerInfo() {
        int count = 0;
            for( MediaView mv : mediaView ) {
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

package uk.ac.soton.comp1206.media;

import java.util.Objects;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.SettingsScene;

/**
 * The Multimedia class handles playing music and sound.
 */
public class Multimedia {
  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * The music to be played in the background
   */
  public static MediaPlayer backgroundPlayer;

  /**
   * Used to play the sounds
   */
  public static MediaPlayer soundPlayer;

  /**
   * Plays background music from the filename.
   *
   * @param musicFileName name of the music file
   */
  public static void playBackgroundMusic(String musicFileName) {
    try {
      String musicPath = Objects.requireNonNull(
          Multimedia.class.getResource("/music/" + musicFileName)).toExternalForm();
      Media musicMedia = new Media(musicPath);
      backgroundPlayer = new MediaPlayer(musicMedia);

      backgroundPlayer.setAutoPlay(true);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);

      Duration musicDuration = musicMedia.getDuration();
      backgroundPlayer.setStartTime(Duration.ZERO);
      backgroundPlayer.setStopTime(musicDuration);

      backgroundPlayer.setVolume(SettingsScene.musicVolume/100);
      backgroundPlayer.play();
      logger.info("Background music started: {}", musicFileName);
    } catch (Exception e) {
      logger.error("Error playing background music: {}", musicFileName, e);
    }
  }

  /**
   * Plays a sound from the filename.
   *
   * @param soundFileName the name of file
   */
  public static void playSound(String soundFileName) {
    try {
      String soundPath = Objects.requireNonNull(
          Multimedia.class.getResource("/sounds/" + soundFileName)).toExternalForm();
      Media soundMedia = new Media(soundPath);
      soundPlayer = new MediaPlayer(soundMedia);
      soundPlayer.setVolume(SettingsScene.effectVolume/100);
      soundPlayer.play();
      logger.info("Sound effect played: {}", soundFileName);
    } catch (Exception e) {
      logger.error("Error playing sound effect: {}", soundFileName, e);
    }
  }

  /**
   * Stops the background music.
   */
  public static void stopBackground() {
    if (backgroundPlayer != null) {
      backgroundPlayer.stop();
      logger.info("Background music stopped");
    }
  }
}

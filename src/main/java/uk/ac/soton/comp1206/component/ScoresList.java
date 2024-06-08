package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

/**
 * Stores the list of local and online scores
 */
public class ScoresList extends VBox {

  /**
   * The scores of the players
   */
  public final ListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>();

  /**
   * If it is local or online
   */
  private final StringProperty title = new SimpleStringProperty();

  /**
   * Creates a new scores list
   */
  public ScoresList(){
    getStyleClass().add("scoreList");
    setAlignment(Pos.CENTER);
    setSpacing(3);

    Label titleLabel = new Label();
    titleLabel.textProperty().bind(title);

    //Update score list when score array list is updated
    scores.addListener((ListChangeListener<? super Pair<String, Integer>>) (c) -> updateScoresList());
  }

  /**
   * Presents the top 10 scores
   */
  public void updateScoresList(){
    getChildren().clear();
    //Add title label
    Label titleLabel = new Label(getTitle());
    titleLabel.getStyleClass().add("heading");
    getChildren().add(titleLabel);

    //loop through top 10 scores
    int count = 0;
    for (Pair<String,Integer> score : scores){
      count ++;
      if (count > 10) break;

      Text localScores = new Text(score.getKey() + ":" + score.getValue().toString());
      localScores.getStyleClass().add("score");
      this.getChildren().add(localScores);
    }
  }

  /**
   * Sets the title to either local or online
   * @param text the title to be set
   */
  public void setTitle(String text){
    title.set(text);
  }

  /**
   * Returns the title
   * @return online or local
   */
  public String getTitle(){
    return title.get();
  }

  /**
   * Returns the score list
   * @return the list with the scores
   */
  public ListProperty<Pair<String,Integer>> scoreProperty(){
    return scores;
  }



}

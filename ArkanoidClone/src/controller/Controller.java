package controller;

import game.Properties;
import ui.GameWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class Controller implements Properties {
  private Settings settings;
  private int points = 0;
  private int lives = 3;

  private GameWindow gameWindow;

  public Controller() {
    settings = new Settings();
    gameWindow = new GameWindow(Controller.this, getLanguageString("windowTitle"), settings.isUsingLookAndFeel());
  }

  public int getPoints() {
    return points;
  }

  public int getLives() {
    return lives;
  }

  public void looseLife() {
    lives--;

    gameWindow.setLifeLabel();
  }

  public void resetScore() {
    points = 0;
    lives = 3;

    gameWindow.setScoreLabel();
    gameWindow.setLifeLabel();
  }

  public void addScore(int id) {
    if (id == SQUARE_ID) {
      this.points += SQUARE_POINTS;
      gameWindow.setScoreLabel();
    }
  }

  public void startMatch(ActionEvent e) {
    if (e != null) {
      if (e.getActionCommand().equals(getLanguageString("startMatch")) || e.getActionCommand().equals(getLanguageString("restartMatch"))) {
        resetScore();
        gameWindow.requestMatchAction(e.getActionCommand());
      }
    }
  }

  public String getLanguageString(String key) {
    return settings.getResourceBundle().getString(key);
  }

  public void showMessageDialog(String type) {
    if (type.equals(getLanguageString("matchOver"))) {
      JOptionPane.showMessageDialog(gameWindow, getLanguageString("matchOverText"), type, JOptionPane.INFORMATION_MESSAGE);
    }
  }
}

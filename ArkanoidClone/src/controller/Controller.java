package controller;

import ui.GameWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class Controller {
  private Settings settings;

  private GameWindow gameWindow;

  public Controller() {
    settings = new Settings();
    gameWindow = new GameWindow(Controller.this, getLanguageString("windowTitle"), settings.isUsingLookAndFeel());
  }

  public void startMatch(ActionEvent e) {
    if (e != null) {
      if (e.getActionCommand().equals(getLanguageString("startMatch")) || e.getActionCommand().equals(getLanguageString("restartMatch"))) {
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

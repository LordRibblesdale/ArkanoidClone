package controller;

import ui.GameWindow;

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
      if (e.getActionCommand().equals(getLanguageString("startMatch"))) {
        gameWindow.requestMatchAction(e.getActionCommand());
      }
    }
  }

  public String getLanguageString(String key) {
    return settings.getResourceBundle().getString(key);
  }
}

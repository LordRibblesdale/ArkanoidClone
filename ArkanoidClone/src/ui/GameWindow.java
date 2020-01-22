package ui;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameWindow extends JFrame {
  private Controller controller;

  private GamePanel gamePanel;

  private JMenuBar menu;
  private JMenu match;
  private JMenu about;

  private JPanel buttonPanel;
  private JButton startMatch;

  public GameWindow(Controller controller, String title, boolean useLookAndFeel) {
    super(title);
    this.controller = controller;

    if(useLookAndFeel) {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            GameWindow.this,
            e.getMessage(),
            "LookAndFeel UserController00" + e.getStackTrace()[0].getLineNumber(),
            JOptionPane.ERROR_MESSAGE);
      }
    }

    menu = new JMenuBar();
    match = new JMenu(controller.getLanguageString("matchMenu"));
    about = new JMenu(controller.getLanguageString("aboutMenu"));
    menu.add(match);
    menu.add(about);
    setJMenuBar(menu);

    gamePanel = new GamePanel(controller);
    add(gamePanel);

    buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    startMatch = new JButton(controller.getLanguageString("startMatch"));
    startMatch.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        //TODO add restart match

        controller.startMatch(e);
      }
    });

    buttonPanel.add(startMatch);
    add(buttonPanel, BorderLayout.PAGE_END);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(600, 800));
    //TODO set percentage window proportion
    setLocationRelativeTo(null);
    setResizable(false);
    setVisible(true);
  }

  public void requestMatchAction(String command) {
    gamePanel.startGame(command);
  }
}

package ui;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel {
  private Controller controller;

  private ArrayList<ArrayList<Rectangle2D>> blocks;
  private ArrayList<Color> blocksColor;
  private Rectangle2D player;
  private Ellipse2D ball;
  private double[] direction = null;
  private double intensity = 0;
  private int playerPosition;

  private final int PLAYER_WIDTH = 80;
  private final int PLAYER_HEIGHT = 20;
  private final int PLAYER_Y_POSITION = 600;
  private final int BALL_WIDTH = 10;
  private final int BALL_HEIGHT = 10;
  private final int BLOCK_HEIGHT = PLAYER_HEIGHT*2;
  private final int BLOCKS_COLUMN_NUMBER = 11;  //TODO fix even blocks
  private final int BLOCKS_ROW_NUMBER = 500 / BLOCK_HEIGHT;
  private final int EPSILON = 10;
  private final double DEFAULT_INTENSITY = 0.5;

  private Timer timer = null;
  private int refreshRate;

  private boolean isGameStarted = false;
  private boolean isBallMoving = false;
  private boolean hasBeenClicked = false;

  GamePanel(Controller controller) {
    super(null);
    this.controller = controller;

    refreshRate = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode().getRefreshRate();

    timer = new Timer(1/refreshRate, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (isBallMoving) {
          ball.setFrame(
                  ball.getX() + intensity*direction[0], ball.getY() + intensity*direction[1],
                  ball.getWidth(), ball.getHeight()
          );

          if (ball.getMinX() <= 0 || ball.getMaxX() >= GamePanel.this.getWidth()) {
            direction[0] = -direction[0];
          }

          if (ball.getMinY() <= 0) {
            direction[1] = -direction[1];
          }

          //TODO fix even intersection
          for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).size() != 0) {
              for (int j = 0; j < blocks.get(i).size(); j++) {
                if (blocks.get(i).get(j) != null) {
                  Rectangle2D block = blocks.get(i).get(j);

                  if (ball.getBounds2D().intersects(block)) {
                    Rectangle2D intersection = ball.getBounds2D().createIntersection(block);

                    if (Math.abs(block.getMaxX() - intersection.getCenterX()) < EPSILON ||
                            Math.abs(block.getMinX() - intersection.getCenterX()) < EPSILON) {
                      direction[0] = -direction[0];
                    } else if (Math.abs(block.getMinY() - intersection.getCenterY()) < EPSILON ||
                            Math.abs(block.getMaxY() - intersection.getCenterY()) < EPSILON) {
                      direction[1] = -direction[1];
                    }

                    blocks.get(i).set(j, null);
                  }
                }
              }
            }
          }

          if (ball.getMaxY() >= player.getMinY()) {
            if (ball.getCenterX() <= player.getMaxX() && ball.getCenterX() >= player.getMinX()) {
              //TODO fix with realistic reflection
              double value = (ball.getCenterX() - player.getCenterX())/2;
              double norm = Math.sqrt(ball.getCenterX() + player.getCenterX());
              System.out.println(value + " " + norm);
              direction[1] = -direction[1];
              direction[0] = value/norm;
            } else {
              timer.stop();
              controller.showMessageDialog(controller.getLanguageString("matchOver"));

              isGameStarted = false;
              isBallMoving = false;
            }
          }
        }

        player.setFrame(playerPosition, PLAYER_Y_POSITION, PLAYER_WIDTH, PLAYER_HEIGHT);

        GamePanel.this.repaint();
      }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);

        playerPosition = e.getX() - PLAYER_WIDTH/2;
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        if (timer != null && !hasBeenClicked) {
          intensity = DEFAULT_INTENSITY;
          isBallMoving = true;
          hasBeenClicked = true;
          timer.start();
        }
      }
    });
  }

  private void generateBlocks() {
    double blockWidth = GamePanel.this.getWidth()/(double) BLOCKS_COLUMN_NUMBER;

    blocks = new ArrayList<>(BLOCKS_ROW_NUMBER);
    blocksColor = new ArrayList<>(BLOCKS_COLUMN_NUMBER * BLOCKS_ROW_NUMBER);

    for (int i = 0; i < BLOCKS_ROW_NUMBER; i++) {
      blocks.add(new ArrayList<>(BLOCKS_COLUMN_NUMBER));

      for (int j = 0; j < BLOCKS_COLUMN_NUMBER; j++) {
        Random rand = new Random();
        blocks.get(i).add(new Rectangle2D.Double(j*blockWidth, 500-((i+1)*BLOCK_HEIGHT), blockWidth, BLOCK_HEIGHT));
        blocksColor.add(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
      }
    }
  }

  private void initialiseGame(boolean isFirstTime) {
    generateBlocks();
    direction = new double[] {0, -1};

    if (isFirstTime) {
      if (player == null) {
        player = new Rectangle2D.Double(GamePanel.this.getWidth()/2 - PLAYER_WIDTH/2, PLAYER_Y_POSITION, PLAYER_WIDTH, PLAYER_HEIGHT);
      }

      if (ball == null) {
        ball = new Ellipse2D.Double(GamePanel.this.getWidth()/2 - BALL_WIDTH/2, player.getMinY() - BALL_HEIGHT, BALL_WIDTH, BALL_HEIGHT);
      }
    } else {
      player.setFrame(GamePanel.this.getWidth()/2 - PLAYER_WIDTH/2, PLAYER_Y_POSITION, PLAYER_WIDTH, PLAYER_HEIGHT);
      ball.setFrame(GamePanel.this.getWidth()/2 - BALL_WIDTH/2, player.getMinY() - BALL_HEIGHT, BALL_WIDTH, BALL_HEIGHT);
    }
  }

  void startGame(String command) {
    if (command.equals(controller.getLanguageString("startMatch"))) {
      initialiseGame(true);
    } else if (command.equals(controller.getLanguageString("restartMatch"))) {
      initialiseGame(false);
    }

    hasBeenClicked = false;
    isGameStarted = true;
    GamePanel.this.repaint();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D graphics2D = (Graphics2D) g;

    //TODO fix drawing background
    graphics2D.setBackground(Color.BLACK);

    if (isGameStarted) {
      int index = 0;  //TODO fix index with i and j
      for (int i = 0; i < BLOCKS_ROW_NUMBER; i++) {
        for (int j = 0; j < BLOCKS_COLUMN_NUMBER; j++) {
          graphics2D.setColor(blocksColor.get(index++));
          if (blocks.get(i).get(j) != null) {
            graphics2D.draw(blocks.get(i).get(j));
            graphics2D.fill(blocks.get(i).get(j));
          }
        }
      }

      graphics2D.setColor(Color.BLACK);
      graphics2D.draw(player);
      graphics2D.fill(player);
      graphics2D.setColor(Color.MAGENTA);
      graphics2D.draw(ball);
      graphics2D.fill(ball);
    }
  }
}

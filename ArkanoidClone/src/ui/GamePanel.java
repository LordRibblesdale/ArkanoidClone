package ui;

import controller.Controller;
import game.Properties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements Properties, UIProperties {
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
  private final int BLOCKS_COLUMN_NUMBER = 12;  //TODO fix even blocks
  private final int BLOCKS_ROW_NUMBER = 500 / BLOCK_HEIGHT;
  private final int EPSILON = 5;
  private final double DEFAULT_INTENSITY = 2;

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

          setBouncesToWalls();

          if (ball.getBounds2D().getMinY() < blocks.get(0).get(0).getMaxY()) {
            setBouncesToRectangles();
          }

          setBottomBounce();
        }

        player.setFrame(playerPosition, PLAYER_Y_POSITION, PLAYER_WIDTH, PLAYER_HEIGHT);

        GamePanel.this.repaint();
      }

      private void setBouncesToWalls() {
        if (ball.getMinX() <= 0 || ball.getMaxX() >= GamePanel.this.getWidth()) {
          direction[0] = -direction[0];
        }

        if (ball.getMinY() <= 0) {
          direction[1] = -direction[1];
        }
      }

      private void setBouncesToRectangles() {
        //TODO optimise bounces (only when needed) VV

        for (ArrayList<Rectangle2D> rectangle2DS : blocks) {
          boolean hasBounced = false;

          if (rectangle2DS.size() != 0) {
            for (int j = 0; j < rectangle2DS.size(); j++) {
              if (rectangle2DS.get(j) != null) {
                Rectangle2D block = rectangle2DS.get(j);

                if (ball.getBounds2D().intersects(block)) {
                  Rectangle2D intersection = ball.getBounds2D().createIntersection(block);

                  if (Math.abs(block.getMaxX() - intersection.getCenterX()) < EPSILON ||
                      Math.abs(block.getMinX() - intersection.getCenterX()) < EPSILON) {
                    direction[0] = -direction[0];
                  }

                  if (Math.abs(block.getMinY() - intersection.getCenterY()) < EPSILON ||
                      Math.abs(block.getMaxY() - intersection.getCenterY()) < EPSILON) {
                    direction[1] = -direction[1];
                  }

                  rectangle2DS.set(j, null);
                  intensity += 0.025;
                  controller.addScore(SQUARE_ID);
                  hasBounced = true;
                  break;
                }
              }
            }
          }

          if (hasBounced) {
            break;
          }
        }
      }

      private void setBottomBounce() {
        if (ball.getMaxY() >= player.getMinY()) {
          if (ball.getCenterX() <= player.getMaxX() && ball.getCenterX() >= player.getMinX()) {
            //TODO fix with realistic reflection
            double value = (ball.getCenterX() - player.getCenterX())/2;
            double norm = Math.sqrt(ball.getCenterX() + player.getCenterX());
            direction[1] = -direction[1];
            direction[0] = value/norm;
          } else {
            timer.stop();

            if (controller.getLives() > 0) {
              try {
                synchronized (GamePanel.this) {
                  GamePanel.this.wait(1000);
                }
              } catch (InterruptedException e) {
                e.printStackTrace();
              }

              hasBeenClicked = false;
              controller.looseLife();
              initialiseGame(USE_LIFE);
            } else {
              controller.showMessageDialog(controller.getLanguageString("matchOver"));
              controller.resetScore();

              isGameStarted = false;
            }

            isBallMoving = false;
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

  private void initialiseGame(int initialiseID) {
    direction = new double[] {0, -1};

    switch (initialiseID) {
      case NEW_GAME:
      case RESTART_GAME:
        generateBlocks();
        break;
    }

    //TODO fix player painting in panel
    player = new Rectangle2D.Double(GamePanel.this.getWidth()/2f - PLAYER_WIDTH/2f, PLAYER_Y_POSITION, PLAYER_WIDTH, PLAYER_HEIGHT);
    ball = new Ellipse2D.Double(GamePanel.this.getWidth()/2f - BALL_WIDTH/2f, player.getMinY() - BALL_HEIGHT, BALL_WIDTH, BALL_HEIGHT);
  }

  void startGame(String command) {
    if (command.equals(controller.getLanguageString("startMatch"))) {
      initialiseGame(NEW_GAME);
    } else if (command.equals(controller.getLanguageString("restartMatch"))) {
      initialiseGame(RESTART_GAME);
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

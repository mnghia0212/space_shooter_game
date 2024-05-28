package com.example.spaceshooter;

public class EnemyShipPlus extends EnemyShip {
    public EnemyShipPlus() {
        resetEnemyShip();
    }

    @Override
    public void resetEnemyShip() {
        if (Math.random() < 0.5) {
            enemyX = -200;
            enemySpeedX = 6 + (int) (Math.random() * 15);
        } else {
            enemyX = GameView.screenWidth + 200;
            enemySpeedX = -1 * (5 + (int) (Math.random() * 15));
        }
        enemyY = (int) (Math.random() * 250);
        if (Math.random() < 0.5) {
            enemySpeedY = 3 + (int) (Math.random() * 6);
        } else {
            enemySpeedY = -1 * (3 + (int) (Math.random() * 6));
        }
    }

    // getter
    @Override
    public int getWidth() {
        return GameView.enemyShipPlus.getWidth();
    }

    @Override
    public int getHeight() {
        return GameView.enemyShipPlus.getHeight();
    }
}

package appClientModule.theantsproblem;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Territory {
    private int tam; // Matrix size
    private boolean occupied[][];
    String description = "ReentrantLock with multiple Condition";
    private Log log;
    ReentrantLock lock;
    Condition ocupadas[][];

    public String getDesc() {
        return description;
    }

    public Territory(int tamT, Log l) {
        lock = new ReentrantLock();
        tam = tamT;
        ocupadas = new Condition[tam][tam];
        occupied = new boolean[tam][tam];
        log = l;

        // Initializing the matrix
        for (int i = 0; i < tam; i++) {
            for (int j = 0; j < tam; j++) {
                occupied[i][j] = false;
                ocupadas[i][j] = lock.newCondition();
            }
        }
    }

    public int getTam() {
        return tam;
    }

    public void putAnt(Ant h, int x, int y) {

        try{
            lock.lock();
            while (occupied[x][y]) {
                try {
                    // Write in the log: ant waiting
                    log.writeLog(LogItem.PUT, h.getid(), x, y, LogItem.WAITINS,
                        "Ant " + h.getid() + " waiting for [" + x + "," + y + "]");
                    ocupadas[x][y].await();
                } catch (InterruptedException e) {
                }
            }
            occupied[x][y] = true;
            h.setPosition(x, y);
            // Write in the log: ant inside territory
            log.writeLog(LogItem.PUT, h.getid(), x, y, LogItem.OK, "Ant " + h.getid() + " : [" + x + "," + y + "]  inside");

        } finally{

            lock.unlock();
        }
    }

    public void takeAnt(Ant h) {

        try{
            lock.lock();
            int x = h.getX();
            int y = h.getY();
            occupied[x][y] = false;
            // Write in the log: ant outside territory
            log.writeLog(LogItem.TAKE, h.getid(), x, y, LogItem.OUT, "Ant " + h.getid() + " : [" + x + "," + y + "] out");
            ocupadas[x][y].signalAll(); 
        } finally{

            lock.unlock();
        }
    }

    public void moves(Ant h, int x1, int y1, int step) {

        int x = h.getX();
        int y = h.getY();
        try{
            lock.lock();
            while (occupied[x1][y1]) {
                try {
                    // Write in the log: ant waiting
                    log.writeLog(LogItem.MOVE, h.getid(), x1, y1, LogItem.WAIT,
                        "Ant " + h.getid() + " waiting for [" + x1 + "," + y1 + "]");
                    ocupadas[x1][y1].await();
                } catch (InterruptedException e) {
                }
            }
            occupied[x][y] = false;
            occupied[x1][y1] = true;
            h.setX(x1);
            h.setY(y1);
            // Write in the log: ant moving
            log.writeLog(LogItem.MOVE, h.getid(), x1, y1, LogItem.OK,
                "Ant " + h.getid() + " : [" + x + "," + y + "] -> [" + x1 + "," + y1 + "] step:" + step);
            ocupadas[x][y].signalAll();
        }finally{

            lock.unlock();
        }
    }
}

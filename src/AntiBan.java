import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.awt.*;

public class AntiBan {

    final static private int DEFAULT_SHORT_DELAY = 300; // milliseconds
    final static private int DEFAULT_LONG_DELAY = 1200;

    private final AbstractScript s;
    private final int tickRate;
    private final int jiggleFreq;
    private final int delayShort;
    private final int delayShortMin;
    private final int delayShortMax;
    private final int delayLong;
    private final int delayLongMin;
    private final int delayLongMax;
    private long lastBanEvade;
    private long lastIdle;

    public AntiBan(AbstractScript script, int tickRate, int delayShort, int delayLong) {
        this.s = script;
        this.tickRate = tickRate;
        this.jiggleFreq = Math.max(2, 2400/tickRate);
        Logger.info("jiggle freq is " + this.jiggleFreq);
        this.delayShort = delayShort;
        this.delayShortMin = (int) (delayShort*.80);
        this.delayShortMax = (int) (delayLong*1.20);
        this.delayLong = delayLong;
        this.delayLongMin = (int) (delayLong*.80);
        this.delayLongMax = (int) (delayLong*1.20);
        this.lastBanEvade = this.lastIdle = System.currentTimeMillis();
    }

    public AntiBan(AbstractScript script, int tickRate) {
        this(script, tickRate, DEFAULT_SHORT_DELAY, DEFAULT_LONG_DELAY);
    }

    public boolean doAntiBan(){
        int event = Calculations.random(0, 11);
        switch(event){
            case 0: // do nothing
                Logger.info("antiban doing nothing");
                return true;

            case 1: // exam random entity
                Logger.info("antiban examining random entity");
                if(Calculations.random(0, 2) == 1){
                    Logger.info("random entity is an item");
                    Inventory.open();
                    idleShort();
                }

                return true;

            case 2: // check stat
                Logger.info("antiban checking relevant stat");
                return true;

            case 3: // walk somewhere
                Logger.info("antiban walking somewhere randomly");
                return true;

            case 4: // open a menu
                Logger.info("antiban opening random menu");
                return true;

            case 5: // mouse off-screen and afk
                Logger.info("antiban going afk");
                return true;

            case 6: // log out
                Logger.info("antiban logging out");
                return true;
        }

        Logger.error("antiban failed");
        return false;
    }

    public int getTick(){
        return Calculations.random(Math.max(0, tickRate-25), tickRate+25);
    }

    /**
     * Pauses main thread a random amount of time between a min and max
     *
     * @param min min wait time in ms
     * @param max max wait time in ms
     * @return void
     * @see org.dreambot.api.utilities.Sleep
     */
    public void idle(int min, int max){
        this.s.sleep(min, max);
        this.lastIdle = System.currentTimeMillis();
    }

    /**
     * Pauses main thread a short amount of time
     *
     * @return void
     * @see org.dreambot.api.utilities.Sleep
     */
    public void idleShort(){
        idle(this.delayShortMin, this.delayShortMax);
    }

    /**
     * Pauses main thread a longer amount of time
     *
     * @return void
     * @see org.dreambot.api.utilities.Sleep
     */
    public void idleLong(){
        idle(this.delayLongMin, this.delayLongMax);
    }

    /**
     * @return a random value between the min/max short delay
     */
    public int getShortDelay(){
        return Calculations.random(this.delayShortMin, this.delayShortMax);
    }

    /**
     * @return a random value between the min/max long delay
     */
    public int getLongDelay(){
        return Calculations.random(this.delayLongMin, this.delayLongMax);
    }

    /**
     * Moves the mouse a random small distance so we appear human
     * @return void
     * @see Mouse
     */
    public void mouseJiggle(){
        if(Calculations.random(0,this.jiggleFreq) == 1 && Mouse.isMouseInScreen()) {
            int half = 3;
            int x = Mouse.getX();
            int y = Mouse.getY();

            x = Calculations.random(x - half, x + half);
            y = Calculations.random(y - half, y + half);

            Logger.info("jiggled mouse: " + x + "/" + y);
            Mouse.move(new Point(x, y));
        }
    }
}

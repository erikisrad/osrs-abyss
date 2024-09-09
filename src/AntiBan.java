import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.awt.*;

public class AntiBan {

    final static private int DEFAULT_SHORT_DELAY = 300; // milliseconds
    final static private int DEFAULT_LONG_DELAY = 1200;

    private final AbstractScript s;
    private final int delayShort;
    private final int delayShortMin;
    private final int delayShortMax;
    private final int delayLong;
    private final int delayLongMin;
    private final int delayLongMax;
    private long lastBanEvade;
    private long lastIdle;

    public AntiBan(AbstractScript script, int delayShort, int delayLong) {
        this.s = script;
        this.delayShort = delayShort;
        this.delayShortMin = (int) (delayShort*.80);
        this.delayShortMax = (int) (delayLong*1.20);
        this.delayLong = delayLong;
        this.delayLongMin = (int) (delayLong*.80);
        this.delayLongMax = (int) (delayLong*1.20);
        this.lastBanEvade = this.lastIdle = System.currentTimeMillis();
    }

    public AntiBan(AbstractScript script) {
        this(script, DEFAULT_SHORT_DELAY, DEFAULT_LONG_DELAY);
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
     * Moves the mouse a random small distance so we appear human
     * @return void
     * @see Mouse
     */
    public void mouseJiggle(){
        if(Calculations.random(0,2) == 1 && Mouse.isMouseInScreen()) {
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

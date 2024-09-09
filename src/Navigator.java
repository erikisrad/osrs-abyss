import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;

public class Navigator {

    private static final int DEFAULT_RUN_STAM = 90;
    private static final int DEFAULT_RUN_INTERVAL = 2200;
    private static final int DEFAULT_WALK_INTERVAL = 3200;
    private static final int MIN_PAD = 25;
    private static final int MAX_PAD = 75;

    // the stamina value at which we should enable running
    private final int runStamSet;
    //slightly change runStamSet so we don't suspiciously toggle run at the same value every time
    private int runStamRoll;
    private long lastMove;

    public void setRandomPad() {
        this.randomPad = Calculations.random(MIN_PAD,MAX_PAD);
    }

    private int randomPad;


    public Navigator(int runStam){
        this.runStamRoll = this.runStamSet = runStam;
        this.lastMove = 0;

    }

    public Navigator() {
        this(DEFAULT_RUN_STAM);
    }

    /**
     * turns on running and rerolls our stamina threshold
     */
    public void toggleRun(){
        Walking.toggleRun();
        this.runStamRoll = Calculations.random(
                Math.max(0, this.runStamSet-5),
                Math.min(100, this.runStamSet+5));
        Logger.debug("run threshhold is now: " + this.runStamRoll);
    }

    /**
     * determines whether we should toggle running on
     * @return boolean, true if we should run
     */
    public boolean shouldRun() {
        return Walking.getRunEnergy() >= this.runStamRoll && !Walking.isRunEnabled();
    }

    /**
     * figures out a reasonable interval we should be clicking to move at
     * @return int, move interval
     */
    public int autoInterval(){
        if(Walking.isRunEnabled()){
            return DEFAULT_RUN_INTERVAL;
        }else{
            return DEFAULT_WALK_INTERVAL;
        }
    }

    /**
     * moves towards the specified area, clicking at specified intervals
     * @param area Area, where we want to go
     * @param interval int, how often we want to click (ms)
     * @return boolean, true if we are walking without issue
     */
    public boolean runIfTime(Area area, int interval){
        long currentTime = System.currentTimeMillis();

        if(((currentTime - this.lastMove) > (interval + randomPad)
                && !area.contains(Walking.getDestination()))
                || !Abyss.localPlayer.isMoving()) {

            setRandomPad();
            this.lastMove = currentTime;
            Logger.debug("clicking to move");
            if(shouldRun()) toggleRun();
            return Walking.walk(area.getRandomTile());
        }else{
            Logger.debug("too early to move");
            return true;
        }
    }

    /**
     * moves towards the specified area, clicking at reasonable intervals
     * @param area Area, where we want to go
     * @return boolean, true if we are walking without issue
     */
    public boolean runIfTime(Area area){
        return runIfTime(area, autoInterval());
    }
}

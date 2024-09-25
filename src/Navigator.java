import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Player;

public class Navigator {

    private static final int DEFAULT_RUN_STAM = 90;
    private static final int DEFAULT_RUN_INTERVAL = 2200;
    private static final int DEFAULT_WALK_INTERVAL = 3200;
    private static final int MIN_PAD = 25;
    private static final int MAX_PAD = 75;

    private long lastMove;
    private int intervalWalk;
    private int intervalRun;

    // the stamina value at which we should enable running
    private final int runStamSet;
    //slightly change runStamSet so we don't suspiciously toggle run at the same value every time
    private int runStamRoll;
    

    private void setRandomPad() {
        this.randomPad = Calculations.random(MIN_PAD,MAX_PAD);
    }

    private void setRunStamRoll(){
        this.runStamRoll = Calculations.random(
            Math.max(0, this.runStamSet-5),
            Math.min(100, this.runStamSet+5));
    }

    private int randomPad;

    public Navigator(int runStam, int intervalWalk, int intervalRun){
        this.runStamSet = this.runStamRoll = runStam;
        this.intervalWalk = intervalWalk;
        this.intervalRun = intervalRun;
        setRandomPad();
    }

    public Navigator(int runStam){
        this(DEFAULT_RUN_STAM, DEFAULT_WALK_INTERVAL, DEFAULT_RUN_INTERVAL);
    }

    public Navigator() {
        this(DEFAULT_RUN_STAM);
    }

    /**
     * turns on running and rerolls our stamina threshold
     */
    public void toggleRun(){
        Walking.toggleRun();
        setRunStamRoll();
        Logger.debug("run threshhold is now: " + this.runStamRoll);
    }

    /**
     * resets last move so player moves asap
     */
    public void resetLastMove(){
        lastMove = 0;
    }

    /**
     * determines whether we should toggle running on
     * @return boolean, true if we should run
     */
    public boolean shouldToggleRun() {
        return Walking.getRunEnergy() >= this.runStamRoll && !Walking.isRunEnabled();
    }

    /**
     * figures out a reasonable interval we should be clicking to move at
     * @return int, move interval
     */
    public int autoInterval(){
        if(Walking.isRunEnabled()){
            return this.intervalRun;
        }else{
            return this.intervalWalk;
        }
    }

    /**
     * moves towards the specified area, clicking at specified intervals
     * @param area Area, where we want to go
     * @return boolean, true if we are walking without issue
     */
    public boolean moveIfTime(Area area, Player player){
        long currentTime = System.currentTimeMillis();
        if(shouldToggleRun()) toggleRun();

        if(((currentTime - this.lastMove) > (autoInterval() + randomPad)
                && !area.contains(Walking.getDestination()))
                || !player.isMoving()) {

            Logger.debug("clicking to move");
            boolean success = Walking.walk(area.getRandomTile());
            if(success){
                Logger.debug("move successful");
                setRandomPad();
                this.lastMove = currentTime;
            }else{
                Logger.error("failed to move");
            }
            return success;
        }else{
            Logger.debug("too early to move");
            return true;
        }
    }
}

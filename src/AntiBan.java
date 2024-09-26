import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.awt.*;

public class AntiBan {

    final static private int DEFAULT_SHORT_DELAY = 300; // milliseconds
    final static private int DEFAULT_LONG_DELAY = 1200;
    final static private int DEFAULT_EVADE_FREQ = 24;

    private final Skill[] STATS_TO_CHECK = {
            Skill.RUNECRAFTING,
            Skill.DEFENCE
    };

    private final Tab[] TABS_TO_CHECK = {
            Tab.MAGIC,
            Tab.CLAN,
            Tab.ACCOUNT_MANAGEMENT,
            Tab.COMBAT,
            Tab.EMOTES,
            Tab.FRIENDS,
            Tab.MUSIC,
            Tab.OPTIONS,
            Tab.PRAYER,
            Tab.QUEST
    };

    //attributes
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
    private final int evadeFreqMinutes;
    private final int evadeFreqMsMin;
    private final int evadeFreqMsMax;
    private int evadeFreqRoll;
    private Player lp;

    public AntiBan(AbstractScript script, int tickRate, int delayShort, int delayLong, int evadeFreqMinutes, Player lp) {
        this.s = script;
        this.tickRate = tickRate;
        this.jiggleFreq = Math.max(2, 2400/tickRate);

        this.delayShort = delayShort;
        this.delayShortMin = (int) (delayShort*.80);
        this.delayShortMax = (int) (delayLong*1.20);

        this.delayLong = delayLong;
        this.delayLongMin = (int) (delayLong*.80);
        this.delayLongMax = (int) (delayLong*1.20);

        this.lastBanEvade = System.currentTimeMillis();
        this.evadeFreqMinutes = evadeFreqMinutes;
        this.evadeFreqMsMin = (int) (evadeFreqMinutes*60000*.80);
        this.evadeFreqMsMax = (int) (evadeFreqMinutes*60000*1.20);
        setEvadeFreqRoll();

        this.lp = lp;
    }

    public AntiBan(AbstractScript script, int tickRate, Player lp) {
        this(script, tickRate, DEFAULT_SHORT_DELAY, DEFAULT_LONG_DELAY, DEFAULT_EVADE_FREQ, lp);
    }

    /**
     * checks if we should do major antiban action - otherwise does minor mouse jiggle
     * @return true if antiban check didn't encounter any errors
     */
    public boolean checkAntiBan(){
        long currentTime = System.currentTimeMillis();
        if(this.lastBanEvade + this.evadeFreqRoll < currentTime) {
            Logger.info("time for antiban");
            if(doAntiBan()){
                Logger.info("antiban successful");
                setEvadeFreqRoll();
                return true;
            }else{
                Logger.error("antiban failed");
                return false;
            }

        }else{
            Logger.debug("too early to antiban (" +
                    (currentTime - this.lastBanEvade) + " / " + this.evadeFreqRoll + " ms)");
            mouseJiggle();
            return true;
        }
    }

    public boolean doAntiBan(){
        int event = Calculations.random(0, 7);
        event = 1; //test
        switch(event){
            case 0: // do nothing
                Logger.info("antiban doing nothing");
                return true;

            case 1: // exam random entity (item, npc, or prop)
                Logger.info("antiban examining random entity");
                int roll = Calculations.random(0, 3);

                if(roll == 0){
                    Logger.info("random entity is an item");
                    Inventory.open();
                    idleShort();
                    Item item = InteractableItem.getRandomItem();

                    if(item==null){
                        Logger.error("antiban examine random item failed");
                        return false;
                    }else{
                        idleShort();
                        Logger.info("attempting item examine");
                        return Inventory.drag(item, 4);
                    }

                }else if (roll == 1){
                    Logger.info("random entity is an npc");
                    NPC npc = InteractableNPC.getRandomNPC();

                    if(npc==null){
                        Logger.error("antiban examine random npc failed");
                        return false;
                    }else{
                        idleShort();
                        Logger.info("attempting npc examine");
                        return Mouse.click(npc.getClickablePoint(), true);
                    }

                }else if (roll == 2){
                    Logger.info("random entity is a prop");
                    GameObject prop = InteractableProp.getRandomProp();

                    if(prop==null){
                        Logger.error("antiban examine random prop failed");
                        return false;
                    }else{
                        idleShort();
                        Logger.info("attempting prop examine");
                        return Mouse.click(prop.getClickablePoint(), true);
                    }

                }else{
                    Logger.error("bad examine roll");
                    return false;
                }

            case 2: // check stat
                Logger.info("antiban checking relevant stat");
                Tabs.open(Tab.SKILLS);
                idleShort();
                Skill skill = STATS_TO_CHECK[Calculations.random(0, STATS_TO_CHECK.length-1)];
                Logger.info("checking " + skill);
                if(Skills.hoverSkill(skill)){
                    idleLong();
                    Inventory.open();
                    return true;
                }else{
                    Logger.error("antiban checking stat failed");
                    return false;
                }

            case 3: // walk somewhere
                Logger.info("antiban walking somewhere randomly");
                Area pArea = lp.getSurroundingArea(12);
                Tile tile = pArea.getRandomTile();
                if(Walking.walk(tile)){
                    idle(3200, 5000);
                    return true;
                }else{
                    Logger.error("antiban random walk failed");
                    return false;
                }

            case 4: // open a menu
                Logger.info("antiban opening random menu");
                Tab tab = TABS_TO_CHECK[Calculations.random(0, TABS_TO_CHECK.length-1)];
                int x = Calculations.random(560, 720);
                int y = Calculations.random(220, 450);
                Point point = new Point(x, y);
                Logger.info("opening " + tab + " and moving mouse to " + x + "/" + y);
                if(Tabs.open(tab) && Mouse.move(point)){
                    idleLong();
                    Inventory.open();
                    return true;
                }else{
                    Logger.error("antiban open random menu failed");
                    return false;
                }

            case 5: // mouse off-screen and afk
                int afk = Calculations.random(60000, 150000);
                Logger.info("antiban going afk for " + afk + "ms");
                if(Mouse.moveOutsideScreen(true)){
                    idle(afk);
                    return true;
                }else{
                    return false;
                }

            case 6: // log out
                Logger.info("antiban logging out");
                //TODO: implement this
                Logger.info("todo...");
                return false;
        }

        Logger.error("antiban failed");
        return false;
    }

    public int getTick(){
        return Calculations.random(Math.max(0, tickRate-25), tickRate+25);
    }

    /**
     * Pauses main thread a random amount of time between a min and max
     * @param min min wait time in ms
     * @param max max wait time in ms
     * @return void
     * @see org.dreambot.api.utilities.Sleep
     */
    public void idle(int min, int max){
        this.s.sleep(min, max);
    }

    /**
     * Pauses main thread a amount of time
     * @param time how long in ms
     * @return void
     * @see org.dreambot.api.utilities.Sleep
     */
    public void idle(int time){
        this.s.sleep(time);
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

    private void setEvadeFreqRoll(){
        this.evadeFreqRoll = Calculations.random(this.evadeFreqMsMin, this.evadeFreqMsMax);
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

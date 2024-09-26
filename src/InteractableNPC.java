import javafx.util.Pair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.List;
import java.util.Objects;

public class InteractableNPC {

    static private Pair<String, Long> lastInteract = new Pair<>("name", 0L);

    final static int DEFAULT_WAIT = 4200;
    final static boolean DEFAULT_MOVING = true;
    final static boolean DEFAULT_ANIMATING = true;
    final static int MAX_DISTANCE = 10;

    static int waitRoll = DEFAULT_WAIT;

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }

    final private String name;
    final private int ID;
    private int wait;
    final private double waitMin;
    final private double waitMax;
    final private boolean moving;
    final private boolean animating;
    NPC npc;

    /**
     * specifies a world npc and how the player should interact with it
     *
     * @param name      name of npc
     * @param ID        unique npc id
     * @param wait      how long we should wait before clicking the same prop twice
     *                  will be a random value + or - 20%
     * @param moving    are we allowed to click the npc while our character is moving
     * @param animating are we allowed to click the npc while our character is animating
     */
    public InteractableNPC(String name, int ID, int wait, boolean moving, boolean animating) {
        this.name = name;
        this.ID = ID;
        this.wait = wait;
        this.waitMin = wait * .80;
        this.waitMax = wait * 1.20;
        this.moving = moving;
        this.animating = animating;
        this.npc = null;
        generateWait();
    }

    /**
     * specifies a world prop and how the player should interact with it
     * defaults wait time and moving check
     *
     * @param name name of prop
     * @param ID   unique prop id
     */
    public InteractableNPC(String name, int ID) {
        this(name, ID, DEFAULT_WAIT, DEFAULT_MOVING, DEFAULT_ANIMATING);
    }

    /**
     * rolls a new random wait value
     */
    public void generateWait() {
        this.wait = (int) Math.round(Calculations.random(this.waitMin, this.waitMax));
    }

    /**
     * rolls a new random wait value for static objects
     */
    private static void generateStaticWait(){
        waitRoll = (int) Math.round(Calculations.random(DEFAULT_WAIT*.9,DEFAULT_WAIT*1.1));
    }

    /**
     * resets last interact to a generic value so we can immediately interact with a new npc
     */
    public static void resetLastInteract(){
        lastInteract = new Pair<>("name", 0L);
    }

    /**
     * sets last interacted prop to our prop + current time
     * @param propName last acted on prop
     */
    private static void setLastInteract(String propName) {
        lastInteract = new Pair<>(propName, System.currentTimeMillis());
    }

    /**
     * sets last interacted prop to our prop + current time
     */
    private void setLastInteract(){
        setLastInteract(this.name);
    }

    /**
     * checks if we haven't interacted with an npc too recently
     * @return boolean representing if we are allowed to interact with the prop
     */
    public boolean shouldAct(Player player){
        long currentTime = System.currentTimeMillis();
        long lastTime = lastInteract.getValue();

        //don't act if we already acted on this prop recently
        if(lastInteract.getKey().equals(this.name) && lastTime > currentTime - this.wait) {
            Logger.info("too early to interact with " + this.name
                    + " (" + (currentTime - lastTime) + " / " + this.wait + " ms)");
            return false;

            //don't act if we want to wait for player to stop moving
        }else if((!this.moving) && player.isMoving()){
            Logger.info("can't interact with " + this.name + " while moving");
            return false;

            //don't act if we want to wait for animations to stop
        }else if((!this.animating) && player.isAnimating()) {
            Logger.info("can't interact with " + this.name + " while animating");
            return false;

            //let's interact with the prop
        }else{
            return true;
        }
    }

    /**
     * checks if we haven't interacted with an npc too recently
     * @param propName prop we want to interact with
     * @return boolean representing if we are allowed to interact with the prop
     */
    private static boolean shouldAct(String propName){
        long currentTime = System.currentTimeMillis();
        long lastTime = lastInteract.getValue();

        //if current prop isn't our last interacted and lastTime is still more than currentTime - wait
        boolean shouldNot = (lastInteract.getKey().equals(propName) && lastTime > currentTime - waitRoll);

        if(shouldNot) {
            Logger.info("too early to interact with " + propName
                    + " (" + (currentTime - lastTime) + " / " + waitRoll + " ms)");
        }else{
            generateStaticWait();
        }

        //TODO: make this not a double negative
        return !shouldNot;
    }

    /**
     * attempts to do action on this npc
     * @param action what action to do
     * @param da where the npc is
     * @param player local player object
     * @return true if interact was successful
     */
    public boolean interactWith(String action, DefinedArea da, Player player){
        // check if we already interacted with a similar object recently, quit if so
        if(!shouldAct(player)) return true;

        //grab nearest object with our id, in our area, with our action
        this.npc = NPCs.closest(n ->
                n.getName().equals(this.getName())
                && da.contains(n.getTile())
                && n.canReach()
                && n.hasAction(action));

        // if we found a nearby object, let's try interacting with it and return our success
        if(this.npc != null){
            Logger.info("found " + this.name + " to " + action);
            boolean success = this.npc.interact(action);
            if(success){
                Logger.info("successfully did " + action + " on " + this.name);
                setLastInteract();
            }else{
                Logger.error("failed " + action + " on " + this.name);
            }
            return success;

            //if we didn't find object, quit out
        }else{
            Logger.warn("no valid " + this.name + " to " + action);
            return false;
        }
    }

    /**
     * attempts to interact with npc but moves closer if too far
     * @param player local player object
     * @param action what we want to do
     * @param area where npc is
     * @param n navigator object
     * @return true if move or interact was successful
     */
    public boolean moveToInteract(String action, DefinedArea area, Player player, Navigator n){
        // check if we already interacted with a similar object recently, quit if so
        if(!shouldAct(player)) return true;

        //check if area is too far for a realistic interact
        if(area.getNearestTile(player).distance() > MAX_DISTANCE){
            Logger.info(this.getName() + " too far, just moving instead");
            return n.moveIfTime(area, player);
        }

        //grab nearest object with our id
        this.npc = NPCs.closest(r ->
                r.getName().equals(this.getName())
                && r.hasAction(action)
                && area.contains(r.getTile()));

        //if we didn't find object
        if(this.npc == null){
            Logger.info("no valid " + this.name + " to do " + action);
            return n.moveIfTime(area, player);

        // if we found a nearby object, let's try interacting with it and return our success
        }else{
            boolean success = this.npc.interact(action);
            if(success){
                Logger.info("successfully did " + action + " on " + this.name);
                setLastInteract();
            }else{
                Logger.error("failed to do " + action + " on " + this.name);
                n.moveIfTime(area, player);
            }
            return success;
        }
    }

    public static NPC getRandomNPC(){
        try {
            List<NPC> npcs = NPCs.all(n -> n.distance() < 12);
            Logger.info("npcs found " + npcs.size());
            NPC npc = npcs.get(Calculations.random(0, (npcs.size()-1)));
            assert npc != null;
            Logger.info("randomly selected npc is " + npc.getName());
            return npc;
        }catch(Exception err) {
            Logger.error("failed to get random NPC: " + err);
            return null;
        }
    }

}
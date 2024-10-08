import javafx.util.Pair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.methods.map.Area;

import java.util.List;

public class InteractableProp {

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
    GameObject gObj;

    /**
     * specifies a world prop and how the player should interact with it
     * @param name name of prop
     * @param ID unique prop id
     * @param wait how long we should wait before clicking the same prop twice
     *             will be a random value + or - 20%
     * @param moving are we allowed to click the prop while our character is moving
     * @param animating are we allowed to click the prop while our character is animating
     */
    public InteractableProp(String name, int ID, int wait, boolean moving, boolean animating){
        this.name = name;
        this.ID = ID;
        this.wait = wait;
        this.waitMin = wait*.80;
        this.waitMax = wait*1.20;
        this.moving = moving;
        this.animating = animating;
        this.gObj = null;
        generateWait();
    }

    /**
     * specifies a world prop and how the player should interact with it
     * defaults wait time and moving check
     * @param name name of prop
     * @param ID unique prop id
     */
    public InteractableProp(String name, int ID){
        this(name, ID, DEFAULT_WAIT, DEFAULT_MOVING, DEFAULT_ANIMATING);
    }

    /**
     * checks if we haven't interacted with a prop too recently
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
     * resets last interact to a generic value so we can immediately interact with a new prop
     */
    public static void resetLastInteract(){
        lastInteract = new Pair<>("name", 0L);
    }

    /**
     * rolls a new random wait value
     */
    public void generateWait() {
        this.wait = (int) Math.round(Calculations.random(this.waitMin,this.waitMax));
    }

    private static void generateStaticWait(){
        waitRoll = (int) Math.round(Calculations.random(DEFAULT_WAIT*.9,DEFAULT_WAIT*1.1));
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
     * Attempts to do a specified action in a specified area on this InteractableObj
     *
     * @param action the right-click menu action option
     * @param da the DefinedArea where the InteractableObj must exist
     * @return boolean representing whether we did an action or not
     * @see GameObject
     */
    public boolean interactWith(String action, DefinedArea da, Player player){

        // check if we already interacted with a similar object recently, quit if so
        if(!shouldAct(player)) return true;

        //grab nearest object with our id, in our area, with our action
        this.gObj = GameObjects.closest(r ->
                r.getID() == this.ID
                && da.contains(r.getTile())
                && r.canReach()
                && r.hasAction(action));

        // if we found a nearby object, let's try interacting with it and return our success
        if(this.gObj != null){
            Logger.info("found " + this.name + " to " + action);
            boolean success = this.gObj.interact(action);
            if(success){
                Logger.info("successfully " + action + " on " + this.name);
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
     * Attempts to left-click interact with this InteractableObj
     *
     * @return boolean returns false if can't find object
     * @see GameObject
     */
    public boolean interactWith(Player player){
        // check if we already interacted with a similar object recently, quit if so
        if(!shouldAct(player)) return true;

        //grab nearest object with our id
        this.gObj = GameObjects.closest(r ->
            r.getID() == this.ID
            && r.canReach());

        // if we found a nearby object, let's try interacting with it and return our success
        if(this.gObj != null){
            Logger.info("found " + this.name + " to do default action");
            boolean success = this.gObj.interact();
            if(success){
                Logger.info("successfully did default action on " + this.name);
                setLastInteract();
            }else{
                Logger.error("failed to do default action on " + this.name);
            }
            return success;

        //if we didn't find object, quit out
        }else{
            Logger.warn("no valid " + this.name + " to do default action");
            return false;
        }
    }

    /**
     * try to interact and move closer if unsuccessful
     * @param player local player
     * @param action right click option
     * @param area where the prop is
     * @param n our navigator handle
     * @return true if successful
     */
    public boolean moveToInteract(Player player, String action, DefinedArea area, Navigator n){
        // check if we already interacted with a similar object recently, quit if so
        if(!shouldAct(player)) return true;

        //check if area is too far for a realistic interact
        if(area.getNearestTile(player).distance() > MAX_DISTANCE){
            Logger.info(this.getName() + " too far, just moving instead");
            return n.moveIfTime(area, player);
        }

        //grab nearest object with our id
        this.gObj = GameObjects.closest(r ->
                r.getID() == this.ID
                && r.hasAction(action)
                && area.contains(r.getTile())
                && r.canReach());

        //if we didn't find object
        if(this.gObj == null){
            Logger.info("no valid " + this.name + " to do " + action);
            return n.moveIfTime(area, player);

        // if we found a nearby object, let's try interacting with it and return our success
        }else{
            boolean success = this.gObj.interact(action);
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

    /**
     * try to interact using default action and move closer if unsuccessful
     * @param player local player
     * @param area where the prop is
     * @param n our navigator handle
     * @return true if successful
     */
    public boolean moveToInteract(Player player, DefinedArea area, Navigator n){
        // check if we already interacted with a similar object recently, quit if so
        if(!shouldAct(player)) return true;

        //check if area is too far for a realistic interact
        if(area.getNearestTile(player).distance() > MAX_DISTANCE){
            Logger.info(this.getName() + " too far, just moving instead");
            return n.moveIfTime(area, player);
        }

        //grab nearest object with our id
        this.gObj = GameObjects.closest(r ->
                r.getID() == this.ID
                && area.contains(r.getTile())
                && r.canReach());

        //if we didn't find object
        if(this.gObj == null){
            Logger.info("no valid " + this.name + " to do default action");
            return n.moveIfTime(area, player);

            // if we found a nearby object, let's try interacting with it and return our success
        }else{
            boolean success = this.gObj.interact();
            if(success){
                Logger.info("successfully did default action on " + this.name);
                setLastInteract();
            }else{
                Logger.error("failed to do default action on " + this.name);
                n.moveIfTime(area, player);
            }
            return success;
        }
    }

    /**
     * interacts with the nearest prop in array - will just move closer to the prop instead if too far
     * @param props props we want to look for
     * @param player local player
     * @param n navigator object to use
     * @return boolean representing if we found an object
     */
    public static boolean searchAndMove(InteractableProp[] props, Player player, Navigator n){
        GameObject closestProp = null;
        GameObject found;

        //search for every prop in our array
        for(InteractableProp prop : props){
            Logger.debug("looking for " + prop.getName() + " (" + prop.getID() + ") ");

            found = GameObjects.closest(r ->
                r.getName().equals(prop.getName())
                && r.canReach());

            //if we didn't find a valid prop nearby that matches
            if(found == null){
                Logger.info("didn't find " + prop.getName());
                continue;

            //if we found a match
            }else{
                Logger.debug("found " + found.getName() + " (" + found.getID() + ") " +
                        (int) found.distance() + " units away");
            }

            //if this match is closer than previous matches
            if(closestProp == null || found.distance() < closestProp.distance()){
                closestProp = found;
            }
        }

        //check if we found a valid prop
        if(closestProp==null){
            Logger.debug("no valid prop in array");
            return false;
        }

        String closestName = closestProp.getName();
        int distance = (int) closestProp.distance();
        Logger.debug("closest prop was " + closestName + ", " + distance + " units away");

        // if prop is far, move closer instead of interacting
        if(distance > MAX_DISTANCE){
            int x = closestProp.getX();
            int y = closestProp.getY();
            return n.moveIfTime(new Area(x-1, y-1, x+1, y+1), player);
        }

        //if close, double check that we haven't interacted with this prop too recently
        if(!shouldAct(closestName) && (player.isMoving() || distance <= 1)) {
            return true;
        }

        //let's try interacting with it and return our success
        boolean success = closestProp.interact();
        if(success){
            Logger.info("successfully did default action on " + closestName);
            setLastInteract(closestName);
        }else{
            Logger.error("failed to do default action on " + closestName);
        }
        return success;
    }

    public static GameObject getRandomProp(){
        try {
            List<GameObject> props = GameObjects.all(n -> n.distance() < 12 && !n.getName().contains("null"));
            Logger.info("props found " + props.size());
            GameObject prop = props.get(Calculations.random(0, (props.size()-1)));
            assert prop != null;
            Logger.info("randomly selected prop is " + prop.getName());
            return prop;
        }catch(Exception err) {
            Logger.error("failed to get random prop: " + err);
            return null;
        }
    }

}

import javafx.util.Pair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

public class InteractableProp {

    static private Pair<String, Long> lastInteract = new Pair<>("name", 0L);

    final static int DEFAULT_WAIT = 4200;
    final static boolean DEFAULT_MOVING = true;
    final static boolean DEFAULT_ANIMATING = true;

    final private String name;
    final private int ID;
    private double wait;
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
    public boolean shouldNotAct(Player player){
        long currentTime = System.currentTimeMillis();
        long lastTime = lastInteract.getValue();

        //don't act if we already acted on this prop recently
        if(lastInteract.getKey().equals(this.name) && lastTime > currentTime - this.wait) {
            Logger.info("too early to interact with " + this.name
                    + " (" + (currentTime - lastTime) + " / " + this.wait + " ms)");
            return true;

        //don't act if we want to wait for player to stop moving
        }else if((!this.moving) && player.isMoving()){
            Logger.info("can't interact with " + this.name + " while moving");
            return true;

        //don't act if we want to wait for animations to stop
        }else if((!this.animating) && player.isAnimating()) {
            Logger.info("can't interact with " + this.name + " while animating");
            return true;

        //let's interact with the prop
        }else{
            return false;
        }
    }

    /**
     * rolls a new random wait value
     */
    public void generateWait() {
        this.wait = Math.round(Calculations.random(this.waitMin,this.waitMax)*100)/100D;
    }

    /**
     * sets last interacted prop to our prop + current time
     */
    public void setLastInteract(){
        lastInteract = new Pair<>(this.name, System.currentTimeMillis());
        generateWait();
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
        if(shouldNotAct(player)) return false;

        //grab nearest object with our id, in our area, with our action
        this.gObj = GameObjects.closest(r ->
                r.getID() == this.ID
                && da.getArea().contains(r.getTile())
                && r.canReach()
                && r.hasAction(action));

        // if we found a nearby object, let's try interacting with it and return our success
        if(this.gObj != null){
            Logger.info("found " + this.name + " to " + action);
            boolean success = this.gObj.interact(action);
            if(success){
                Logger.info("sucessfully " + action + " on " + this.name);
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
     * @return boolean representing whether we did an action or not
     * @see GameObject
     */
    public boolean interactWith(Player player){

        // check if we already interacted with a similar object recently, quit if so
        if(shouldNotAct(player)) return false;

        //grab nearest object with our id
        this.gObj = GameObjects.closest(r ->
                r.getID() == this.ID
                && r.canReach());

        // if we found a nearby object, let's try interacting with it and return our success
        if(this.gObj != null){
            Logger.info("found " + this.name + " to do default action");
            boolean success = this.gObj.interact();
            if(success){
                Logger.info("sucessfully did default action on " + this.name);
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

}

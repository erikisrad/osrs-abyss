import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

public class InteractableItem {

    private final String name;
    private final int[] IDs;

    public int[] getIDs() {
        return IDs;
    }

    public String getName() {
        return name;
    }

    public InteractableItem(String name, int... IDs) {
        this.name = name;
        this.IDs = IDs;
    }

    /**
     * searches nearby area for this item on the ground
     *
     * @param radius how close to the player the item must be
     * @param area what area the item must be in
     * @return boolean representing whether we found the item nearby - not whether we successfully picked it up
     */
    public boolean collectItem(int radius, Area area, Player player){
        //find nearest item that matches what we want
        GroundItem nearItem = GroundItems.closest(
                item -> item.isOnScreen()
                && item.getID() == this.IDs[0]
                && item.distance(player.getTile()) <= radius
                && item.canReach()
                && area.contains(item.getTile()));

        //if we can take it, do so
        if (nearItem != null && nearItem.hasAction("Take")) {
            Logger.info("taking " + this.name);
            nearItem.interact("Take");

            //pause until item isn't on ground any more or 4sec
            if(Sleep.sleepUntil(() -> (!nearItem.exists()), 4000)){
                Logger.info("grabbed " + this.name);
            }else{
                Logger.warn("grab timed out for " + this.name);
            }
            return true;

        }else{
            Logger.debug("didn't find " + this.name + " within " + radius + " tiles");
            return false;
        }
    }

    /**
     * counts number of copies of this item in inventory
     * @return int, count of item held
     */
    public int inInventoy(){
        int count = 0;
        for(int i : this.getIDs()){
            count += Inventory.count(i);
        }
        return count;
    }

    /**
     * queries if specified item is in local players inventory
     * @return boolean representing if player has that item in inventory
     */
    public boolean inInventory(){
        return Inventory.contains(this.getIDs());
    }

    /**
     * checks if our player is wearing all the specified items
     * @param items array of items we want to check
     * @return boolean representing if we are wearing all the items
     */
    public static boolean inEquipment(InteractableItem[] items){
        for(InteractableItem item : items){
            Logger.debug("checking for " + item.getName());
            if(!item.inEquipment()) return false;
        }
        return true;
    }

    /**
     * queries if specified item is in local players equipment
     * this query is done via inspecting the players equipment rather than inspecting their model
     * @return boolean representing if player has that item equipped
     */
    public boolean inEquipment(){
        return Equipment.contains(this.getIDs());
    }

    /**
     * equips an item in our inventory
     * @return if we successfully equipped it
     */
    public boolean equipItem(){
        Item wearable = Inventory.get(r ->
                r.getName().contains(this.getName())
                        && r.hasAction("Wear"));

        if(wearable != null){
            return Inventory.interact(wearable, "Wear");
        }

        Item wieldable = Inventory.get(r ->
                r.getName().contains(this.getName())
                        && r.hasAction("Wield"));

        if(wieldable != null){
            return Inventory.interact(wieldable, "Wield");
        }

        return false;
    }

}
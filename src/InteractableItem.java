import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import java.util.HashSet;
import java.util.Objects;

public class InteractableItem {

    private final static int RETRIES = 2;

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

    public static boolean allInInventory(InteractableItem[] items){
        for(InteractableItem item : items){
            Logger.debug("checking for " + item.getName());
            if(!item.inInventory()) return false;
        }
        return true;
    }

    public static boolean anyInInventory(InteractableItem[] items){
        for(InteractableItem item : items){
            Logger.debug("checking for " + item.getName());
            if(item.inInventory()) return true;
        }
        return false;
    }

    public static boolean inInventory(InteractableItem[] items){
        for(InteractableItem item : items){
            Logger.debug("checking for " + item.getName());
            if(!item.inInventory()) return false;
        }
        return true;
    }

    /**
     * checks if our player is wearing all the specified items
     * @param items array of items we want to check
     * @return boolean representing if we are wearing all the items
     */
    public static boolean allInEquipment(InteractableItem[] items){
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
        for(int id : this.IDs){
            Item wearable = Inventory.get(r ->
                    r.getID() == id
                    && r.hasAction("Wear"));

            if (wearable != null) {
                Logger.info("wearing " + wearable.getName());
                return Inventory.interact(wearable, "Wear");
            }
            Logger.warn(this.name + " (" + id + ") not wearable");

            Item wieldable = Inventory.get(r ->
                    r.getID() == id
                    && r.hasAction("Wield"));

            if (wieldable != null) {
                Logger.info("wielding " + wieldable.getName());
                return Inventory.interact(wieldable, "Wield");
            }
            Logger.warn(this.name + " (" + id + ") not wieldable");
        }

        //final attempt
        Logger.warn("trying to force wear/wield for " + this.name);
        Item item = Inventory.get(r -> r.getName().contains(this.name));
        if(item != null){
            Logger.info("found matching item to force wield " + this.name);
            return(Inventory.interact(item, "Wear") || Inventory.interact(item, "Wield"));
        }else{
            Logger.warn("failed to force wear/wield for " + this.name);
        }

        Logger.error(this.name + " not wearable or wieldable");
        return false;
    }

    /**
     * wrapper for Bank.depositAllExcept
     * @return true if we successfully deposited all but this item
     */
    public boolean depositAllExcept(){
        return Bank.depositAllExcept(this.getIDs());
    }

    public static void depositAllExcept(InteractableItem[] items){
        //make set of all items we want to exclude from deposit
        HashSet<Integer> targets = new HashSet<>();
        for(InteractableItem item : items){
            for(int id : item.getIDs()) {
                targets.add(id);
            }
        }

        //if item isn't excluded, deposit it
        HashSet<Integer> invSet = new HashSet<>();
        for(Item inv : Inventory.all()){
            if(inv != null && !targets.contains(inv.getID()) && !invSet.contains(inv.getID())){
                invSet.add(inv.getID());
                Bank.depositAll(inv.getID());
            }
        }
    }

    /**
     * wrapper for Inventory.onlyContains
     * @return true if inventory only contains this item
     */
    public boolean onlyContains(){
        return Inventory.onlyContains(this.getIDs());
    }

    public static boolean onlyContains(InteractableItem[] items){
        //make set of all item ids
        HashSet<Integer> targets = new HashSet<>();
        for(InteractableItem item : items){
            for(int id : item.getIDs()) {
                targets.add(id);
            }
        }

        return Inventory.onlyContains(targets.toArray(new Integer[0]));
    }

    /**
     * tries to withdraw an amount of items from bank - uses all available IDs
     * @param amount
     * @return true if withdraw succeeded
     */
    public boolean withdraw(int amount){
        for(int id : this.IDs){
            if(Bank.withdraw(id, amount)) return true;
        }
        return false;
    }

    /**
     * tries to withdraw one copy of an item
     * @return true if withdraw succeeds
     */
    public boolean withdraw(){
        return withdraw(1);
    }

    public boolean hasAction(String action){
        Item i = Inventory.get(n ->
                    n.getName().equals(this.name)
                    && n.hasAction(action));
        return(i!=null);
    }

    /**
     * tries to specified action on item - uses all available IDs
     * @param action
     * @return true if interact succeeded
     */
    public boolean interact(String action){
        for(int id : this.IDs){
            if(Inventory.interact(id, action)) return true;
        }
        return false;
    }

    /**
     * price checks this item
     * @return item's value
     */
    public int getPrice(){
        int price;
        for(int id : this.IDs){
            try{
                price = LivePrices.get(id);
                return price;
            }catch(Exception ignored){}
        }
        Logger.error("failed to price check " + this.name);
        return 0;
    }

    public static Item getRandomItem(){
        if(!Inventory.isEmpty()){
            Item item = Inventory.getRandom(Objects::nonNull);
            Logger.info("randomly selected item is " + item.getName());
            return item;
        }
        else{
            Logger.error("inventory is empty!");
            return null;
        }
    }

}
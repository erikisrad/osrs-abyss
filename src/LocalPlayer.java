import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.interactive.Player;

public class LocalPlayer extends HumanPlayer{
    public LocalPlayer(Player player){
        super(player);
    }

    /**
     * queries if specified item is in local players inventory
     * this query is done via inspecting the players equipment rather than inspecting their model
     * @param item item we are looking for
     * @return boolean representing if player has that item equipped
     */
    @Override
    public boolean inInventory(InteractableItem item){
        return Equipment.contains(item.getID());
    }

    /**
     * checks if our player is wearing all the specified items
     * @param items array of items we want to check
     * @return boolean representing if we are wearing all the items
     */
    public boolean wearingOutfit(InteractableItem[] items){
        for(InteractableItem item : items){
            //if our primary ID matches an item we have equipped, continue and check next item
            if(Equipment.contains(item.getID())){
                continue;

            //if our primary ID didn't match, and we have no alt IDs, we are not wearing the outfit
            }else if(item.getAltIDs() == null) {
                return false;

            //if our primary ID didn't match but one of our alt IDs did, check next item
            }else if(Equipment.contains(item.getAltIDs())){
                continue;

            //if we didn't match the primary ID or any alts, we are not wearing the outfit
            }else{
                return false;
            }
        }
        return true;
    }
}

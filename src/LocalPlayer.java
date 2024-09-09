import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.Arrays;

public class LocalPlayer extends HumanPlayer{
    public LocalPlayer(Player player){
        super(player);
    }

    /**
     * queries if specified item is in local players inventory
     * @param item item we are looking for
     * @return boolean representing if player has that item in inventory
     */
    public boolean inInventory(InteractableItem item){
        return Inventory.contains(item.getIDs());
    }

    /**
     * checks if our player is wearing all the specified items
     * @param items array of items we want to check
     * @return boolean representing if we are wearing all the items
     */
    public boolean inEquipment(InteractableItem[] items){
        for(InteractableItem item : items){
            if(!inInventory(item)) return false;
        }
        return true;
    }

    /**
     * queries if specified item is in local players equipment
     * this query is done via inspecting the players equipment rather than inspecting their model
     * @param item item we are looking for
     * @return boolean representing if player has that item equipped
     */
    @Override
    public boolean inEquipment(InteractableItem item){
        return Equipment.contains(item.getIDs());
    }
}

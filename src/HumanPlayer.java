import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.Arrays;
import java.util.List;

public class HumanPlayer {

    public Player getPlayer() {
        return player;
    }

    private Player player;

    public HumanPlayer(Player player){
        this.player = player;
    }

    /**
     * observes the model of player to inspect items and determines if they are wearing a specific item
     * @param item item we are looking for
     * @return boolean representing if player has that item equipped
     */
    public boolean inEquipment(InteractableItem item){
        for(Item eqp : this.player.getEquipment()){
            if(Arrays.stream(item.getIDs()).noneMatch(i -> (i==eqp.getID()))){
                return false;
            }
        }
        return true;
    }
}


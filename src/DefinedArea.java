import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

public class DefinedArea extends Area{

    /**
     * default syntax where z (vertical plane) is defined
     * @param name
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param z
     */
    final private String name;

    public DefinedArea(String name, int x1, int y1, int x2, int y2, int z){
        super(x1, y1, x2, y2, z);
        this.name = name;
    }

    //no z coordinate defined, default to 0
    public DefinedArea(String name, int x1, int y1, int x2, int y2){
        super(x1, y1, x2, y2);
        this.name = name;
    }

    /**
     * Checks if the player is in the area
     *
     * @return boolean representing whether the player is in the DefinedArea
     * @see Area
     */
    public boolean playerIn(Player player){
        return this.contains(player.getTile());
    }

    static public boolean playerIn(DefinedArea[] areas, Player player){
        for (DefinedArea definedArea : areas) {
            if (definedArea.playerIn(player)) {
                return true;
            }
        }
        return false;
    }
}



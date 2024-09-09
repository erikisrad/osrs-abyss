import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

public class DefinedArea {

    private final int x1;
    private final int x2;
    private final int y1;
    private final int y2;
    private final int z;

    public Area getArea() {
        return area;
    }

    private final Area area;

    /**
     * default syntax where z (vertical plane) is defined
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param z
     */
    public DefinedArea(int x1, int y1, int x2, int y2, int z){
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z = z;
        this.area = new Area(x1, y1, x2, y2, z);
    }

    //no z coordinate defined, default to 0
    public DefinedArea(int x1, int y1, int x2, int y2){
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z = 0;
        this.area = new Area(x1, y1, x2, y2);
    }

    /**
     * Checks if the player is in the area
     *
     * @return boolean representing whether the player is in the DefinedArea
     * @see Area
     */
    public boolean playerIn(Player player){
        return area.contains(player.getTile());
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



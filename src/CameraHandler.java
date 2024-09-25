import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.utilities.Logger;

/**
 * wrapper for the dreambot Camera class
 */
public class CameraHandler {

    public static final int NORTH = 0;
    public static final int EAST = 1536;
    public static final int SOUTH = 1024;
    public static final int WEST = 512;

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getMinPitch() {
        return minPitch;
    }

    public void setMinPitch(int minPitch) {
        this.minPitch = minPitch;
    }

    public int getMaxPitch() {
        return maxPitch;
    }

    public void setMaxPitch(int maxPitch) {
        this.maxPitch = maxPitch;
    }

    private int minZoom;
    private int maxZoom;
    private int minPitch;
    private int maxPitch;
    private int currentZoom;
    private int currentPitch;
    private int currentYaw;

    /**
     * Defines acceptable boundaries for camera zoom and pitch
     * @param minZoom
     * @param maxZoom
     * @param minPitch
     * @param maxPitch
     */
    public CameraHandler(int minZoom, int maxZoom, int minPitch, int maxPitch) {
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
    }

    /**
     * checks if current camera zoom is between defined min and max
     * @return boolean representing if current zoom is within min/max
     */
    public boolean checkZoom(){
        this.currentZoom = Camera.getZoom();

        boolean res = this.minZoom <= this.currentZoom && this.currentZoom <= this.maxZoom;
        if(!res) Logger.info("current zoom of " + this.currentZoom +
                " is not between " + this.minZoom + " and " + this.maxZoom);
        return res;
    }

    /**
     * checks if current camera pitch is between defined min and max
     * @return boolean representing if current pitch is within min/max
     */
    public boolean checkPitch(){
        this.currentPitch = Camera.getPitch();
        return this.minPitch <= this.currentPitch && this.currentPitch <= this.maxPitch;
    }

    /**
     * adjusts camera zoom to a random value between defined min and max
     */
    public void adjustZoom(){
        double mean = (double) (this.minZoom + this.maxZoom) / 2;
        double sigma = (double) (this.maxZoom - this.minZoom) / 4;
        int gaussian = (int) Calculations.nextGaussianRandom(mean, sigma);
        Logger.info("setting camera zoom to " + gaussian);
        Camera.setZoom(gaussian);
    }

    /**
     * adjusts camera pitch to a random value between defined min and max
     */
    public void adjustPitch(){
        double mean = (double) (this.minPitch + this.maxPitch) / 2;
        double sigma = (double) (this.maxPitch - this.minPitch) / 4;
        int gaussian = (int) Calculations.nextGaussianRandom(mean, sigma);
        Logger.info("setting camera pitch to " + gaussian);
        Camera.rotateToPitch(gaussian);
    }

    /**
     * adjusts camera yaw to a random value near desired yaw
     * @param yaw int between 0 and 2047
     */
    public void adjustYaw(int yaw){
        this.currentYaw = Camera.getYaw();

        //don't do anything if we are already looking there
        if(yaw-32+100 > this.currentYaw+100 || this.currentYaw+100 > yaw+32+100) {
            Logger.info("current yaw of " + this.currentYaw + " is out of bounds");
            double sigma = 15;
            int gaussian = (int) Calculations.nextGaussianRandom(yaw, sigma);
            //yaw cant be <0 or >2047
            if(gaussian<0) gaussian=2048+gaussian;
            else if(gaussian>2047) gaussian=gaussian - 2047;
            Logger.info("rotating camera yaw to " + gaussian);
            Camera.rotateToYaw(gaussian);
            this.currentYaw = gaussian;
        }
    }

    /**
     * rotate camera North
     */
    public void yawNorth(){adjustYaw(NORTH);}

    /**
     * rotate camera East
     */
    public void yawEast(){
        adjustYaw(EAST);
    }

    /**
     * rotate camera South
     */
    public void yawSouth(){
        adjustYaw(SOUTH);
    }

    /**
     * rotate camera West
     */
    public void yawWest(){adjustYaw(WEST);}



}

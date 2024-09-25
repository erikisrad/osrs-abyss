import org.dreambot.api.utilities.Logger;

public enum State{

    //UTILITIES
    CLOSING_DIALOGUE,
    ADJUSTING_CAMERA_PITCH,
    ADJUSTING_CAMERA_ZOOM,
    SPRINTING,

    //LOGIC
    USING_NATURE_ALTAR,
    ACTIVATING_BUTTON,
    NAVIGATING_ABYSS,
    TELEPORTING_TO_ABYSS,
    EQUIPPING_OUTFIT,
    WITHDRAWING_ESS,
    IN_ANIMATION,
    USING_SHRINE,
    USING_ENCLAVE_BANK,
    TELEPORTING_TO_EDGEVILLE,
    USING_EDGEVILLE_BANK,
    GOING_TO_EDGEVILLE,
    CLOSING_BANK,
    REPAIRING_POUCH,
    NO_ACTION;

    private static State currentState;
    private static int sameStateCount = 0;

    public static State getState(){
        return currentState;
    }

    public static void setState(State state) {
        if(currentState == state){
            sameStateCount += 1;
            if(sameStateCount%10==0) Logger.info("same state count: " + sameStateCount);
            if(sameStateCount > 240){
                Logger.error("script potentially stuck");
                //ScriptManager.getScriptManager().stop();
            }
        }else{
            currentState = state;
            Logger.log(currentState);
            sameStateCount = 0;
        }
    }
}
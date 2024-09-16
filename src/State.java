import org.dreambot.api.utilities.Logger;

public enum State{

    //UTILITIES
    CLOSING_DIALOGUE,
    ADJUSTING_CAMERA_PITCH,
    ADJUSTING_CAMERA_ZOOM,
    SPRINTING,

    //LOGIC
    USING_NATURE_ALTAR,
    ACTIVATING_DOOR,
    GOING_TO_BUTTON,
    NAVIGATING_ABYSS,
    TELEPORTING_TO_ABYSS,
    GOING_TO_MAGE,
    EQUIPPING_OUTFIT,
    WITHDRAWING_ESS,
    IN_ANIMATION,
    USING_SHRINE,
    GOING_TO_CHAPEL,
    USING_ENCLAVE_BANK,
    TELEPORTING_TO_EDGEVILLE,
    USING_EDGEVILLE_BANK,
    GOING_TO_EDGEVILLE,
    CLOSING_BANK,
    NO_ACTION;

    private static State currentState;

    public static State getState(){
        return currentState;
    }

    public static void setState(State state) {
        if(!(currentState == state)){
            currentState = state;
            Logger.log(currentState);
        }
    }
}
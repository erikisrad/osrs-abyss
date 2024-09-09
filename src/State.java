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
    GOING_TO_DOOR,
    SEARCHING_OUTER_RING,
    MOVING_TO_ROCK,
    HITTING_ROCK,
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
    GOING_TO_EDGEVILLE;

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

    public static void setState(int stateNum){
        State desiredState;
        switch(stateNum){
            case 0:
                desiredState = STAGE_ZERO;
                break;
            case 1:
                desiredState = STAGE_ONE;
                break;
            case 2:
                desiredState = STAGE_TWO;
                break;
            case 3:
                desiredState = STAGE_THREE;
                break;
            case 4:
                desiredState = STAGE_FOUR;
                break;
            case 5:
                desiredState = STAGE_FIVE;
                break;
            case 6:
                desiredState = STAGE_SIX;
                break;
            default:
                Logger.error("invalid state: " + stateNum);
                return;
        }
        if(!(currentState == desiredState)){
            currentState = desiredState;
            Logger.log(currentState);
        }
    }



}
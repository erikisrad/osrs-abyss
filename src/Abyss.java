import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;

@ScriptManifest(name = "Abyss", description = "x", author = "erik",
        version = 1.0, category = Category.RUNECRAFTING, image = "")

public class Abyss extends AbstractScript implements ChatListener {

    private AntiBan ab;
    private CameraHandler ch;
    private Navigator n;
    private LocalPlayer lp;

    final private int tickRate = 50;
    final private int retries = 20;

    //areas
    DefinedArea areaWildClose = new DefinedArea(3087, 3520, 3120, 3536);
    DefinedArea areaWildFar = new DefinedArea(3113, 3540, 3087, 3567);
    DefinedArea areaMageTeleBig = new DefinedArea(3102, 3561, 3108, 3539);
    DefinedArea areaMageTeleSmall = new DefinedArea(3102, 3561, 3110, 3556);
    DefinedArea areaEnclave = new DefinedArea(3155, 3646, 3123, 3617);
    DefinedArea areaChapel = new DefinedArea(3125, 3636, 3135, 3628);
    DefinedArea areaOuterRing = new DefinedArea(3011, 4860, 3069, 4804);
    DefinedArea areaInnerRing = new DefinedArea(3024, 4846, 3054, 4817);
    DefinedArea areaNatureButton = new DefinedArea(3030, 4840, 3038, 4844);
    DefinedArea areaNeRing = new DefinedArea(3039, 4832, 3069, 4861);
    DefinedArea areaNwRing = new DefinedArea(3038, 4832, 3010, 4860);
    DefinedArea areaSwRing = new DefinedArea(3038, 4831, 3010, 4804);
    DefinedArea areaSeRing = new DefinedArea(3039, 4831, 3067, 4804);
    DefinedArea areaNeSpawn = new DefinedArea(3053, 4851, 3057, 4847);
    DefinedArea areaNwSpawn = new DefinedArea(3020, 4851, 3024, 4847);
    DefinedArea areaSwSpawn = new DefinedArea(3019, 4815, 3023, 4811);
    DefinedArea areaSeSpawn = new DefinedArea(3054, 4814, 3058, 4810);
    DefinedArea areaNatureRealm = new DefinedArea(2390, 4851, 2409, 4832);
    DefinedArea areaBankEdgeville = new DefinedArea(3083, 3503, 3104, 3483);

    //items
    InteractableItem itemPureEss = new InteractableItem("Pure essence", 7936);

    //outfit
    InteractableItem[] itemOutfit = {
            new InteractableItem("Bronze pickaxe", 1265),
            new InteractableItem("Amulet of glory", 11978, 11976, 1712, 1710, 1708, 1706),
            new InteractableItem("Ring of dueling", 2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566)
    };

    //props
    InteractableProp propRock = new InteractableProp("Rock", 26574);
    InteractableProp propGap = new InteractableProp("Gap", 26250);
    InteractableProp propPassage = new InteractableProp("Passage", 26208);

    @Override
    public void onStart() {
        ab = new AntiBan(this);
        ch = new CameraHandler(220, 340, 300, 383);
        n = new Navigator();
        Logger.log("booting script...");

        Logger.info("grabbing local player object");
        int tries = 0;
        while(lp == null){
            if(tries>retries){
                throw new RuntimeException("failed to grab local player object!");
            }
            tries++;
            try {
                lp = new LocalPlayer(Players.getLocal());
            }catch(Exception err){
                ab.idleShort();
            }
        }
        Logger.info("grabbed local player in " + tries + " attempt(s)");
    }

    @Override
    public void onMessage(Message message) {
    }

    @Override
    public void onPaint(Graphics g){
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("State: " + State.getState(), 10, 32);
    }

    @Override
    public int onLoop() {
        ab.mouseJiggle();
        decideState();

        switch (State.getState()) {
            //UTILITIES
            case CLOSING_DIALOGUE:
                Dialogues.spaceToContinue();
                break;

            case ADJUSTING_CAMERA_PITCH:
                ch.adjustPitch();
                break;

            case ADJUSTING_CAMERA_ZOOM:
                ch.adjustZoom();
                break;

            case SPRINTING:
                n.toggleRun();
                break;

            //LOGIC


        }

        return tickRate + ab.getShortDelay();
    }

    private void decideState() {

        //UTILITIES
        if (Dialogues.canContinue()) {
            State.setState(State.CLOSING_DIALOGUE);
            return;
        }

        if (!ch.checkPitch()) {
            State.setState(State.ADJUSTING_CAMERA_PITCH);
            return;
        }

        if (!ch.checkZoom()) {
            State.setState(State.ADJUSTING_CAMERA_ZOOM);
            return;
        }

        if (n.shouldRun()) {
            State.setState(State.SPRINTING);
            return;
        }

        //LOGIC

        //STEP 0 - NATURE ALTAR
        boolean atAltar = areaNatureRealm.playerIn(lp.getPlayer());
        int essCount = itemPureEss.inInventoy();

        if(atAltar && essCount>0){
            State.setState((State.USING_NATURE_ALTAR));
            return;
        }

        //STEP 1 - INNER RING BUTTON
        boolean atButton = areaNatureButton.playerIn(lp.getPlayer());

        if(atButton){
            State.setState(State.ACTIVATING_DOOR);
            return;
        }

        //STEP 2 - INNER RING WALK
        boolean atInnerCircle = areaInnerRing.playerIn(lp.getPlayer());

        if(atInnerCircle){
            State.setState(State.GOING_TO_DOOR);
            return;
        }

        //STEP 3 - OUTER RING
        boolean atOuterRing = areaOuterRing.playerIn(lp.getPlayer());

        if(atOuterRing){
            State.setState(State.SEARCHING_OUTER_RING);
            return;
        }

        //STEP 4 -
        boolean atMage = areaMageTeleBig.playerIn(lp.getPlayer());

        if(atMage && essCount>0){
            State.setState(State.TELEPORTING_TO_ABYSS);
            return;
        }

        boolean bankOpen = Bank.isOpen();
        boolean wearingOutfit = lp.inEquipment(itemOutfit);

        Logger.debug("no state to set");
    }
}

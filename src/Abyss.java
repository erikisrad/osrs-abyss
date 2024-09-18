import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@ScriptManifest(name = "Abyss", description = "x", author = "erik",
        version = 1.0, category = Category.RUNECRAFTING, image = "")

public class Abyss extends AbstractScript implements ChatListener {

    private AntiBan ab;
    private CameraHandler ch;
    private Navigator n;
    private Navigator navAbyss;
    private Player lp;
    private String logName;
    private BufferedWriter logWriter;

    final private int retries = 20;

    //areas
    DefinedArea areaMageTeleBig = new DefinedArea("mageTeleBig", 3102, 3561, 3108, 3539);
    DefinedArea areaMageTeleSmall = new DefinedArea("mageTeleSmall", 3102, 3561, 3110, 3556);
    DefinedArea areaEnclave = new DefinedArea("enclave", 3155, 3646, 3123, 3617);
    DefinedArea areaChapel = new DefinedArea("chapel", 3125, 3636, 3135, 3628);
    DefinedArea areaOuterRing = new DefinedArea("outerRing", 3011, 4860, 3069, 4804);
    DefinedArea areaInnerRing = new DefinedArea("innerRing", 3024, 4846, 3054, 4817);
    DefinedArea areaNatureButton = new DefinedArea("natureButton", 3030, 4840, 3048, 4846);
    DefinedArea areaNatureRealm = new DefinedArea("natureRealm", 2390, 4851, 2409, 4832);
    DefinedArea areaBankEdgeville = new DefinedArea("bankEdgeville", 3083, 3503, 3104, 3483);

    //items
    InteractableItem itemPureEss = new InteractableItem("Pure essence", 7936);
    InteractableItem itemNatRune = new InteractableItem("Nature rune", 561);

    InteractableItem[] itemPouches = {
            new InteractableItem("Small pouch", 5509),
            new InteractableItem("Medium pouch", 5510),
            new InteractableItem("Large pouch", 5512)
    };

    //outfit
    InteractableItem[] itemOutfit = {
            new InteractableItem("Bronze pickaxe", 1265),
            new InteractableItem("Amulet of glory", 11978, 11976, 1712, 1710, 1708, 1706),
            new InteractableItem("Ring of dueling", 2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566)
    };

    //props
    InteractableProp[] abyssProps = {
        new InteractableProp("Rock", 26574),
        new InteractableProp("Gap", 26250),
        //new InteractableProp("Passage", 26208)
    };

    InteractableProp propNatAltar = new InteractableProp("Altar", 34768);
    InteractableProp propNatRift = new InteractableProp("Nature rift", 24975);
    InteractableProp propMage = new InteractableProp("Mage of Zamorak", 3228);
    InteractableProp propPool = new InteractableProp("Pool of refreshment", 39651);
    InteractableProp propBankChest = new InteractableProp("Bank chest", 26711);
    InteractableProp propBankBooth = new InteractableProp("Bank booth", 10355);

    public BufferedWriter createLog(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        logName = dateFormat.format(date) + "abyss_output.txt";
        File file = new File(logName);
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new FileWriter(file, true));
        }catch(IOException e){
            Logger.error("failed to create log: " + e);
            ScriptManager.getScriptManager().stop();
        }
        return out;
    }

    public void writeLog(String msg){
        try {
            logWriter.write(msg);
            logWriter.close();
        } catch (IOException e) {
            Logger.error("failed to write log message: " + msg);
            Logger.error(e);
        }
    }

    @Override
    public void onStart() {
        ab = new AntiBan(this, 300);
        ch = new CameraHandler(220, 340, 300, 383);
        n = new Navigator();
        navAbyss = new Navigator(0, 1200, 2200);
        //logWriter = createLog();
        Logger.log("booting script...");

        int tries = 0;
        while(lp == null){
            if(tries>retries){
                Logger.error("failed to grab local player object!");
                ScriptManager.getScriptManager().stop();
            }
            tries++;
            try {
                Logger.info("grabbing local player object");
                lp = Players.getLocal();
            }catch(Exception err){
                Logger.error(err.toString());
                ab.idleShort();
            }
        }
        Logger.info("grabbed local player in " + tries + " attempt(s)");
    }

    final String rockFail = "...but fail to break-up the rock.";
    final String rockSucceed = "..and manage to break through the rock.";
    final String gapFail = "...but you are not agile enough to get through the gap.";
    final String gapSucceed = "...and you manage to crawl through.";

    @Override
    public void onMessage(Message message) {
        if(message.getMessage().contains(rockFail) ||
                message.getMessage().contains(gapFail)){

            Logger.info("reset prop interact");
            InteractableProp.resetLastInteract();

        }else if(message.getMessage().contains(rockSucceed) ||
                message.getMessage().contains(gapSucceed)){

            Logger.info("we're in");
            ab.idleLong();

        }else{
            Logger.info("bogus message of type " + message.getType() + ": " + message.getMessage());
        }
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
            case USING_NATURE_ALTAR:

                //try and craft runes
                if(propNatAltar.interactWith(lp)) {
                    Logger.info("crafting runes");
                    sleepUntil(() -> !itemPureEss.inInventory(), 4000, 100);

                    //try and empty pouch
                    for(InteractableItem pouch : itemPouches) {
                        if (pouch.interact("Empty")) {
                            Logger.info("emptied " + pouch.getName());
                            ab.idleShort();
                            InteractableProp.resetLastInteract();

                            //if we couldn't empty pouch
                        } else if (pouch.inInventory()) {
                            Logger.error("failed to empty " + pouch.getName());
                        } else {
                            Logger.warn(pouch.getName() + " not in inventory");
                        }
                    }

                    //try and craft more runes
                    if (itemPureEss.inInventory() && propNatAltar.interactWith(lp)) {
                        sleepUntil(() -> !itemPureEss.inInventory(), 4000, 100);
                        int runeCount = itemNatRune.inInventoy();
                        Logger.info("crafted " + runeCount + " runes");

                        //if we emptied a pouch but couldn't craft more
                    } else {
                        Logger.error("failed to craft runes again");
                    }

                //if we couldn't craft any runes at all
                }else{
                    Logger.warn("failed to start crafting runes");
                }
                break;

            case ACTIVATING_DOOR:
                if(!propNatRift.interactWith(lp)) {
                    Logger.warn("failed to exit through nature rift");
                }
                break;

            case GOING_TO_BUTTON:
                ch.yawSouth();
                if(!n.moveIfTime(areaNatureButton, lp)){
                    Logger.warn("failed to walk to button");
                }
                break;

            case NAVIGATING_ABYSS:
                InteractableProp.interactOrMove(abyssProps, lp, navAbyss);
                break;

            case TELEPORTING_TO_ABYSS:
                if(!propMage.talkWith("Teleport", areaMageTeleSmall, lp)){
                    Logger.info("couldn't find mage - moving closer");
                    n.moveIfTime(areaMageTeleSmall, lp);
                }else{
                    Logger.debug("teleporing");
                }
                break;

            case GOING_TO_MAGE:
                ch.yawNorth();
                if(!n.moveIfTime(areaMageTeleSmall, lp)){
                    Logger.warn("failed to run to large mage area");
                }
                break;

            case EQUIPPING_OUTFIT:
                Inventory.open();
                if(14>Inventory.emptySlotCount()){
                    Bank.depositAllItems();
                    ab.idleShort();
                }

                for(InteractableItem item : itemOutfit){
                    if(!item.inEquipment()){
                        if(!Bank.withdraw(i -> i.getName().contains(item.getName()))){
                            Logger.error("failed to withdraw " + item.getName());
                            return -1; //exit script
                        }
                        ab.idleShort();
                        if(!item.equipItem()){
                            Logger.error("failed to equip " + item.getName());
                            return -1; //exit script
                        }else{
                            Logger.info(item.getName() + " equipped");
                            ab.idleShort();
                        }
                    }else{
                        Logger.debug(item.getName() + " already worn");
                    }
                }

                ab.idleShort();
                break;

            case WITHDRAWING_ESS:
                Inventory.open();
                InteractableItem.depositAllExcept(itemPouches);

                //try and make sure we have a pouch
                if(!itemSmallPouch.inInventory()){
                    Logger.warn("we don't have a pouch");
                    if(!Inventory.isEmpty()) Bank.depositAllItems();
                    if(!itemSmallPouch.withdraw()){

                        //if we can't get a pouch, just get ess
                        Logger.error("couldn't withdraw pouch");
                        if(itemPureEss.withdraw(28)) {
                            Logger.debug("withdrew ess");
                            break;

                        }else{
                            Logger.error("failed to withdraw ess with no pouch");
                            return -1;
                        }
                    }
                }

                //if we have pouch, deposit everything else
                if(!itemSmallPouch.onlyContains()){
                    itemSmallPouch.depositAllExcept();
                    ab.idleShort();
                }

                //get full inventory of ess
                if(itemPureEss.withdraw(28)){
                    Logger.debug("withdrew ess");
                    ab.idleShort();

                    //fill pouch
                    if(itemSmallPouch.interact("Fill")) {
                        Logger.debug("filled pouch");
                        ab.idleShort();

                        //fill inventory with ess again
                        if(itemPureEss.withdraw(28)) {
                            Logger.debug("withdrew ess again");

                        }else{
                            Logger.warn("failed to get more ess");
                        }

                    }else{
                        Logger.warn("failed to fill pouch");
                    }

                }else{
                    Logger.error("failed to withdraw ess");
                    return -1;
                }
                break;

            case CLOSING_BANK:
                Bank.close();
                break;

            case IN_ANIMATION:
                Logger.info("waiting for animation");
                if(!sleepUntil(()-> !lp.isAnimating(), ab.getLongDelay(), 100)){
                    Logger.warn("animation wait timed out");
                }
                n.resetLastMove();
                break;

            case USING_SHRINE:
                if(!propPool.interactWith("Drink", areaChapel, lp)){
                    Logger.warn("failed to use pool");
                }
                ch.yawEast();
                break;

            case GOING_TO_CHAPEL:
                Inventory.open();
                ch.yawWest();
                if(!n.moveIfTime(areaChapel, lp)){
                    Logger.warn("failed to run to chapel");
                }
                break;

            case USING_ENCLAVE_BANK:
                Inventory.open();
                if(!propBankChest.interactWith("Use", areaChapel, lp)){
                    Logger.warn("failed to use chapel bank");
                }
                break;

            case TELEPORTING_TO_EDGEVILLE:
                if(Bank.isOpen()){
                    Bank.close();
                    ab.idleShort();
                }
                if(!Equipment.interact(EquipmentSlot.AMULET, "Edgeville")){
                    Logger.error("failed to teleport to edgeville");
                    return -1;
                }else{
                    ab.idle(2600, 3000);
                    Inventory.open();
                }
                break;

            case USING_EDGEVILLE_BANK:
                Inventory.open();
                if(!propBankBooth.interactWith("Bank", areaBankEdgeville, lp)){
                    Logger.warn("failed to use edgeville bank");
                }
                break;

            case GOING_TO_EDGEVILLE:
                if(!n.moveIfTime(areaBankEdgeville, lp)){
                    Logger.warn("failed to run to edgeville");
                }
                break;

        }

        return ab.getTick();
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

        if (n.shouldToggleRun()) {
            State.setState(State.SPRINTING);
            return;
        }

        //LOGIC
        //STEP 0 - NATURE ALTAR
        boolean atAltar = areaNatureRealm.playerIn(lp);
        int essCount = itemPureEss.inInventoy();

        if(atAltar && essCount>0){
            State.setState((State.USING_NATURE_ALTAR));
            return;
        }

        //STEP 1 - INNER RING BUTTON
        boolean atButton = areaNatureButton.playerIn(lp);

        if(atButton){
            State.setState(State.ACTIVATING_DOOR);
            return;
        }

        //STEP 2 - INNER RING WALK
        boolean atInnerCircle = areaInnerRing.playerIn(lp);

        if(atInnerCircle){
            State.setState(State.GOING_TO_BUTTON);
            return;
        }

        //STEP 3 - OUTER RING
        boolean atOuterRing = areaOuterRing.playerIn(lp);

        if(atOuterRing){
            State.setState(State.NAVIGATING_ABYSS);
            return;
        }

        //STEP 4 - TELEPORTING TO ABYSS
        boolean atMage = areaMageTeleBig.playerIn(lp);

        if(atMage && essCount>0){
            State.setState(State.TELEPORTING_TO_ABYSS);
            return;
        }

        boolean bankOpen = Bank.isOpen();
        boolean wearingOutfit = InteractableItem.inEquipment(itemOutfit);

        //STEP 5 - CHECKING OUTFIT
        if(bankOpen && !wearingOutfit){
            State.setState(State.EQUIPPING_OUTFIT);
            return;
        }

        //STEP 6 - CHECKING ESSENCE
        if(bankOpen && 20>essCount){
            State.setState(State.WITHDRAWING_ESS);
            return;
        }

        if(bankOpen){
            State.setState(State.CLOSING_BANK);
            return;
        }

        int animation = lp.getAnimation();

        //STEP 7 - WAITING FOR ANIMATIONS
        if(animation == 7305 || animation == 6132){ //pool of refreshment or jumping trench
            State.setState(State.IN_ANIMATION);
            return;
        }

        int health = lp.getHealthPercent();
        int stamina = Walking.getRunEnergy();
        boolean atChapel = areaChapel.playerIn(lp);

        //STEP 8 - USING SHRINE
        if((100>health || 95>stamina) && atChapel) {
            State.setState(State.USING_SHRINE);
            return;
        }

        boolean wearingGlory = itemOutfit[1].inEquipment();
        boolean atEnclave = areaEnclave.playerIn(lp);
        boolean needChapel = (!wearingGlory || 100>health || 95>stamina);

        //STEP 9 - GOING TO CHAPEL
        if((atAltar && needChapel) || (atEnclave && !atChapel)) {
            State.setState(State.GOING_TO_CHAPEL);
            return;
        }

        //STEP 10 - USING ENCLAVE BANK
        if(atChapel && (!wearingGlory)) {
            State.setState(State.USING_ENCLAVE_BANK);
            return;
        }

        //STEP 11 - TELEPORTING TO START
        if(atEnclave || atAltar) {
            State.setState(State.TELEPORTING_TO_EDGEVILLE);
            return;
        }

        //STEP 12 - GOING TO MAGE
        if(essCount>=20 && wearingOutfit){
            State.setState(State.GOING_TO_MAGE);
            return;
        }

        boolean atEdgevilleBank = areaBankEdgeville.playerIn(lp);

        //STEP 13 - USING BANK
        if(atEdgevilleBank){
            State.setState(State.USING_EDGEVILLE_BANK);
            return;

        //STEP 14 - GOING TO EDGEVILLE
        }else{
            State.setState(State.GOING_TO_EDGEVILLE);
            return;
        }

        //Logger.debug("no state to set");
        //State.setState(State.NO_ACTION);
    }
}

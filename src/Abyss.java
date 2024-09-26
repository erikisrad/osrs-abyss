import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.time.Duration;
import java.awt.*;

@ScriptManifest(name = "Abyss", description = "x", author = "erik",
        version = 1.0, category = Category.RUNECRAFTING, image = "")

public class Abyss extends AbstractScript implements ChatListener {

    private AntiBan ab;
    private CameraHandler ch;
    private Navigator n;
    private Navigator navAbyss;
    private Player lp;
    private FileLogger fl;

    final private int RETRIES = 20;

    //stats
    private int tripCounter = 0;
    private int runesMadeSession = 0;
    private int essUsedSession = 0;
    private int runesMadeEarlier = 0;
    private int essUsedEarlier = 0;
    private long startTime;
    private long lastCraft;
    private long secondLastCraft; // second to last craft
    private int price;

    final private int XP_PER = 9;
    final private int XP_FOR_99 = 13034431;

    //areas
    DefinedArea areaMageTeleSmall = new DefinedArea("mageTeleSmall", 3102, 3561, 3110, 3556);
    DefinedArea areaEnclave = new DefinedArea("enclave", 3155, 3646, 3123, 3617);
    DefinedArea areaChapel = new DefinedArea("chapel", 3130, 3633, 3125, 3636);
    DefinedArea areaEnclaveBank = new DefinedArea("enclaveBank", 3127, 3632, 3133, 3629);
    DefinedArea areaOuterRing = new DefinedArea("outerRing", 3011, 4860, 3069, 4804);
    DefinedArea areaInnerRing = new DefinedArea("innerRing", 3024, 4846, 3054, 4817);
    DefinedArea areaNatureButton = new DefinedArea("natureButton", 3032, 4842, 3037, 4844);
    DefinedArea areaNatureRealm = new DefinedArea("natureRealm", 2390, 4851, 2409, 4832);
    DefinedArea areaBankEdgeville = new DefinedArea("bankEdgeville", 3083, 3503, 3104, 3483);
    DefinedArea areaRepair = new DefinedArea("pouchRepair", 3035, 4835, 3043, 4828);

    //items
    InteractableItem itemPureEss = new InteractableItem("Pure essence", 7936);
    InteractableItem itemNatRune = new InteractableItem("Nature rune", 561);

    InteractableItem[] itemPouches = {
            new InteractableItem("Small pouch", 5509),
            new InteractableItem("Medium pouch", 5510),
            new InteractableItem("Large pouch", 5512)
    };

    InteractableItem[] itemDecayed = {
            new InteractableItem("Medium pouch", 5511),
            new InteractableItem("Large pouch", 5513),
            new InteractableItem("Giant pouch", 5515)
    };

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
    InteractableProp propNatButton = new InteractableProp("Nature rift", 24975, 6200,
            true, false);
    InteractableProp propPool = new InteractableProp("Pool of refreshment", 39651);
    InteractableProp propBankChest = new InteractableProp("Bank chest", 26711);
    InteractableProp propBankBooth = new InteractableProp("Bank booth", 10355);

    //npcs
    InteractableNPC npcMage = new InteractableNPC("Mage of Zamorak", 3228, 6200,
            true, false);
    InteractableNPC npcDarkMage = new InteractableNPC("Dark Mage", 2583);

    @Override
    public void onStart() {
        Logger.log("booting script...");
        Logger.info("using Java: " + System.getProperty("java.version"));

        //grabbing local player object
        int tries = 0;
        while(lp == null){
            if(tries> RETRIES){
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

        //initialize stat tracking utilities
        SkillTracker.start(Skill.RUNECRAFTING);
        this.secondLastCraft = this.lastCraft = this.startTime = System.currentTimeMillis();
        try {
            this.runesMadeEarlier = fl.readInStat("crafted");
            this.essUsedEarlier = fl.readInStat("essence");
            Logger.info("read in log file");
        }catch(Exception err){
            Logger.warn("couldn't read in log file");
            this.runesMadeEarlier = 0;
            this.essUsedEarlier = 0;
        }
        this.price = itemNatRune.getPrice();

        //create needed objects
        ab = new AntiBan(this, 300, lp);
        ch = new CameraHandler(220, 340, 300, 383);
        n = new Navigator();
        navAbyss = new Navigator(0, 1200, 2200);
        fl = new FileLogger();

        fl.writeLog("session start");
    }

    @Override
    public void onExit(){
        fl.writeLog("session end");
        Logger.info("script ended gracefully");
    }

    @Override
    public void onMessage(Message message) {
        final String rockFail = "...but fail to break-up the rock.";
        final String rockSucceed = "..and manage to break through the rock.";
        final String gapFail = "...but you are not agile enough to get through the gap.";
        final String gapSucceed = "...and you manage to crawl through.";

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

        //get stat info
        int vertical = 20;
        int gap = 12;
        long currentTime = System.currentTimeMillis();
        long runTime = (currentTime - this.startTime);
        long lastLap = (this.lastCraft - this.secondLastCraft);
        long averageLap;
        int avgEss;
        int totalRunes = this.runesMadeEarlier + this.runesMadeSession;
        int totalEss = this.essUsedEarlier + this.essUsedSession;
        int profit = this.price * totalRunes;
        int experience = XP_PER * totalEss;
        int lapsUntil99;
        long timeUntil99;
        int currentXP = Skill.RUNECRAFTING.getExperience();

        try {
            averageLap = (this.lastCraft - this.startTime) / this.tripCounter;
            avgEss = this.essUsedSession / this.tripCounter;
            lapsUntil99 = (XP_FOR_99 - currentXP) / (avgEss*XP_PER);
            timeUntil99 = lapsUntil99 * averageLap;
        } catch(ArithmeticException err){
            averageLap = 0;
            lapsUntil99 = 0;
            timeUntil99 = 0;
        }

        //print stat info on screen
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        g.drawString("State: " + State.getState(), 10, vertical+=gap);
        vertical+=gap;

        g.drawString("Runtime: " + getReadableRuntime(runTime), 10, vertical+=gap);
        g.drawString("Runes made: " + this.runesMadeSession, 10, vertical+=gap);
        g.drawString("Lap count: " + this.tripCounter, 10, vertical+=gap);
        //g.drawString("Last lap: " + getReadableRuntime(lastLap), 10, vertical+=gap);
        g.drawString("Avg lap: " + getReadableRuntime(averageLap), 10, vertical+=gap);
        vertical+=gap;

        //g.drawString("Ess Used today: " + totalEss, 10, vertical+=gap);
        g.drawString("Runes today: " + totalRunes, 10, vertical+=gap);
        g.drawString("Profit today: " + String.format("%,d", profit) + "gp", 10, vertical+=gap);
        g.drawString("XP/hour: " + SkillTracker.getGainedExperiencePerHour(Skill.RUNECRAFTING), 10, vertical+=gap);
        //g.drawString("XP today: " + String.format("%,d", experience) + "xp", 10, vertical+=gap);
        //vertical+=gap;

        //g.drawString("Laps til 99: " + lapsUntil99, 10, vertical+=gap);
        g.drawString("Time til 99: " + getReadableRuntime(timeUntil99), 10, vertical+=gap);
    }

    @Override
    public int onLoop() {

        //test
        Logger.log(ab.doAntiBan());
        ab.idle(9999999);

        ab.checkAntiBan();
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
                int tempEss = 0;
                int essCount = 0;
                int runeCount = 0;

                for (InteractableItem pouch : itemPouches) {
                    Logger.info("processing " + pouch.getName());
                    boolean processing = true;
                    while(processing) {

                        //if full of ess, craft runes
                        if (Inventory.isFull() && itemPureEss.inInventory()) {
                            tempEss = itemPureEss.inInventoy();
                            if (propNatAltar.interactWith(lp)) {
                                Logger.info("crafting runes");
                                if (sleepUntil(() -> !itemPureEss.inInventory(), 4000, 100)) {
                                    essCount += tempEss;
                                }
                            } else {
                                Logger.error("failed to craft runes");
                                continue;
                            }
                        }

                        //if we have room in inventory, empty the pouch
                        if (!Inventory.isFull() && pouch.interact("Empty")) {
                            Logger.info("emptied " + pouch.getName());
                            ab.idleShort();
                            InteractableProp.resetLastInteract();
                            if(!Inventory.isFull()) processing = false;

                            //if we couldn't empty pouch
                        } else if (pouch.inInventory()) {
                            Logger.error("failed to empty " + pouch.getName());

                            //if we don't have that pouch
                        } else {
                            Logger.warn(pouch.getName() + " not in inventory");
                        }
                    }
                }

                //after emptying every pouch - do one last craft if needed.
                if (itemPureEss.inInventory()) {
                    tempEss = itemPureEss.inInventoy();
                    if (propNatAltar.interactWith(lp)) {
                        Logger.info("crafting last runes");
                        if (sleepUntil(() -> !itemPureEss.inInventory(), 4000, 100)) {
                            essCount += tempEss;
                        }
                    } else {
                        Logger.error("failed to craft last runes");
                    }
                }

                runeCount = itemNatRune.inInventoy();
                lapComplete(runeCount, essCount);
                break;

            case REPAIRING_POUCH:
                if (!npcDarkMage.moveToInteract("Repairs", areaRepair, lp, n)) {
                    Logger.warn("failed to walk to repair");
                }
                break;

            case ACTIVATING_BUTTON:
                ch.yawSouth();
                if (propNatButton.moveToInteract(lp, areaNatureButton, n)) {
                    Logger.debug("using button");
                } else {
                    Logger.error("failed to use button");
                }
                break;

            case NAVIGATING_ABYSS:
                InteractableProp.searchAndMove(abyssProps, lp, navAbyss);
                break;

            case TELEPORTING_TO_ABYSS:
                ch.yawNorth();
                if (npcMage.moveToInteract("Teleport", areaMageTeleSmall, lp, n)) {
                    Logger.debug("teleporting");
                } else {
                    Logger.error("failed to teleport to abyss");
                }
                break;

            case EQUIPPING_OUTFIT:
                Inventory.open();
                if (14 > Inventory.emptySlotCount()) {
                    Bank.depositAllItems();
                    ab.idleShort();
                }

                for (InteractableItem item : itemOutfit) {
                    if (!item.inEquipment()) {

                        if(!item.inInventory()) {

                            if (!item.withdraw()) {
                                Logger.error("failed to withdraw " + item.getName());
                                return -1; //exit script
                            }
                            ab.idleShort();
                        }

                        if (!item.equipItem()) {
                            Logger.error("failed to equip " + item.getName());
                            return -1; //exit script
                        } else {
                            Logger.info(item.getName() + " equipped");
                            ab.idleShort();
                        }

                    } else {
                        Logger.debug(item.getName() + " already worn");
                    }
                }
                ab.idleShort();
                break;

            case WITHDRAWING_ESS:
                Inventory.open();
                InteractableItem.depositAllExcept(itemPouches);

                //withdraw pouches
                for (InteractableItem pouch : itemPouches) {
                    boolean processing = true;
                    while(processing) {

                        //try and make sure we have a pouch
                        if (!pouch.inInventory()) {
                            Logger.warn("we don't have a " + pouch.getName());
                            if (pouch.withdraw()) {
                                ab.idleShort();
                            } else {
                                Logger.error("failed to withdraw " + pouch.getName());
                            }
                        }

                        //get more ess if empty
                        if (!itemPureEss.inInventory()) {
                            if (itemPureEss.withdraw(28)) {
                                Logger.info("withdrew ess");
                                ab.idleShort();
                            } else {
                                Logger.error("failed to withdraw ess");
                                return -1;
                            }
                        }

                        //fill pouch
                        if (pouch.interact("Fill")) {
                            Logger.info("filled " + pouch.getName());
                            ab.idleShort();
                            //if we had ess left over, pouch must be full
                            if(itemPureEss.inInventory()){
                                processing = false;
                            }else{
                                Logger.info("pouch might not be full");
                            }

                        } else {
                            Logger.warn("failed to fill " + pouch.getName());
                            processing = false;
                        }
                    }
                }

                //get full inventory of ess before we leave
                if(!Inventory.isFull()) {
                    if (itemPureEss.withdraw(28)) {
                        Logger.info("finally withdrew ess");
                        ab.idleShort();
                    } else {
                        Logger.error("failed to finally withdraw ess");
                        return -1;
                    }
                }
                break;

            case CLOSING_BANK:
                Bank.close();
                break;

            case IN_ANIMATION:
                Logger.info("waiting for animation");
                if (!sleepUntil(() -> !lp.isAnimating(), ab.getLongDelay(), 100)) {
                    Logger.warn("animation wait timed out");
                }
                n.resetLastMove();
                break;

            case USING_SHRINE:
                ch.yawWest();
                if (propPool.moveToInteract(lp, "Drink", areaChapel, n)){
                    Logger.debug("drinking from pool");
                }else{
                    Logger.warn("failed to use pool");
                }
                break;

            case USING_ENCLAVE_BANK:
                Inventory.open();
                if(propBankChest.moveToInteract(lp, "Use", areaEnclaveBank, n)) {
                    Logger.info("banking at enclave");
                }else{
                    Logger.warn("failed to use enclave bank");
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
                ch.yawEast();
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

        boolean atInnerCircle = areaInnerRing.playerIn(lp);
        boolean hasDecayed = InteractableItem.anyInInventory(itemDecayed);

        if(atInnerCircle && hasDecayed){
            State.setState(State.REPAIRING_POUCH);
            return;
        }

        if(atInnerCircle){
            State.setState(State.ACTIVATING_BUTTON);
            return;
        }

        //STEP 3 - OUTER RING
        boolean atOuterRing = areaOuterRing.playerIn(lp);

        if(atOuterRing){
            State.setState(State.NAVIGATING_ABYSS);
            return;
        }

        boolean bankOpen = Bank.isOpen();
        boolean wearingOutfit = InteractableItem.allInEquipment(itemOutfit);

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
        boolean needChapel = (100>health || 95>stamina);
        boolean atEnclave = areaEnclave.playerIn(lp);

        //STEP 9 - GOING TO CHAPEL
        if((atAltar || atEnclave) && needChapel) {
            State.setState(State.USING_SHRINE);
            return;
        }

        boolean wearingGlory = itemOutfit[1].inEquipment();

        //STEP 10 - USING ENCLAVE BANK
        if(atEnclave && (!wearingGlory)) {
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
            State.setState(State.TELEPORTING_TO_ABYSS);
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

    private void lapComplete(int runeCount, int essCount){
        Logger.info("crafted " + runeCount + " runes using " + essCount + " essence");
        fl.writeLog("essence used " + essCount);
        fl.writeLog("crafted " + runeCount);
        this.secondLastCraft = this.lastCraft;
        this.lastCraft = System.currentTimeMillis();
        long lastLap = (this.lastCraft - this.secondLastCraft);
        Logger.info("lap time " + getReadableRuntime(lastLap));
        this.runesMadeSession += runeCount;
        this.essUsedSession += essCount;
        this.tripCounter += 1;
    }

    private String getReadableRuntime(long ms){
        Duration duration = Duration.ofMillis(ms);
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .replaceAll("\\.\\d+", "")
                .toLowerCase();
    }
}

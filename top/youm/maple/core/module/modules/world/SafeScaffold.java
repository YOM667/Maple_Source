package top.youm.maple.core.module.modules.world;

import com.darkmagician6.eventapi.EventTarget;
import com.darkmagician6.eventapi.events.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import top.youm.maple.common.events.*;
import top.youm.maple.common.settings.impl.CheckBoxSetting;
import top.youm.maple.common.settings.impl.SelectButtonSetting;
import top.youm.maple.common.settings.impl.SliderSetting;
import top.youm.maple.core.module.Module;
import top.youm.maple.core.module.ModuleCategory;
import top.youm.maple.utils.TimerUtil;
import top.youm.maple.utils.math.MathUtil;
import top.youm.maple.utils.network.PacketUtil;
import top.youm.maple.utils.player.MovementUtil;
import top.youm.maple.utils.player.RotationUtil;
import top.youm.maple.utils.player.ScaffoldUtil;

public class SafeScaffold extends Module {

    private final CheckBoxSetting rotations = new CheckBoxSetting("Rotations", true);
    private final SelectButtonSetting rotationMode = new SelectButtonSetting("Rotation Mode", "Watchdog", "Watchdog", "NCP", "Back", "45", "Enum", "Down", "0","Vulcan");
    private final SelectButtonSetting placeType = new SelectButtonSetting("Place Type", "Post", "Pre", "Post", "Legit", "Dynamic","Telly");
    public static SelectButtonSetting keepYMode = new SelectButtonSetting("Keep Y Mode", "Always", "Always", "Speed toggled");
    public static SelectButtonSetting sprintMode = new SelectButtonSetting("Sprint Mode", "Vanilla", "Vanilla", "Watchdog", "Cancel");
    public static SelectButtonSetting towerMode = new SelectButtonSetting("Tower Mode", "Watchdog", "Vanilla", "NCP", "Watchdog", "Verus");
    public static SelectButtonSetting swingMode = new SelectButtonSetting("Swing Mode", "Client", "Client", "Silent");
    public static SliderSetting delay = new SliderSetting("Delay", 0, 2, 0, 0.05);
    private final SliderSetting timer = new SliderSetting("Timer", 1, 5, 0.1, 0.1);
    public static final CheckBoxSetting auto3rdPerson = new CheckBoxSetting("Auto 3rd Person", false);
    public static final CheckBoxSetting speedSlowdown = new CheckBoxSetting("Speed Slowdown", true);
    public static final SliderSetting speedSlowdownAmount = new SliderSetting("Slowdown Amount", 0.1, 0.2, 0.01, 0.01);
    public static final CheckBoxSetting itemSpoof = new CheckBoxSetting("Item Spoof", false);
    public static final CheckBoxSetting downwards = new CheckBoxSetting("Downwards", false);
    public static final CheckBoxSetting safewalk = new CheckBoxSetting("Safewalk", false);
    public static final CheckBoxSetting sprint = new CheckBoxSetting("Sprint", false);
    private final CheckBoxSetting sneak = new CheckBoxSetting("Sneak", false);
    public static final CheckBoxSetting tower = new CheckBoxSetting("Tower", false);
    private final SliderSetting towerTimer = new SliderSetting("Tower Timer Boost", 1.2, 5, 0.1, 0.1);
    private final CheckBoxSetting swing = new CheckBoxSetting("Swing", true);
    private final CheckBoxSetting autoJump = new CheckBoxSetting("Auto Jump", false);
    private final CheckBoxSetting hideJump = new CheckBoxSetting("Hide Jump", false);
    private final CheckBoxSetting baseSpeed = new CheckBoxSetting("Base Speed", false);
    public static CheckBoxSetting keepY = new CheckBoxSetting("Keep Y", false);
    private ScaffoldUtil.BlockCache blockCache, lastBlockCache;
    private float y;
    private float speed;
    private final TimerUtil delayTimer = new TimerUtil();
    private final TimerUtil timerUtil = new TimerUtil();
    public static double keepYCoord;
    private boolean pre;
    private int slot;
    private int prevSlot;
    private float[] cachedRots = new float[2];
    public static boolean noSprint;

    public SafeScaffold() {
        super("OldScaffold", ModuleCategory.WORLD, Keyboard.KEY_NONE);
        this.addSettings( rotations, rotationMode, placeType, keepYMode, sprintMode, towerMode, swingMode, delay, timer,
                auto3rdPerson, speedSlowdown, speedSlowdownAmount, itemSpoof, downwards, safewalk, sprint, sneak, tower, towerTimer,
                swing, autoJump, hideJump, baseSpeed, keepY);
        rotationMode.addParent(rotations, CheckBoxSetting::getValue);
        sprintMode.addParent(sprint, CheckBoxSetting::getValue);
        towerMode.addParent(tower, CheckBoxSetting::getValue);
        swingMode.addParent(swing, CheckBoxSetting::getValue);
        towerTimer.addParent(tower, CheckBoxSetting::getValue);
        keepYMode.addParent(keepY, CheckBoxSetting::getValue);
        hideJump.addParent(autoJump, CheckBoxSetting::getValue);
        speedSlowdownAmount.addParent(speedSlowdown, CheckBoxSetting::getValue);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent e) {
        // Timer Stuff
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.timer.timerSpeed = timer.getValue().floatValue();
        } else {
            mc.timer.timerSpeed = tower.getValue() ? towerTimer.getValue().floatValue() : 1;
        }

        if (e.getState() == Event.State.PRE) {
            // Auto Jump
            if (baseSpeed.getValue()) {
                MovementUtil.setSpeed(MovementUtil.getBaseMoveSpeed() * 0.7);
            }
            if (autoJump.getValue() && mc.thePlayer.onGround && MovementUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.jump();
            }

            if (sprint.getValue() && sprintMode.getValue().equals("Watchdog") && mc.thePlayer.onGround && MovementUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown() && !isDownwards() && mc.thePlayer.isSprinting()) {
                final double[] offset = MathUtil.yawPos(mc.thePlayer.getDirection(), MovementUtil.getSpeed() / 2);
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX - offset[0], mc.thePlayer.posY, mc.thePlayer.posZ - offset[1], true));
            }

            // Rotations
            if (rotations.getValue()) {
                float[] rotations = new float[]{0, 0};
                switch (rotationMode.getValue()) {
                    case "Watchdog":
                        rotations = new float[]{MovementUtil.getMoveYaw(e.getYaw()) - 180, y};
                        e.setRotations(rotations[0], rotations[1]);
                        break;
                    case "NCP":
                        float prevYaw = cachedRots[0];
                        if ((blockCache = ScaffoldUtil.getBlockInfo()) == null) {
                            blockCache = lastBlockCache;
                        }
                        if (blockCache != null && (mc.thePlayer.ticksExisted % 3 == 0
                                || mc.theWorld.getBlockState(new BlockPos(e.getPosX(), ScaffoldUtil.getYLevel(), e.getPosZ())).getBlock() == Blocks.air)) {
                            cachedRots = RotationUtil.getRotations(blockCache.getPosition(), blockCache.getFacing());
                        }
                        if ((mc.thePlayer.onGround || (MovementUtil.isMoving() && tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown())) && Math.abs(cachedRots[0] - prevYaw) >= 90) {
                            cachedRots[0] = MovementUtil.getMoveYaw(e.getYaw()) - 180;
                        }
                        rotations = cachedRots;
                        if(placeType.getValue().equals("Telly")){
                            new Thread(()->{
                                while(!canPlace && !mc.thePlayer.onGround){
                                    try {
                                        Thread.sleep(200L);
                                        canPlace = true;
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }).start();
                            if(canPlace){
                                e.setRotations(rotations[0], rotations[1]);
                            }
                        }else {
                            e.setRotations(rotations[0], rotations[1]);
                        }

                        break;
                    case "Back":
                        rotations = new float[]{MovementUtil.getMoveYaw(e.getYaw()) - 180, 77};
                        e.setRotations(rotations[0], rotations[1]);
                        break;
                    case "Down":
                        e.setPitch(90);
                        break;
                    case "45":

                        float val;
                        if (MovementUtil.isMoving()) {
                            float f = MovementUtil.getMoveYaw(e.getYaw()) - 180;
                            float[] numbers = new float[]{-135, -90, -45, 0, 45, 90, 135, 180};
                            float lastDiff = 999;
                            val = f;
                            for (float v : numbers) {
                                float diff = Math.abs(v - f);
                                if (diff < lastDiff) {
                                    lastDiff = diff;
                                    val = v;
                                }
                            }
                        } else {
                            val = rotations[0];
                        }
                        if(placeType.getValue().equals("Telly")){
                            new Thread(()->{
                                while(!canPlace && !mc.thePlayer.onGround){
                                    try {
                                        Thread.sleep(200L);
                                        canPlace = true;
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }).start();
                            if(canPlace){
                                rotations = new float[]{
                                        (val + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationYawHead)) / 2.0F,
                                        (77 + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationPitchHead)) / 2.0F};
                                e.setRotations(rotations[0], rotations[1]);
                            }
                        }else {
                            rotations = new float[]{
                                    (val + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationYawHead)) / 2.0F,
                                    (77 + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationPitchHead)) / 2.0F};
                            e.setRotations(rotations[0], rotations[1]);
                        }

                        break;
                    case "Enum":
                        if (lastBlockCache != null) {
                            float yaw = RotationUtil.getEnumRotations(lastBlockCache.getFacing());

                            e.setRotations(yaw, 77);
                        } else {
                            e.setRotations(mc.thePlayer.rotationYaw + 180, 77);
                        }
                        break;
                    case "0":
                        e.setRotations(0, 0);
                        break;
                    default:
                        if(Minecraft.getMinecraft().thePlayer.isBlockEdge){
                            float v1;
                            if (MovementUtil.isMoving()) {
                                float f = MovementUtil.getMoveYaw(e.getYaw()) - 180;
                                float[] numbers = new float[]{-135, -90, -45, 0, 45, 90, 135, 180};
                                float lastDiff = 999;
                                v1 = f;
                                for (float v : numbers) {
                                    float diff = Math.abs(v - f);
                                    if (diff < lastDiff) {
                                        lastDiff = diff;
                                        v1 = v;
                                    }
                                }
                            } else {
                                v1 = rotations[0];
                            }
                            rotations = new float[]{
                                    (v1 + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationYawHead)) / 2.0F,
                                    (77 + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationPitchHead)) / 2.0F};
                            e.setRotations(rotations[0], rotations[1]);
                        }
                        break;
                }
                RotationUtil.setRotations(e.getYaw(),e.getPitch());
            }

            // Speed 2 Slowdown
            if (speedSlowdown.getValue() && mc.thePlayer.isPotionActive(Potion.moveSpeed) && !mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround) {
                MovementUtil.setSpeed(speedSlowdownAmount.getValue().doubleValue());
            }

            if (sneak.getValue()){
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), mc.thePlayer.isBlockEdge);
            }

            // Save ground Y level for keep Y
            if (mc.thePlayer.onGround) {
                keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
            }

            if (tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) {
                double centerX = Math.floor(e.getPosX()) + 0.5, centerZ = Math.floor(e.getPosZ()) + 0.5;
                switch (towerMode.getValue()) {
                    case "Vanilla":
                        mc.thePlayer.motionY = 0.42f;
                        break;
                    case "Verus":
                        if (mc.thePlayer.ticksExisted % 2 == 0)
                            mc.thePlayer.motionY = 0.42f;
                        break;
                    case "Watchdog":
                        if (!MovementUtil.isMoving() && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() != Blocks.air && lastBlockCache != null) {
                            if (mc.thePlayer.ticksExisted % 6 == 0) {
                                e.setPosX(centerX + 0.1);
                                e.setPosZ(centerZ + 0.1);
                            } else {
                                e.setPosX(centerX - 0.1);
                                e.setPosZ(centerZ - 0.1);
                            }
                            MovementUtil.setSpeed(0);
                        }

                        mc.thePlayer.motionY = 0.3;
                        e.setOnGround(true);
                        break;
                    case "NCP":
                        if (!MovementUtil.isMoving() || MovementUtil.getSpeed() < 0.16) {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.motionY = 0.42;
                            } else if (mc.thePlayer.motionY < 0.23) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX, (int) mc.thePlayer.posY, mc.thePlayer.posZ);
                                mc.thePlayer.motionY = 0.42;
                            }
                        }
                        break;
                }
            }

            // Setting Block Cache
            blockCache = ScaffoldUtil.getBlockInfo();
            if (blockCache != null) {
                lastBlockCache = ScaffoldUtil.getBlockInfo();
            } else {
                return;
            }

            if (mc.thePlayer.ticksExisted % 4 == 0) {
                pre = true;
            }

            // Placing Blocks (Pre)
            if (placeType.getValue().equals("Pre") || (placeType.getValue().equals("Dynamic") && pre)) {
                if (place()) {
                    pre = false;
                }
            }
        } else {
            // Setting Item Slot
            if (!itemSpoof.getValue()) {
                mc.thePlayer.inventory.currentItem = slot;
            }

            // Placing Blocks (Post)
            if (placeType.getValue().equals("Post") || (placeType.getValue().equals("Dynamic") && !pre)) {
                place();
            }

            pre = false;
        }
    }

    private boolean place() {
        int slot = ScaffoldUtil.getBlockSlot();
        if (blockCache == null || lastBlockCache == null || slot == -1) return false;

        if (this.slot != slot) {
            this.slot = slot;
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(this.slot));
        }

        boolean placed = false;
        if (delayTimer.hasTimeElapsed(delay.getValue().longValue() * 1000)) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
                    mc.thePlayer.inventory.getStackInSlot(this.slot),
                    lastBlockCache.getPosition(), lastBlockCache.getFacing(),
                    ScaffoldUtil.getHypixelVec3(lastBlockCache))) {
                placed = true;
                y = MathUtil.getRandomInRange(79.5f, 83.5f);
                if (swing.getValue()) {
                    if (swingMode.getValue().equals("Client")) {
                        mc.thePlayer.swingItem();
                    } else {
                        PacketUtil.sendPacket(new C0APacketAnimation());
                    }
                }
            }
            delayTimer.reset();
            blockCache = null;
        }
        return placed;
    }
    private boolean canPlace = false;
    @EventTarget
    public void onBlockPlaceable(BlockPlaceableEvent event) {
        if(placeType.getValue().equals("Telly")){
            if(canPlace) {
                if(!mc.thePlayer.onGround){
                    place();
                }else{
                    canPlace = false;
                }
            }
        } else if (placeType.getValue().equals("Legit")){
            place();
        }

    }

    @EventTarget
    public void onTickEvent(TickEvent event) {
        if (mc.thePlayer == null) return;
        this.setSuffixes(this.placeType.getValue());
        if (hideJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && MovementUtil.isMoving() && !mc.thePlayer.onGround && autoJump.getValue()) {
            mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
            mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
            mc.thePlayer.cameraYaw = mc.thePlayer.cameraPitch = 0.1F;
        }
        if (downwards.getValue()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            mc.thePlayer.movementInput.sneak = false;
        }
        if(sprint.getValue() && sprintMode.getValue().equals("Cancel")){
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            noSprint = true;
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            if (!itemSpoof.getValue()) mc.thePlayer.inventory.currentItem = prevSlot;
            if (slot != mc.thePlayer.inventory.currentItem && itemSpoof.getValue())
                PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

            if (auto3rdPerson.getValue()) {
                mc.gameSettings.thirdPersonView = 0;
            }
            if (mc.thePlayer.isSneaking() && sneak.getValue())
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        }
        mc.timer.timerSpeed = 1;
        noSprint = false;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        lastBlockCache = null;
        if (mc.thePlayer != null) {
            prevSlot = mc.thePlayer.inventory.currentItem;
            slot = mc.thePlayer.inventory.currentItem;
            if (mc.thePlayer.isSprinting() && sprint.getValue() && sprintMode.getValue().equals("Cancel")) {
                PacketUtil.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            if (auto3rdPerson.getValue()) {
                mc.gameSettings.thirdPersonView = 1;
            }
        }
        speed = 1.1f;
        timerUtil.reset();
        y = 80;
    }

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent e) {
        if (e.getPacket() instanceof C0BPacketEntityAction
                && ((C0BPacketEntityAction) e.getPacket()).getAction() == C0BPacketEntityAction.Action.START_SPRINTING
                && sprint.getValue() && sprintMode.getValue().equals("Cancel")) {
            e.setCancelled(true);
        }
        if (e.getPacket() instanceof C09PacketHeldItemChange && itemSpoof.getValue()) {
            e.setCancelled(true);
        }
    }

    @EventTarget
    public void onSafeWalkEvent(SafeWalkEvent event) {
        if ((safewalk.getValue() && !isDownwards()) || ScaffoldUtil.getBlockCount() == 0) {
            event.setSafe(true);
        }
    }

    public static boolean isDownwards() {
        return downwards.getValue() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak);
    }

}

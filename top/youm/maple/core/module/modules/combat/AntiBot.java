package top.youm.maple.core.module.modules.combat;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import top.youm.maple.common.events.TickEvent;
import top.youm.maple.common.settings.impl.CheckBoxSetting;
import top.youm.maple.common.settings.impl.SelectButtonSetting;
import top.youm.maple.core.module.Module;
import top.youm.maple.core.module.ModuleCategory;
import top.youm.maple.utils.player.Helper;

import java.util.ArrayList;
import java.util.List;
/**
 * @author YouM
 *
 */
public class AntiBot extends Module {
    SelectButtonSetting mode = new SelectButtonSetting("Mode","Advanced","Classic","Advanced","BMC");
    CheckBoxSetting pingCheck = new CheckBoxSetting("Ping Check",true);
    public AntiBot() {
        super("Anti Bot", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
        this.addSettings(mode,pingCheck);
    }
    @EventTarget
    public void onUpdate(TickEvent event){
        this.setSuffixes(mode.getValue());
    }


    public boolean isNPC(EntityLivingBase entity) {
        if(!this.isEnabled()) return false;
        if (entity == null) {
            return true;
        }
        if (entity == mc.thePlayer) {
            return true;
        }
        if (!(entity instanceof EntityPlayer)) {
            return true;
        }
        if (entity.isPlayerSleeping()) {
            return true;
        }
        if (pingCheck.getValue()) {
            if (mc.getNetHandler().getPlayerInfo(entity.getUniqueID()).getResponseTime() == 0) {
                return true;
            }
        }
        if(mode.getValue().equals("BMC")){
            String entityName = entity.getDisplayName().getFormattedText();
            return entityName.contains("SHOP");
        }
        return (mode.getValue().equals("Classic") || mode.getValue().equals("Advanced")) && (entity).ticksExisted <= 80;
    }
    public boolean isServerBot(Entity entity) {
        if(!this.isEnabled()){
            return true;
        }
        if (Helper.onServer("hypixel")) {
            return !entity.getDisplayName().getFormattedText().startsWith("§") || entity.isInvisible() || entity.getDisplayName().getFormattedText().toLowerCase().contains("npc");
        } else if (Helper.onServer("mooncookie")) {
            List<EntityPlayer> newList = new ArrayList<>(getTabPlayerList().size());
            getTabPlayerList().forEach(player -> {
                if (!newList.contains(player)) {
                    newList.add(player);
                }
            });
            for (EntityPlayer player : newList) {
                if(!entity.equals(player)){
                    continue;
                }
                return false;
            }
            return true;
        }
        return false;
    }
    private List<EntityPlayer> getTabPlayerList() {
        ArrayList<EntityPlayer> list = new ArrayList<>();
        List<NetworkPlayerInfo> players = GuiPlayerTabOverlay.field_175252_a.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());
        for (NetworkPlayerInfo info : players) {
            if (info == null) {
                continue;
            }
            list.add(mc.theWorld.getPlayerEntityByName(info.getGameProfile().getName()));
        }
        return list;
    }
}



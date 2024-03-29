package top.youm.maple.core.module.modules.player;

import com.darkmagician6.eventapi.EventTarget;
import top.youm.maple.common.events.MotionEvent;
import top.youm.maple.common.settings.impl.SliderSetting;
import top.youm.maple.core.module.Module;
import top.youm.maple.core.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

/**
 * @author YouM
 */
public final class Timer extends Module {

    private final SliderSetting amount = new SliderSetting("Amount", 1, 10, 0.1, 0.1);
    @EventTarget
    public void onMotion(MotionEvent motionEvent) {
        this.mc.timer.timerSpeed = amount.getValue().floatValue();
    }

    public Timer() {
        super("Timer", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
        this.addSettings(amount);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
    }

}
package top.youm.maple.core.module.modules.visual;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.ScaledResolution;
import top.youm.maple.Maple;
import top.youm.maple.common.events.Render2DEvent;
import top.youm.maple.common.events.TickEvent;
import top.youm.maple.common.settings.impl.BoolSetting;
import top.youm.maple.common.settings.impl.NumberSetting;
import top.youm.maple.core.module.Module;
import top.youm.maple.core.module.ModuleCategory;
import top.youm.maple.core.ui.clickgui.classic.theme.Theme;
import top.youm.maple.core.ui.font.FontLoaders;
import org.lwjgl.input.Keyboard;
import top.youm.maple.core.ui.hud.KeyStrokesUI;
import top.youm.maple.core.ui.hud.ModuleListUI;
import top.youm.maple.core.ui.hud.noti.NotificationManager;

import java.awt.*;

/**
 * @author YouM
 */
public class HUD extends Module {
    public BoolSetting ttf_font = new BoolSetting("ttf-font",false);
    public static BoolSetting notification = new BoolSetting("notification",true);
    public static BoolSetting moduleList = new BoolSetting("ModuleList",true);
    public static BoolSetting keyStrokes = new BoolSetting("KeyStrokes",true);
    public static NumberSetting red = new NumberSetting("red",Theme.theme.getRed(),255,0,1);
    public static NumberSetting green = new NumberSetting("green",Theme.theme.getGreen(),255,0,1);
    public static NumberSetting blue = new NumberSetting("blue",Theme.theme.getBlue(),255,0,1);
    private final ModuleListUI moduleListUI = new ModuleListUI();
    private final KeyStrokesUI keyStrokesUI = new KeyStrokesUI();
    public HUD() {
        super("HUD", ModuleCategory.VISUAL, Keyboard.KEY_NONE);
        this.setToggle(true);
        this.addSetting(ttf_font,notification,keyStrokes,red,green,blue);
    }
    @EventTarget
    public void onRender(Render2DEvent event){
        ScaledResolution sr = new ScaledResolution(mc);

        if(this.ttf_font.getValue()){
            FontLoaders.robotoR22.drawStringWithShadow(Maple.getInstance().NAME,5,5, getHUDThemeColor().getRGB());
        }else {
            mc.fontRendererObj.drawStringWithShadow(Maple.getInstance().NAME,5,5, getHUDThemeColor().getRGB());
        }
        NotificationManager.draw(sr.getScaledWidth(),sr.getScaledHeight() - (sr.getScaledHeight() / 10.0f));
        if(moduleList.getValue()){
            moduleListUI.draw();
        }
        if(keyStrokes.getValue()){
            keyStrokesUI.draw();
        }
    }
    public static Color getHUDThemeColor(){
        return new Color(red.getValue().intValue(),green.getValue().intValue(),blue.getValue().intValue());
    }
}

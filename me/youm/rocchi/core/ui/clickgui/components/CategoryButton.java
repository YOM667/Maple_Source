package me.youm.rocchi.core.ui.clickgui.components;

import me.youm.rocchi.core.module.ModuleCategory;
import me.youm.rocchi.core.ui.IComponent;
import me.youm.rocchi.core.ui.Theme;
import me.youm.rocchi.core.ui.clickgui.ClickGuiScreen;
import me.youm.rocchi.core.ui.font.FontLoaders;
import me.youm.rocchi.utils.render.RenderUtil;
import me.youm.rocchi.utils.render.RoundedUtil;

import java.awt.*;

public class CategoryButton implements IComponent {
    public float x,y;
    public int mouseX,mouseY;
    public final ModuleCategory category;

    public CategoryButton(ModuleCategory category) {
        this.category = category;
    }

    @Override
    public void draw(float xPos, float yPos) {
        this.x = xPos;this.y = yPos;
        if(ClickGuiScreen.moduleCategory == this.category){
            RoundedUtil.drawRound(x,y,FontLoaders.comfortaaR22.getStringWidth(category.name()) + 3,13,5, Theme.titleColor);
        }
        FontLoaders.comfortaaR22.drawStringWithShadow(category.name(),x + 2,y + 2,new Color(248,248,248).getRGB());
    }

    @Override
    public void mouse(int mouseX, int mouseY,int mouseButton) {
        this.mouseX = mouseX;this.mouseY = mouseY;
        if(isHover((int) x, (int) y,50,20,this.mouseX,this.mouseY) && mouseButton == 0){
            ClickGuiScreen.moduleCategory = this.category;
        }
    }

}

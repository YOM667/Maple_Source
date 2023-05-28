package top.youm.rocchi.core.ui.clickgui.modern.component;

import org.lwjgl.input.Mouse;
import top.youm.rocchi.core.module.ModuleCategory;
import top.youm.rocchi.core.ui.clickgui.modern.Component;
import top.youm.rocchi.core.ui.clickgui.modern.Screen;
import top.youm.rocchi.core.ui.clickgui.modern.animation.Animation;
import top.youm.rocchi.core.ui.clickgui.modern.animation.Direction;
import top.youm.rocchi.core.ui.clickgui.modern.animation.SmoothStepAnimation;
import top.youm.rocchi.core.ui.clickgui.modern.state.UIState;
import top.youm.rocchi.core.ui.clickgui.modern.theme.Theme;
import top.youm.rocchi.core.ui.clickgui.old.MouseType;
import top.youm.rocchi.core.ui.font.FontLoaders;
import top.youm.rocchi.utils.math.MathUtil;
import top.youm.rocchi.utils.render.RoundedUtil;

import static top.youm.rocchi.core.ui.clickgui.modern.Screen.*;


public class CategoryComponent extends Component {
    public final ModuleCategory category;
    private float scroll;
    private float maxScroll;
    private float minScroll;
    private float rawScroll;
    private Animation scrollAnimation;
    public CategoryComponent(ModuleCategory category) {
        super(category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase());
        this.scrollAnimation = new SmoothStepAnimation(0, 0.0, Direction.BACKWARDS);
        this.category = category;
        this.maxScroll = Float.MAX_VALUE;
        this.minScroll = 0.0f;
        this.width = 80;
        this.height = 20;
    }
    @Override
    public void draw(float xPos, float yPos,int mouseX, int mouseY) {
        this.x = xPos;this.y = yPos;
        this.mouseX = mouseX;this.mouseY = mouseY;
        if(UIState.currentCategory == category){
            RoundedUtil.drawRound(x,y,width,height,2, Theme.theme);
        }else if(componentHover()){
            RoundedUtil.drawRound(x,y,width,height,2, Theme.themeHover);
        }
        FontLoaders.robotoB22.drawCenteredStringWithShadow(name,xPos + width / 2.0f,y + height / 2.0f - FontLoaders.robotoR22.getHeight() / 2.0f,Theme.font.getRGB());

    }
    public void moduleMenu(int mouseX,int mouseY){
        if(!UIState.settingFocused && isHover((Screen.x + Screen.navbarWidth),  (Screen.y + 30),Screen.screenWidth - Screen.navbarWidth - 10,Screen.screenHeight - 30,mouseX,mouseY)){
            this.onScroll(30);
        }

        int yOffset = 0;
        for (ModuleComponent moduleComponent : Screen.getModuleComponents()) {
            if(moduleComponent.getModule().getCategory() != this.category){
                continue;
            }
            moduleComponent.draw(Screen.x + navbarWidth,  (Screen.y + 35 + yOffset + getScroll()),mouseX,mouseY);
            yOffset += 35;
        }
    }
    @Override
    public void mouse(int mouseButton, MouseType mouseType) {
        if(mouseType == MouseType.CLICK) {
            if (componentHover() && mouseButton == 0) {
                UIState.currentCategory = this.category;
            }
        }
    }

    @Override
    public void input(char typedChar, int keyCode) {

    }

    public float getScroll() {
        return (float) MathUtil.roundToHalf(scroll());
    }

    public void onScroll(final int ms) {
        this.scroll = (float)(this.rawScroll - this.scrollAnimation.getOutput());
        this.rawScroll += Mouse.getDWheel() / 4.0f;
        this.rawScroll = Math.max(Math.min(this.minScroll, this.rawScroll), -this.maxScroll);
        this.scrollAnimation = new SmoothStepAnimation(ms, this.rawScroll - this.scroll, Direction.BACKWARDS);
    }
    public float scroll() {
        return this.scroll = (float)(this.rawScroll - this.scrollAnimation.getOutput());
    }
}
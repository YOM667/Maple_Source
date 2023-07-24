package top.youm.rocchi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.embed.swing.JFXPanel;
import top.youm.rocchi.core.command.CommandManager;
import top.youm.rocchi.core.config.ConfigManager;
import top.youm.rocchi.core.module.ModuleManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;
import top.youm.rocchi.core.music.QQMusic;
import top.youm.rocchi.core.music.QQMusicUser;
import top.youm.rocchi.core.ui.clickgui.music.MusicPlayerScreen;
import top.youm.rocchi.utils.math.MathUtil;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static top.youm.rocchi.utils.tools.Catcher.runCatching;

/**
 * @author YouM
 * This mod's main class <br/>
 * include many important field and manager<br/>
 * handle game start and end
 */
@SuppressWarnings("all")
public class Rocchi {
    private static final Rocchi instance = new Rocchi();
    public static Gson gson;
    public static Rocchi getInstance() {
        return instance;
    }
    public static Logger log = LogManager.getLogger();
    public String NAME = "Rocchi";
    public String VERSION = "beta-0.5";
    public String dev = "YouM";
    public String username = "YouM"+ MathUtil.getRandomInRange(1,999);;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private QQMusic music;
    public List<String> songList;
    public MusicPlayerScreen musicPlayerScreen;
    public void initializeToolkit()
    {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel();
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void startGame(){
        initializeToolkit();
/*        musicPlayerScreen = new MusicPlayerScreen();*/
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        moduleManager = new ModuleManager();
        moduleManager.initialize();

        commandManager = new CommandManager();
        commandManager.initialize();

        log.info("developer: " + dev.toString());
        Display.setTitle(NAME + " | " + VERSION);

        configManager = new ConfigManager();

        runCatching(() -> {
            configManager.initialize();
        });


    }
    public void shutDownGame(){
        log.info("Thank you to play Rocchi client. Goodbye");
        configManager.save();
    }
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public List<String> getSongList() {
        return songList;
    }
}
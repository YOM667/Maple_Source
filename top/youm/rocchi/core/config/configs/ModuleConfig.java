package top.youm.rocchi.core.config.configs;

import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import top.youm.rocchi.Rocchi;
import top.youm.rocchi.common.config.ModuleConfiguration;
import top.youm.rocchi.core.config.Config;
import top.youm.rocchi.core.config.ConfigManager;
import top.youm.rocchi.core.module.Module;
import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YouM
 * read 
 */
public class ModuleConfig extends Config {
    public ModuleConfig() {
        super("module.json");
    }

    @Override
    public void loadConfig() {
        try {
            List<ModuleConfiguration> configurations = ConfigManager.gson.fromJson(
                    FileUtils.readFileToString(this.file),
                    new TypeToken<List<ModuleConfiguration>>() {}.getType()
            );
            for (Module module : Rocchi.getInstance().getModuleManager().modules) {
                for (ModuleConfiguration configuration : configurations) {
                    if (configuration.getName().equals(module.getName())) {
                        if (module.isToggle() != configuration.isEnable()) {
                            module.toggled();
                        }else if (!configuration.isEnable() && module.isToggle())
                            module.setToggle(false);
                        module.setKey(Keyboard.getKeyIndex(configuration.getKey()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        List<ModuleConfiguration> configurations = Rocchi.getInstance().getModuleManager().
                modules.stream().
                map(module -> new ModuleConfiguration(module.getName(), module.isToggle(), Keyboard.getKeyName(module.getKey()))).
                collect(Collectors.toList());

        this.context = ConfigManager.gson.toJson(configurations);
    }
}

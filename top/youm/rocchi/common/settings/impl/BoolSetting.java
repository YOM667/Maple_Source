package top.youm.rocchi.common.settings.impl;

import top.youm.rocchi.common.settings.Setting;

/**
 * @author YouM
 * can enable or disable setting like checkbox
 */
public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, boolean value) {
        super(name, value);
    }
}

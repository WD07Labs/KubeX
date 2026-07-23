package com.ourgram.kubex.neoforge;

import net.minecraft.commands.CommandSourceStack;

public final class KubeXReloadBridge {
    public boolean reload(CommandSourceStack source) {
        source.getServer().getCommands().performPrefixedCommand(source, "reload");
        return true;
    }
}

package com.icbf.cannons.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * Legacy cannontest command stub. Command registration removed; keeping a harmless stub
 * so older setups that still reference the class won't fail at compile-time.
 */
public class CannonTestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // intentionally left blank - cannontest removed in favor of POV raytrace targeting
    }
}

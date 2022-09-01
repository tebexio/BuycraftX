package net.buycraft.plugin.fabric.mixin;

import net.buycraft.plugin.fabric.util.access.ServerTimerAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class ServerTimer implements ServerTimerAccess {
    @Unique
    private long ticksUntilSomething;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) { // Fix parameters as needed
        if (--this.ticksUntilSomething <= 0L) {
//            doSomething();
            //System.out.println("Hello, world!");
            // If you want to repeat this, reset ticksUntilSomething here.
        }
    }

    @Override
    public void tebex_setTimer(long ticksUntilSomething) {
        this.ticksUntilSomething = ticksUntilSomething;
    }
}

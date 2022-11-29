package atonaddons.mixins.entity;

import atonaddons.events.PositionUpdateEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {EntityPlayerSP.class}, priority = 800)
public abstract class MixinPlayerSP extends MixinAbstractClientPlayer {

    @Shadow
    public abstract boolean isSneaking();

    @Inject(method = {"onUpdateWalkingPlayer"}, at = {@At("HEAD")})
    private void onPlayerWalkUpdate(CallbackInfo ci) {
        PositionUpdateEvent.Pre pre = new PositionUpdateEvent.Pre(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround, isSprinting(), isSneaking());
        MinecraftForge.EVENT_BUS.post(pre);
    }
}

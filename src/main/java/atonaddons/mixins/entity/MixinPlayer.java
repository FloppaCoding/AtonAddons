package atonaddons.mixins.entity;


import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({EntityPlayer.class})
public abstract class MixinPlayer extends MixinEntityLivingBase {
}

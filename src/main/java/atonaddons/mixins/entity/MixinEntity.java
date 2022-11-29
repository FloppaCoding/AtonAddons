package atonaddons.mixins.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = {Entity.class}, priority = 800)
public abstract class MixinEntity {

    @Shadow
    public float rotationPitch;

    @Shadow
    public boolean onGround;

    @Shadow
    public float rotationYaw;

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public abstract boolean equals(Object paramObject);

    @Shadow
    public abstract boolean isSprinting();

}

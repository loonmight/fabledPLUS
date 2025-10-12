package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CHealBEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;       // entity who applied the modifier (playerSource)
    private final LivingEntity target;       // entity who received the modifier
    private final LivingEntity healed;       // entity who was healed
    private final double blockedAmount;      // healing that was blocked/reduced
    private final String skillId;            // skill that caused the reduction

    public CHealBEvent(LivingEntity caster, LivingEntity target, LivingEntity healed,
                       double blockedAmount, String skillId) {
        this.caster = caster;
        this.target = target;
        this.healed = healed;
        this.blockedAmount = blockedAmount;
        this.skillId = skillId;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public LivingEntity getHealed() {
        return healed;
    }

    public double getBlockedAmount() {
        return blockedAmount;
    }

    public String getSkillId() {
        return skillId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

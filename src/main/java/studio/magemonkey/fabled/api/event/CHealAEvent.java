package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CHealAEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;   // the entity who applied the modifier (playerSource)
    private final LivingEntity target;   // the entity who received the modifier
    private final LivingEntity healed;   // the entity who was healed
    private final double extraAmount;    // extra healing applied
    private final String skillId;        // skill that caused the extra healing

    public CHealAEvent(LivingEntity caster, LivingEntity target, LivingEntity healed,
                       double extraAmount, String skillId) {
        this.caster = caster;
        this.target = target;
        this.healed = healed;
        this.extraAmount = extraAmount;
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

    public double getExtraAmount() {
        return extraAmount;
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

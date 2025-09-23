package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CDmgREvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;
    private final LivingEntity target;
    private final double amount;
    private final String skillid;
    private final String skilltype;

    public CDmgREvent(LivingEntity caster, LivingEntity target, double amount, String skillid, String skilltype) {
        this.caster = caster;
        this.target = target;
        this.amount = amount;
        this.skillid = skillid;
        this.skilltype = skilltype;
    }

    /** The entity that caused the damage */
    public LivingEntity getCaster() {
        return caster;
    }

    /** The entity that received the damage */
    public LivingEntity getTarget() {
        return target;
    }

    public double getAmount() {
        return amount;
    }

    public String getSkillid() {
        return skillid;
    }

    public String getSkilltype() {
        return skilltype;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

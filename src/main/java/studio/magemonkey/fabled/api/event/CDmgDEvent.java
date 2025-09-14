package studio.magemonkey.fabled.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CDmgDEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;
    private final LivingEntity target;
    private final double amount;

    public CDmgDEvent(LivingEntity caster, LivingEntity target, double amount) {
        this.caster = caster;
        this.target = target;
        this.amount = amount;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

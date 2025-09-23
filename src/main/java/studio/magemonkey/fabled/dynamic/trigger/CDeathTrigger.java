package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CDeathEvent;

public class CDeathTrigger implements Trigger<CDeathEvent> {

    @Override
    public String getKey() {
        return "CDeath";
    }

    @Override
    public Class<CDeathEvent> getEvent() {
        return CDeathEvent.class;
    }

    @Override
    public boolean shouldTrigger(CDeathEvent event, int level, Settings settings) {
        return true; // Always fires unless you want conditions
    }

    @Override
    public void setValues(CDeathEvent event, CastData data) {
        data.put("cdeath-amount", event.getAmount());
        data.put("cdeath-skillid", event.getSkillid());
        data.put("cdeath-skilltype", event.getSkilltype());
    }

    @Override
    public LivingEntity getCaster(CDeathEvent event) {
        return event.getCaster(); // Dealer of damage
    }

    @Override
    public LivingEntity getTarget(CDeathEvent event, Settings settings) {
        return event.getTarget(); // Victim
    }
}

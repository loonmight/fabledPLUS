package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CKillEvent;

public class CKillTrigger implements Trigger<CKillEvent> {

    @Override
    public String getKey() {
        return "CKill";
    }

    @Override
    public Class<CKillEvent> getEvent() {
        return CKillEvent.class;
    }

    @Override
    public boolean shouldTrigger(CKillEvent event, int level, Settings settings) {
        return true; // Always fires unless you want conditions
    }

    @Override
    public void setValues(CKillEvent event, CastData data) {
        data.put("ckill-amount", event.getAmount());
        data.put("ckill-skillid", event.getSkillid());
        data.put("ckill-skilltype", event.getSkilltype());
    }

    @Override
    public LivingEntity getCaster(CKillEvent event) {
        return event.getCaster(); // Dealer of damage
    }

    @Override
    public LivingEntity getTarget(CKillEvent event, Settings settings) {
        return event.getTarget(); // Victim
    }
}

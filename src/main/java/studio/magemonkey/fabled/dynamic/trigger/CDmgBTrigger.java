package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CDmgBEvent;

import java.util.List;
import java.util.stream.Collectors;

public class CDmgBTrigger implements Trigger<CDmgBEvent> {

    @Override
    public String getKey() {
        return "CDmgB";
    }

    @Override
    public Class<CDmgBEvent> getEvent() {
        return CDmgBEvent.class;
    }

    @Override
    public boolean shouldTrigger(CDmgBEvent event, int level, Settings settings) {
        double min = settings.getDouble("min-amount", 0);
        double max = settings.getDouble("max-amount", 999);
        double blocked = event.getBlockedAmount();

        if (blocked < min || blocked > max) return false;

        // --- Skill ID check ---
        List<String> skillIds = settings.getStringList("allowed-skillids").stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> blackSkillIds = skillIds.stream().filter(s -> s.startsWith("!"))
                .map(s -> s.substring(1))
                .collect(Collectors.toList());
        skillIds = skillIds.stream().filter(s -> !s.startsWith("!")).collect(Collectors.toList());

        if (!skillIds.isEmpty() && !skillIds.contains(event.getSkillId())
                || blackSkillIds.contains(event.getSkillId())) {
            return false;
        }

        return true;
    }

    @Override
    public void setValues(CDmgBEvent event, CastData data) {
        data.put("cdmgb-amount", event.getBlockedAmount());
        data.put("cdmgb-skillid", event.getSkillId());
        data.put("cdmgb-victim", event.getVictim());
    }

    @Override
    public LivingEntity getCaster(CDmgBEvent event) {
        return event.getCaster();
    }

    @Override
    public LivingEntity getTarget(CDmgBEvent event, Settings settings) {
        return event.getTarget();
    }

    public LivingEntity getVictim(CDmgBEvent event) {
        return event.getVictim();
    }
}

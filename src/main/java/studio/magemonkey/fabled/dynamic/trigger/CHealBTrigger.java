package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CHealBEvent;

import java.util.List;
import java.util.stream.Collectors;

public class CHealBTrigger implements Trigger<CHealBEvent> {

    @Override
    public String getKey() {
        return "CHealB";
    }

    @Override
    public Class<CHealBEvent> getEvent() {
        return CHealBEvent.class;
    }

    @Override
    public boolean shouldTrigger(CHealBEvent event, int level, Settings settings) {
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
    public void setValues(CHealBEvent event, CastData data) {
        data.put("chealb-amount", event.getBlockedAmount());
        data.put("chealb-skillid", event.getSkillId());
        data.put("chealb-healed", event.getHealed());
    }

    @Override
    public LivingEntity getCaster(CHealBEvent event) {
        return event.getCaster();
    }

    @Override
    public LivingEntity getTarget(CHealBEvent event, Settings settings) {
        return event.getTarget();
    }

	public LivingEntity getHealed(CHealBEvent event) {
		return event.getHealed();
	}
}

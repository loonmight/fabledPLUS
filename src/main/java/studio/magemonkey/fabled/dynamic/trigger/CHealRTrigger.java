package studio.magemonkey.fabled.dynamic.trigger;

import org.bukkit.entity.LivingEntity;
import studio.magemonkey.fabled.api.CastData;
import studio.magemonkey.fabled.api.Settings;
import studio.magemonkey.fabled.api.event.CHealREvent;

import java.util.List;
import java.util.stream.Collectors;

public class CHealRTrigger implements Trigger<CHealREvent> {

    @Override
    public String getKey() {
        return "CHealR";
    }

    @Override
    public Class<CHealREvent> getEvent() {
        return CHealREvent.class;
    }

    @Override
    public boolean shouldTrigger(CHealREvent event, int level, Settings settings) {
        double min = settings.getDouble("min-amount", 0);
        double max = settings.getDouble("max-amount", 999);
        double heal = event.getAmount();

        if (heal < min || heal > max) return false;

        // --- Skill ID check ---
        List<String> skillIds = settings.getStringList("allowed-skillids").stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> blackSkillIds = skillIds.stream().filter(s -> s.startsWith("!"))
                .map(s -> s.substring(1))
                .collect(Collectors.toList());
        skillIds = skillIds.stream().filter(s -> !s.startsWith("!")).collect(Collectors.toList());

        if ((!skillIds.isEmpty() && !skillIds.contains(event.getSkillid()))
                || blackSkillIds.contains(event.getSkillid())) {
            return false;
        }

        // --- Skill Type check ---
        List<String> skillTypes = settings.getStringList("allowed-skilltypes").stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        List<String> blackSkillTypes = skillTypes.stream().filter(s -> s.startsWith("!"))
                .map(s -> s.substring(1))
                .collect(Collectors.toList());
        skillTypes = skillTypes.stream().filter(s -> !s.startsWith("!")).collect(Collectors.toList());

        if ((!skillTypes.isEmpty() && !skillTypes.contains(event.getSkilltype()))
                || blackSkillTypes.contains(event.getSkilltype())) {
            return false;
        }

        return true;
    }

    @Override
    public void setValues(CHealREvent event, CastData data) {
        data.put("chealr-amount", event.getAmount());
        data.put("chealr-skillid", event.getSkillid());
        data.put("chealr-skilltype", event.getSkilltype());
    }

    @Override
    public LivingEntity getCaster(CHealREvent event) {
        return event.getCaster();
    }

    @Override
    public LivingEntity getTarget(CHealREvent event, Settings settings) {
        return event.getTarget();
    }
}

package com.geel.customitemhovers.providers;

import com.geel.customitemhovers.expressions.ExecutionContext;
import com.geel.customitemhovers.expressions.models.Variable;
import com.geel.customitemhovers.expressions.providers.LazyVariableProvider;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

@Singleton
public class PlayerVariableProvider extends LazyVariableProvider {
    private Client client;

    @Inject
    public PlayerVariableProvider(Client client) {
        this.client = client;

        // TODO: Skills namespaces?
        for (Skill skill : Skill.values()) {
            String skillName = skill.getName().toLowerCase(Locale.ROOT);
            addNullSafe(skillName + "_level", player -> Variable.fromConstant(client.getBoostedSkillLevel(skill)));
            addNullSafe(skillName + "_level_real", player -> Variable.fromConstant(client.getRealSkillLevel(skill)));
            addNullSafe(skillName + "_xp", player -> Variable.fromConstant(client.getSkillExperience(skill)));
        }

        addNullSafe("player_x", player -> Variable.fromConstant(player.getWorldLocation().getX()));
        addNullSafe("player_y", player -> Variable.fromConstant(player.getWorldLocation().getY()));
        addNullSafe("player_plane", player -> Variable.fromConstant(player.getWorldLocation().getPlane()));
        addNullSafe("player_region", player -> Variable.fromConstant(player.getWorldLocation().getRegionID()));
        addNullSafe("player_region_x", player -> Variable.fromConstant(player.getWorldLocation().getRegionX()));
        addNullSafe("player_region_y", player -> Variable.fromConstant(player.getWorldLocation().getRegionY()));

        addNullSafe("player_hp", player -> Variable.fromConstant(client.getBoostedSkillLevel(Skill.HITPOINTS)));
        addNullSafe("player_prayer", player -> Variable.fromConstant(client.getBoostedSkillLevel(Skill.PRAYER)));

    }

    private void addNullSafe(String name, Function<Player, Variable> function) {
        super.add(name, createNullSafeFunction(function));
    }

    private Function<ExecutionContext, Variable> createNullSafeFunction(Function<Player, Variable> function) {
        return (context) -> {
            if (client == null || client.getGameState() != GameState.LOGGED_IN) {
                return Variable.fromConstant(0);
            }

            Player player = client.getLocalPlayer();
            return function.apply(player);
        };
    }
}

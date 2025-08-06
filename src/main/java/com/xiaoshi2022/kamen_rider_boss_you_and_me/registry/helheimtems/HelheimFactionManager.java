package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;

import java.util.*;

public class HelheimFactionManager {
    private static final Map<UUID, Faction> FACTIONS = new HashMap<>();

    public static class Faction {
        public final UUID id;
        public final FactionLeader leader;
        public final Set<ElementaryInvesHelheim> minions = new HashSet<>();

        public Faction(FactionLeader leader) {
            this.id = UUID.randomUUID();
            this.leader = leader;
        }
    }

    public static Optional<FactionLeader> findLeaderById(String factionId) {
        // 遍历FACTIONS中的所有值，将每个值映射为faction.leader
        return FACTIONS.values().stream().map(faction -> faction.leader)
                // 过滤出factionId与leader.getFactionId()相等的leader
                .filter(leader -> leader.getFactionId().equals(factionId)).findFirst();
    }

    public static Optional<Faction> getFaction(FactionLeader leader) {
        return Optional.ofNullable(FACTIONS.get(leader.getFactionId()));
    }
}
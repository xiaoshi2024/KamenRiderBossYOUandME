# 骑士腰带装备、变身与技能触发系统说明

## 腰带装备机制

当前系统支持以下几种腰带类型：
- **Genesis Driver** - 创世纪驱动器
- **Ghost Driver** - 幽灵驱动器
- **Sengoku Drivers (EPMTY)** - 战极驱动器
- **Two-Si Driver** - 二骑驱动器

### 腰带装备流程
1. **基础装备**：腰带必须通过右键直接装备到Curio槽位（参考AbstractRiderBelt类）
2. **精英怪物装备**：
   - EliteMonsterEquipHandler监听实体tick事件
   - 检测Curio槽中是否有腰带
   - 满足条件时触发变身状态

### 装备条件检查
```java
// 检查Curio槽中是否有腰带（唯一支持的装备方式）
boolean hasBeltInCurio = BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster);
```

## 变身触发机制

### 变身过程
1. **设置变身状态**：`eliteMonster.setTransformed(true)`
2. **触发变身逻辑**：调用`triggerTransformation(eliteMonster, driverItem)`
3. **更新实体状态**：刷新尺寸、重置血量
4. **装备对应盔甲**：根据腰带类型和模式装备不同盔甲

### 盔甲装备逻辑
- Genesis Driver根据不同BeltMode（LEMON、MELON等）装备对应盔甲
- 每种模式都有特定的盔甲套装和变身音效
- 变身成功后实体生命值提升到80点

```java
// 变身核心逻辑示例
private static void triggerTransformation(EliteMonsterNpc eliteMonster, ItemStack beltStack) {
    // 设置血量
    eliteMonster.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80.0D);
    eliteMonster.setHealth(80.0F);
    
    // 根据腰带类型和模式装备盔甲
    if (beltStack.getItem() instanceof Genesis_driver) {
        Genesis_driver driver = (Genesis_driver) beltStack.getItem();
        Genesis_driver.BeltMode mode = driver.getMode(beltStack);
        
        switch (mode) {
            case LEMON -> {
                // 装备Duke盔甲
                equipArmor(eliteMonster, EquipmentSlot.HEAD, new ItemStack(ModItems.DUKE_HELMET.get()));
                // ...
            }
            // 其他模式...
        }
    }
}
```

## 技能触发机制

### 技能触发流程
1. **技能调用入口**：EliteMonsterNpc类的useSkill方法
2. **技能选择逻辑**：根据armorType选择对应的技能类
3. **技能执行**：调用对应技能类的perform方法

```java
// 技能调用示例（来自EliteMonsterNpc类）
public void useSkill(Level world, LivingEntity target) {
    if (armorType == 6) {
        DarkOrangeAngelsSkill.perform(world, this, target);
    }
    // 其他盔甲类型的技能...
}
```

### 技能使用条件
- 实体必须处于变身状态
- 目标必须存在
- 满足技能冷却时间要求
- 概率触发（部分技能有40%的触发概率）

## 代码示例：为怪物装备腰带

### 使用辅助类装备腰带
```java
// 为精英怪物装备Genesis Driver（柠檬模式）到Curio槽
BeltEquipHelper.equipGenesisDriver(eliteMonster, Genesis_driver.BeltMode.LEMON);

// 为精英怪物装备Ghost Driver（黑暗骑士眼魂模式）到Curio槽
BeltEquipHelper.equipGhostDriver(eliteMonster, GhostDriver.BeltMode.DARK_RIDER_EYE);

// 为精英怪物装备Sengoku Driver到Curio槽
BeltEquipHelper.equipSengokuDriver(eliteMonster);

// 为精英怪物装备Two-Si Driver到Curio槽
BeltEquipHelper.equipTwoSiDriver(eliteMonster);
```

> 注意：所有腰带辅助方法都设计为将腰带装备到Curio槽位，这是唯一支持的装备方式。

### 使用命令生成带腰带的怪物
```
# 注意：手持装备腰带不再被支持，以下命令示例仅作参考，实际效果可能不符合预期
/summon kamen_rider_boss_you_and_me:elite_monster_npc ~ ~1 ~ {HandItems:[{id:"kamen_rider_boss_you_and_me:genesis_driver",Count:1b,tag:{BeltMode:"LEMON"}},{}]}

# 召唤装备了幽灵驱动器(DARK_RIDER_EYE模式)的精英怪物
/summon kamen_rider_boss_you_and_me:elite_monster_npc ~ ~1 ~ {HandItems:[{id:"kamen_rider_boss_you_and_me:ghost_driver",Count:1b,tag:{BeltMode:"DARK_RIDER_EYE"}},{}]}
```

## 注意事项
1. **自动变身**：将腰带装备到Curio槽后，EliteMonsterEquipHandler会在实体更新时自动触发变身
2. **盔甲可见性**：变身状态下盔甲才会显示，确保isTransformed为true
3. **技能冷却**：技能有冷却时间，避免频繁触发
4. **音效冷却**：变身音效有3秒冷却，防止重复播放
5. **Ghost Driver特殊效果**：Ghost Driver会为持有者提供永久夜视效果，并在装备时触发'show'动画
6. **模式对应**：不同腰带的模式设置需要与对应的盔甲类相匹配，以确保正确的变身效果
7. **槽位要求**：腰带必须放置在Curio槽位才能正常工作，手持腰带不再被支持

通过以上系统，实现了腰带装备、变身和技能触发的完整功能链，为精英怪物提供了骑士腰带相关的能力。
# 异类Decade召唤实体配置指南

本指南提供了整合包作者如何通过数据包和命令系统来配置异类Decade（Another Decade）可以召唤的实体。

## 功能概述

异类Decade的`AnotherWorldSummon`技能现在支持通过命令和数据包来动态配置它可以召唤的实体。您可以：

- 添加任何模组或原版实体到普通召唤池
- 添加特殊实体到低血量召唤池（当异类Decade血量≤30%时使用）
- 添加强大实体到愤怒状态召唤池（当异类Decade处于狂暴状态时使用）
- 为每种实体设置权重，控制召唤概率
- 查看当前配置的实体列表
- 开启调试模式以查看召唤相关日志

## 命令系统

### 1. 管理员命令（需要OP权限）

#### 添加普通召唤实体
```
/anotherdecade summon add <entity_id> <weight>
```
- `entity_id`: 实体的资源位置，格式为`modid:entity_name`
- `weight`: 召唤权重（0.1-5.0之间），值越高被召唤的概率越大

**示例：**
```
/anotherdecade summon add minecraft:zombie 1.0
/anotherdecade summon add anotherdimension:another_geats 2.0
```

#### 添加低血量时召唤的实体
```
/anotherdecade summon add low_health <entity_id> <weight>
```

**示例：**
```
/anotherdecade summon add low_health minecraft:vex 2.0
/anotherdecade summon add low_health minecraft:enderman 1.5
```

#### 添加愤怒状态时召唤的实体
```
/anotherdecade summon add enraged <entity_id> <weight>
```

**示例：**
```
/anotherdecade summon add enraged minecraft:ravager 2.5
/anotherdecade summon add enraged anotherdimension:another_ohma_zi_o 3.0
```

#### 查看当前配置的实体列表
```
/anotherdecade summon list
```

#### 清空所有召唤实体配置
```
/anotherdecade summon clear
```

#### 启用/禁用调试模式
```
/anotherdecade debug on
/anotherdecade debug off
```

### 2. 数据包命令（无需OP权限，适合数据包使用）

数据包中使用以下命令格式来配置实体：

```
/data anotherdecade summon add <pool_type> <entity_id> <weight>
```
- `pool_type`: 召唤池类型（normal、low_health、enraged）
- `entity_id`: 实体的资源位置
- `weight`: 召唤权重

## 数据包配置示例

以下是一个数据包中配置文件的示例，展示如何为异类Decade添加自定义召唤实体：

### 1. 创建数据包结构

```
my_pack/
├── pack.mcmeta
└── data/
    └── my_pack/
        └── functions/
            └── configure_another_decade.mcfunction
```

### 2. 在`configure_another_decade.mcfunction`中添加命令

```mcfunction
# 添加普通召唤实体
data anotherdecade summon add normal minecraft:zombie 1.0
data anotherdecade summon add normal minecraft:skeleton 1.0
data anotherdecade summon add normal anotherdimension:another_geats 1.5
data anotherdecade summon add normal anotherdimension:another_revice 1.3

# 添加低血量时召唤的实体
data anotherdecade summon add low_health minecraft:vex 2.0
data anotherdecade summon add low_health minecraft:enderman 1.8
data anotherdecade summon add low_health anotherdimension:phantom_rider 2.2

# 添加愤怒状态时召唤的实体
data anotherdecade summon add enraged minecraft:ravager 2.5
data anotherdecade summon add enraged minecraft:wither_skeleton 2.0
data anotherdecade summon add enraged anotherdimension:another_ohma_zi_o 3.0
data anotherdecade summon add enraged anotherdimension:ultimate_another_form 3.5
```

### 3. 在`pack.mcmeta`中定义数据包

```json
{
  "pack": {
    "pack_format": 10,
    "description": "配置异类Decade召唤实体"
  }
}
```

### 4. 创建一个初始化函数来在世界加载时执行配置

在`data/my_pack/tags/functions/load.json`中：

```json
{
  "values": [
    "my_pack:configure_another_decade"
  ]
}
```

## 最佳实践

1. **平衡权重设置**：普通实体（1.0-1.5）、精英实体（1.5-2.5）、终极实体（2.5-3.5）

2. **考虑游戏难度**：确保配置的实体不会让游戏过于困难或过于简单

3. **利用特殊状态召唤池**：为低血量和愤怒状态配置更具挑战性的实体

4. **混合模组实体**：结合不同模组的实体，创造独特的战斗体验

5. **测试配置**：使用调试模式验证实体是否正确被召唤

## 技术细节

- 实体ID必须使用`modid:entity_name`格式
- 权重值限制在0.1到5.0之间，超出范围会被自动调整
- 召唤时会使用基于权重的随机选择算法
- 低血量（≤30%）和愤怒状态时，会同时使用普通召唤池和对应特殊召唤池

## 常见问题解答

**Q: 为什么我添加的实体没有被召唤？**
A: 请检查：
- 实体ID格式是否正确（modid:entity_name）
- 实体是否是Mob类型（只有生物才能被召唤）
- 可以启用调试模式查看详细日志

**Q: 如何移除特定实体？**
A: 目前移除特定实体功能正在开发中，您可以使用`/anotherdecade summon clear`清空所有配置后重新添加。

**Q: 可以添加Boss级实体吗？**
A: 可以，但请注意控制权重，避免频繁召唤过强的实体破坏游戏平衡。

**Q: 配置会在服务器重启后保留吗？**
A: 通过数据包配置的会保留，通过管理员命令配置的在重启后会重置为默认值。建议使用数据包进行永久配置。
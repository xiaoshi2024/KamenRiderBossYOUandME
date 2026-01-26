package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.PlayerArmorPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.PlayerEquipmentPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 数据包批处理类，用于合并多个小数据包，减少网络传输次数
 */
public class BatchPacket {
    // 定义支持的数据包类型枚举
    public enum PacketType {
        PLAYER_ANIMATION(0),
        PLAYER_ARMOR(1),
        PLAYER_EQUIPMENT(2),
        BELT_ANIMATION(3);
        
        private final int id;
        
        PacketType(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public static PacketType fromId(int id) {
            for (PacketType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }
    
    // 内部类：表示单个批量数据包项
    public static class BatchItem {
        private final PacketType type;
        private final byte[] data;
        
        public BatchItem(PacketType type, byte[] data) {
            this.type = type;
            this.data = data;
        }
        
        public PacketType getType() {
            return type;
        }
        
        public byte[] getData() {
            return data;
        }
    }
    
    private final List<BatchItem> items;
    
    public BatchPacket() {
        this.items = new ArrayList<>();
    }
    
    public BatchPacket(List<BatchItem> items) {
        this.items = items;
    }
    
    /**
     * 添加一个数据包到批处理中
     */
    public void addItem(PacketType type, byte[] data) {
        this.items.add(new BatchItem(type, data));
    }
    
    /**
     * 检查批处理是否为空
     */
    public boolean isEmpty() {
        return this.items.isEmpty();
    }
    
    /**
     * 获取批处理中的所有项
     */
    public List<BatchItem> getItems() {
        return this.items;
    }
    
    /**
     * 编码批处理数据包
     */
    public static void encode(BatchPacket packet, FriendlyByteBuf buffer) {
        // 写入数据包数量
        buffer.writeVarInt(packet.items.size());
        
        // 写入每个数据包项
        for (BatchItem item : packet.items) {
            // 写入数据包类型
            buffer.writeVarInt(item.getType().getId());
            // 写入数据长度和数据
            buffer.writeVarInt(item.getData().length);
            buffer.writeBytes(item.getData());
        }
    }
    
    /**
     * 解码批处理数据包
     */
    public static BatchPacket decode(FriendlyByteBuf buffer) {
        BatchPacket batchPacket = new BatchPacket();
        int itemCount = buffer.readVarInt();
        
        // 读取每个数据包项
        for (int i = 0; i < itemCount; i++) {
            // 读取数据包类型
            int typeId = buffer.readVarInt();
            PacketType type = PacketType.fromId(typeId);
            if (type != null) {
                // 读取数据长度和数据
                int dataLength = buffer.readVarInt();
                byte[] data = buffer.readBytes(dataLength).array();
                batchPacket.addItem(type, data);
            }
        }
        
        return batchPacket;
    }
    
    /**
     * 处理批处理数据包
     */
    public static void handle(BatchPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 处理批处理数据包中的每个子数据包
            for (BatchItem item : packet.items) {
                handleBatchItem(item, context);
            }
        });
        context.setPacketHandled(true);
    }
    
    /**
     * 处理单个批处理项
     */
    private static void handleBatchItem(BatchItem item, NetworkEvent.Context context) {
        switch (item.getType()) {
            case PLAYER_ANIMATION:
                PlayerAnimationPacket animationPacket = decodePlayerAnimationPacket(item.getData());
                PlayerAnimationPacket.handle(animationPacket, () -> context);
                break;
            case PLAYER_ARMOR:
                PlayerArmorPacket armorPacket = decodePlayerArmorPacket(item.getData());
                PlayerArmorPacket.handle(armorPacket, () -> context);
                break;
            case PLAYER_EQUIPMENT:
                PlayerEquipmentPacket equipmentPacket = decodePlayerEquipmentPacket(item.getData());
                PlayerEquipmentPacket.handle(equipmentPacket, () -> context);
                break;
            case BELT_ANIMATION:
                BeltAnimationPacket beltAnimationPacket = decodeBeltAnimationPacket(item.getData());
                BeltAnimationPacket.handle(beltAnimationPacket, () -> context);
                break;
        }
    }
    
    /**
     * 将byte[]数据转换为FriendlyByteBuf
     */
    private static FriendlyByteBuf toByteBuf(byte[] data) {
        // 使用Netty的Unpooled类来创建ByteBuf
        return new FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(data));
    }
    
    /**
     * 解码PlayerAnimationPacket
     */
    private static PlayerAnimationPacket decodePlayerAnimationPacket(byte[] data) {
        return PlayerAnimationPacket.decode(toByteBuf(data));
    }
    
    /**
     * 解码PlayerArmorPacket
     */
    private static PlayerArmorPacket decodePlayerArmorPacket(byte[] data) {
        return PlayerArmorPacket.decode(toByteBuf(data));
    }
    
    /**
     * 解码PlayerEquipmentPacket
     */
    private static PlayerEquipmentPacket decodePlayerEquipmentPacket(byte[] data) {
        return PlayerEquipmentPacket.decode(toByteBuf(data));
    }
    
    /**
     * 解码BeltAnimationPacket
     */
    private static BeltAnimationPacket decodeBeltAnimationPacket(byte[] data) {
        return BeltAnimationPacket.decode(toByteBuf(data));
    }
}
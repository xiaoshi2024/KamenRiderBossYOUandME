//package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
//import net.minecraft.client.model.VillagerModel;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.entity.layers.RenderLayer;
//import net.minecraft.world.entity.npc.Villager;
//import net.minecraft.world.item.ItemDisplayContext;
//import net.minecraft.world.item.ItemStack;
//import top.theillusivec4.curios.api.CuriosApi;
//
//public class KaitoCuriosLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
//
//    public KaitoCuriosLayer(RenderLayerParent<Villager, VillagerModel<Villager>> renderer) {
//        super(renderer);
//    }
//
//    @Override
//    public void render(PoseStack pose,
//                       MultiBufferSource buffer,
//                       int packedLight,
//                       Villager villager,
//                       float limbSwing,
//                       float limbSwingAmount,
//                       float partialTicks,
//                       float ageInTicks,
//                       float netHeadYaw,
//                       float headPitch) {
//
//        // 1. 安全地拿到腰带 ItemStack
//        CuriosApi.getCuriosInventory(villager).ifPresent(inv -> {
//            var opt = inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver);
//            if (opt.isPresent()) {
//                ItemStack stack = opt.get().stack();
//
//                pose.pushPose();
//
//                // 2. 手动平移到腰部（VillagerModel 没有 .body）
//                //    参考 VillagerModel 的渲染：root -> body，但直接 translate 就行
//                pose.translate(0, 0.75, 0);   // Y 值根据村民模型微调
//                pose.scale(0.6F, 0.6F, 0.6F); // 整体缩小
//
//                // 3. 渲染腰带
//                new GenesisDriverRenderer().renderByItem(stack,
//                        ItemDisplayContext.FIXED,
//                        pose,
//                        buffer,
//                        packedLight,
//                        0);
//
//                pose.popPose();
//            }
//        });
//    }
//}
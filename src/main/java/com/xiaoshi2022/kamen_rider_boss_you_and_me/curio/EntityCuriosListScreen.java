package com.xiaoshi2022.kamen_rider_boss_you_and_me.curio;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class EntityCuriosListScreen extends AbstractContainerScreen<EntityCuriosListMenu> {

    // 背景纹理 - 使用自定义占位图像
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("kamen_rider_boss_you_and_me:textures/gui/container/entity_curios_list_placeholder.png");
    private float xMouse = 0.0F;
    private float yMouse = 0.0F;

    public EntityCuriosListScreen(EntityCuriosListMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        // 参考RiderFusionMachine的尺寸，提供更大的空间
        this.imageWidth = 176;
        this.imageHeight = 166; // 保持合理的高度
        // 调整标签位置以匹配新的布局
        this.inventoryLabelY = 92;
    }

    @Override
    protected void init() {
        super.init();
        // 初始化屏幕组件
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelX = 8; // 与参考代码保持一致
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 渲染背景，添加正确的纹理缩放参数
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // 为Curio槽位区域添加自定义背景框（可选）
        // 这里可以添加额外的视觉提示，比如在腰带槽位周围渲染边框
        
        // 计算腰带槽位区域的位置和大小
        int beltSlotsX = this.leftPos + 25; // 与Menu中的槽位X坐标对应
        int beltSlotsY = this.topPos + 18; // 与Menu中的槽位Y坐标对应
        int beltSlotsWidth = 50; // 单个槽位的宽度区域
        int beltSlotsHeight = 36; // 槽位高度区域
        
        // 如果有目标实体，可以在这里渲染实体模型
        if (this.menu.getTarget() != null) {
            // 实体模型渲染代码（可选）
            // InventoryScreen.renderEntityInInventory(guiGraphics, x + 120, y + 30, 30, (float)(x + 120) - this.xMouse, (float)(y + 30 - 40) - this.yMouse, this.menu.getTarget());
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染标题
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 6, 4210752, false);

        // 渲染玩家物品栏标签
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        
        // 添加腰带槽位区域的标签，放在槽位上方
        // 使用硬编码文本作为后备，确保即使翻译键未找到也能显示内容
        Component beltSlotText = Component.translatable("gui.kamen_rider_boss_you_and_me.entity_curios.belt_slot");
        if (beltSlotText.getString().contains("gui.kamen_rider_boss_you_and_me.entity_curios.belt_slot")) {
            beltSlotText = Component.literal("腰带槽位");
        }
        guiGraphics.drawString(this.font, beltSlotText, 30, 12, 4210752, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 处理Shift+右键点击背包物品快速放入curios槽位的功能
        if (button == 1 && this.getMinecraft().options.keyShift.isDown() && 
                this.hoveredSlot != null && !this.menu.getCarried().isEmpty() && 
                this.hoveredSlot.container instanceof Inventory) {
            // 查找第一个可用的curios槽位
            for (int i = 0; i < this.menu.slots.size(); i++) {
                Slot slot = this.menu.slots.get(i);
                if (slot instanceof EntityCuriosListMenu.CurioSlot && 
                        slot.mayPlace(this.menu.getCarried()) && !slot.hasItem()) {
                    // 正确处理物品移动
                    this.getMinecraft().gameMode.handleInventoryMouseClick(
                            this.menu.containerId, i, 0, ClickType.PICKUP, this.getMinecraft().player);
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void removed() {
        super.removed();
        // 清理资源
    }

}
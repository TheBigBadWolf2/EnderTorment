package white_blizz.ender_torment.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import white_blizz.ender_torment.common.container.EnderFluxBatteryContainer;
import white_blizz.ender_torment.utils.Ref;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
public class EnderFluxBatteryScreen extends ContainerScreen<EnderFluxBatteryContainer> {
	private static final ResourceLocation BACKGROUND_TEXTURE = Ref.loc(
			"textures/gui/container/",
			"widget",
			"png"
	);


	public EnderFluxBatteryScreen(EnderFluxBatteryContainer container, PlayerInventory player, ITextComponent title) {
		super(container, player, title);
		addButton(new Panel(this, title.getFormattedText(),
				0xFFFF00,
				container.inventorySlots
						.stream()
						.filter(slot -> !(slot.inventory instanceof PlayerInventory))
						.toArray(Slot[]::new)
		));
		addButton(new Panel(this, player.getDisplayName().getFormattedText(),
				0xFF00FF,
				container.inventorySlots
						.stream()
						.filter(slot -> slot.inventory instanceof PlayerInventory)
						.toArray(Slot[]::new)
		));
	}

	@Override
	public void render(final int mouseX, final int mouseY, final float partialTicks) {
		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);

		int relMouseX = mouseX - this.guiLeft;
		int relMouseY = mouseY - this.guiTop;

	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		// Copied from AbstractFurnaceScreen#drawGuiContainerForegroundLayer
		//String s = this.title.getFormattedText();
		//this.font.drawString(s, (float) (this.xSize / 2 - this.font.getStringWidth(s) / 2), 6.0F, 0x404040);
		//this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, (float) (this.ySize - 96 + 2), 0x404040);
	}

	@Override
	protected void init() {
		super.init();
		addButton(new Panel(this, title.getFormattedText(),
				0xFFFF00,
				container.inventorySlots
						.stream()
						.filter(slot -> !(slot.inventory instanceof PlayerInventory))
						.toArray(Slot[]::new)
		));
		addButton(new Panel(this, playerInventory.getDisplayName().getFormattedText(),
				0xFF00FF,
				container.inventorySlots
						.stream()
						.filter(slot -> slot.inventory instanceof PlayerInventory)
						.toArray(Slot[]::new)
		));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		int startX = this.guiLeft;
		int startY = this.guiTop;

		GuiUtils.drawContinuousTexturedBox(
				BACKGROUND_TEXTURE,
				startX, startY,
				0, 0,
				this.xSize, this.ySize,
				12, 12,
				4, this.getBlitOffset()
		);

		// Screen#blit draws a part of the current texture (assumed to be 256x256) to the screen
		// The parameters are (x, y, u, v, width, height)

		//this.blit(startX, startY, 0, 0, this.xSize, this.ySize);

		getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		//container.inventorySlots.forEach(slot -> this.blit(startX + slot.xPos - 1, startY + slot.yPos - 1, 12, 0, 18, 18));
	}

}

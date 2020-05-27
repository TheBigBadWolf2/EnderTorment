package white_blizz.ender_torment.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import white_blizz.ender_torment.utils.Ref;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class Panel extends Widget {
	private static final ResourceLocation WIDGET_LOC = Ref.MOD.rl.loc(
			"textures/gui/container/",
			"widget",
			"png"
	);

	private final ContainerScreen<?> parent;
	private final int color;
	private final Slot[] slots;

	public Panel(ContainerScreen<?> parent, String msg, int color, Slot... slots) {
		super(-1, -1, -1, -1, msg);
		this.parent = parent;
		this.color = color;
		this.slots = slots;

		class Point {
			int x, y;

			Point(int x, int y) {
				this.x = x;
				this.y = y;
			}

			Point copy() { return new Point(x, y); }
		}

		class MinMax {
			Point min, max;

			MinMax test(Point p) {
				if (min == null) min = p.copy();
				else {
					if (p.x < min.x) min.x = p.x;
					if (p.y < min.y) min.y = p.y;
				}
				if (max == null) max = p.copy();
				else {
					if (p.x > max.x) max.x = p.x;
					if (p.y > max.y) max.y = p.y;
				}
				return this;
			}
		}

		MinMax minMax = Arrays.stream(slots)
				.map(slot -> new Point(slot.xPos, slot.yPos))
				.reduce(new MinMax(), MinMax::test, (mm1, mm2) -> mm1);

		int minX = minMax.min.x;
		int minY = minMax.min.y;
		int maxX = minMax.max.x;
		int maxY = minMax.max.y;

		x = minX - 3;
		y = minY - 3;
		width = maxX - minX + 18 + 4;
		height = maxY - minY + 18 + 4;

		/*Arrays.stream(slots)
				.mapToInt(slot -> slot.xPos)
				.min()
				.ifPresent(minX -> Arrays.stream(slots)
						.mapToInt(slot -> slot.yPos)
						.min()
						.ifPresent(minY -> Arrays.stream(slots)
								.mapToInt(slot -> slot.xPos)
								.max()
								.ifPresent(maxX -> Arrays.stream(slots)
										.mapToInt(slot -> slot.yPos)
										.max()
										.ifPresent(maxY -> {
			x = minX - 3;
			y = minY - 3;
			width = maxX - minX + 18 + 4;
			height = maxY - minY + 18 + 4;
		}))));*/
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float p_renderButton_3_) {
		Minecraft mc = Minecraft.getInstance();
		this.renderBg(mc, mouseX, mouseY);
		FontRenderer fr = mc.fontRenderer;
		String msg = getMessage();
		int dy = fr.FONT_HEIGHT - 2;
		drawBox(getX(), getY() - dy - 2,
				fr.getStringWidth(msg) + 4,
				dy + 3, true);
		this.drawString(fr, msg,
				getX() + 2,
				getY() - dy,
				getFGColor()
		);
	}

	private int getX() { return parent.getGuiLeft() + x; }
	private int getY() { return parent.getGuiTop() + y; }

	private int getNeededWidth() {
		return Math.max(width, Minecraft.getInstance().fontRenderer.getStringWidth(getMessage()) + 4);
	}

	private void drawBox(int x, int y, int width, int height, boolean tab) {
		int v = tab ? 18 : 25;
		GuiUtils.drawContinuousTexturedBox(
				WIDGET_LOC,
				x, y,
				0, v,
				width, height,
				7, 7,
				1, this.getBlitOffset()
		);
		int r = (color >> 16) & 255;
		int g = (color >>  8) & 255;
		int b = (color >>  0) & 255;
		RenderSystem.color3f(r / 255F, g / 255F, b / 255F);
		WidgetUtils.drawContinuousTexturedBox(
				WIDGET_LOC,
				x, y,
				7, v,
				width, height,
				7, 7,
				1, this.getBlitOffset()
		);
	}

	@Override
	protected void renderBg(Minecraft mc, int mouseX, int mouseY) {
		/*RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		GuiUtils.drawContinuousTexturedBox(
				WIDGET_LOC,
				getX(), getY(),
				0, 25,
				width, height,
				7, 7,
				1, this.getBlitOffset()
		);
		int r = (color >> 16) & 255;
		int g = (color >>  8) & 255;
		int b = (color >>  0) & 255;
		RenderSystem.color3f(r / 255F, g / 255F, b / 255F);
		GuiUtils.drawContinuousTexturedBox(
				WIDGET_LOC,
				getX(), getY(),
				7, 25,
				width, height,
				7, 7,
				1, this.getBlitOffset()
		);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);*/
		FontRenderer fr = mc.fontRenderer;
		drawBox(getX(), getY(), getNeededWidth(), height, false);
		mc.getTextureManager().bindTexture(WIDGET_LOC);
		for (Slot slot : slots) {
			this.blit(parent.getGuiLeft() + slot.xPos - 1, parent.getGuiTop() + slot.yPos - 1, 12, 0, 18, 18);
		}

	}
}

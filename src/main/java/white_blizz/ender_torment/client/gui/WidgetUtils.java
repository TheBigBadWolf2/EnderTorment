package white_blizz.ender_torment.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public final class WidgetUtils {

	public static void drawContinuousTexturedBox(
			ResourceLocation res,
			int x, int y,
			int u, int v,
			int width, int height,
			int textureWidth, int textureHeight,
			int borderSize,
			float zLevel) {
		drawContinuousTexturedBox(
				res,
				x, y,
				u, v,
				width, height,
				textureWidth, textureHeight,
				borderSize, borderSize, borderSize, borderSize,
				zLevel
		);
	}

	public static void drawContinuousTexturedBox(
			ResourceLocation res,
			int x, int y,
			int u, int v,
			int width, int height,
			int textureWidth, int textureHeight,
			int topBorder, int bottomBorder, int leftBorder, int rightBorder,
			float zLevel) {
		Minecraft.getInstance().getTextureManager().bindTexture(res);
		drawContinuousTexturedBox(
				x, y,
				u, v,
				width, height,
				textureWidth, textureHeight,
				topBorder, bottomBorder, leftBorder, rightBorder,
				zLevel
		);
	}

	public static void drawContinuousTexturedBox(
			int x, int y,
			int u, int v,
			int width, int height,
			int textureWidth, int textureHeight,
			int topBorder, int bottomBorder, int leftBorder, int rightBorder,
			float zLevel
	) {
		//RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		int fillerWidth = textureWidth - leftBorder - rightBorder;
		int fillerHeight = textureHeight - topBorder - bottomBorder;
		int canvasWidth = width - leftBorder - rightBorder;
		int canvasHeight = height - topBorder - bottomBorder;
		int xPasses = canvasWidth / fillerWidth;
		int remainderWidth = canvasWidth % fillerWidth;
		int yPasses = canvasHeight / fillerHeight;
		int remainderHeight = canvasHeight % fillerHeight;

		// Draw Border
		// Top Left
		GuiUtils.drawTexturedModalRect(x, y, u, v, leftBorder, topBorder, zLevel);
		// Top Right
		GuiUtils.drawTexturedModalRect(x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
		// Bottom Left
		GuiUtils.drawTexturedModalRect(x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
		// Bottom Right
		GuiUtils.drawTexturedModalRect(x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);

		for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
			// Top Border
			GuiUtils.drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel);
			// Bottom Border
			GuiUtils.drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel);

			// Throw in some filler for good measure
			for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
				GuiUtils.drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel);
		}

		// Side Borders
		for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
			// Left Border
			GuiUtils.drawTexturedModalRect(x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
			// Right Border
			GuiUtils.drawTexturedModalRect(x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
		}
	}
}

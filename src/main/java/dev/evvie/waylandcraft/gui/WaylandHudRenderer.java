package dev.evvie.waylandcraft.gui;

import java.awt.Color;
import java.util.Calendar;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.WaylandCraft.KeyboardCaptureMode;
import dev.evvie.waylandcraft.bridge.IconSurface;
import dev.evvie.waylandcraft.bridge.WLCAbstractWindow.SurfaceGeometry;
import dev.evvie.waylandcraft.bridge.WLCToplevel;
import dev.evvie.waylandcraft.desktop.DesktopEntry;
import dev.evvie.waylandcraft.render.RenderUtils;
import dev.evvie.waylandcraft.render.WindowFramebuffer;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WaylandHudRenderer {
	
	private WaylandCraft wlc;
	private static final ResourceLocation TIME_DATE = ResourceLocation.fromNamespaceAndPath(WaylandCraft.MOD_ID, "time-date");
	private static final ResourceLocation APP_LIST = ResourceLocation.fromNamespaceAndPath(WaylandCraft.MOD_ID, "app-list");
	private static final ResourceLocation PINNED_TOPLEVEL = ResourceLocation.fromNamespaceAndPath(WaylandCraft.MOD_ID, "pinned-toplevel");
	private static final ResourceLocation DND_ICON = ResourceLocation.fromNamespaceAndPath(WaylandCraft.MOD_ID, "dnd-icon");
	
	public WaylandHudRenderer(WaylandCraft wlc) {
		this.wlc = wlc;
	}
	
	public void register(LayeredDrawerWrapper wrapper) {
		wrapper.attachLayerAfter(IdentifiedLayer.CHAT, TIME_DATE, this::renderTimeDate);
		wrapper.attachLayerAfter(IdentifiedLayer.CHAT, APP_LIST, this::renderAppList);
		wrapper.attachLayerAfter(IdentifiedLayer.CHAT, PINNED_TOPLEVEL, this::renderPinnedToplevel);
		wrapper.attachLayerAfter(IdentifiedLayer.CHAT, DND_ICON, this::renderDNDIcon);
	}
	
	private void renderAppList(GuiGraphics context, DeltaTracker deltaTracker) {
		Font font = Minecraft.getInstance().font;
		int yoff = 30;
		int ystep = font.lineHeight + 2;
		
		if(WaylandCraft.instance.keyboardCaptureMode == KeyboardCaptureMode.CAPTURE) {
			String text = "KEYBOARD CAPTURED [PRESS ESCAPE]";
			context.drawString(font, text, context.guiWidth() - font.width(text) - 10, yoff, Color.red.getRGB(), true);
			yoff += ystep;
		}
		else if(WaylandCraft.instance.keyboardCaptureMode == KeyboardCaptureMode.HARD_CAPTURE) {
			String text = "KEYBOARD CAPTURED [PRESS ALT+Q]";
			context.drawString(font, text, context.guiWidth() - font.width(text) - 10, yoff, Color.red.getRGB(), true);
			yoff += ystep;
		}
		
		for(WLCToplevel toplevel : WaylandCraft.instance.bridge.getMappedToplevels()) {
			String appID = toplevel.appID;
			DesktopEntry entry = wlc.xdgManager.forAppId(appID);
			
			String name = "<unknown app>";
			if(appID != null) name = appID;
			if(entry != null && entry.name != null) name = entry.name;
			
			Style style = Style.EMPTY;
			Color color = Color.white;
			
			if(!wlc.hasDisplayFor(toplevel)) {
				color = Color.lightGray;
			}
			if(toplevel == wlc.bridge.getMostRecentFocus()) {
				style = style.applyFormat(ChatFormatting.UNDERLINE);
			}
			
			int x = context.guiWidth() - font.width(name) - 10;
			context.drawString(font, Component.literal(name).withStyle(style), x, yoff, color.getRGB(), true);
			
			if(entry != null) {
				ResourceLocation icon = entry.getIcon();
				int iconX = x - font.lineHeight - 2;
				int iconY = yoff;
				int iconSize = font.lineHeight;
				if(icon != null) RenderUtils.blit(context, icon, iconX, iconY, iconSize, iconSize);
			}
			
			yoff += ystep;
		}
	}
	
	private void renderPinnedToplevel(GuiGraphics context, DeltaTracker deltaTracker) {
		int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
		
		if(wlc.pinnedToplevel != null && !wlc.pinnedToplevel.isAlive()) wlc.pinnedToplevel = null;
		if(wlc.pinnedToplevel != null) {
			WindowFramebuffer buf = wlc.pinnedToplevel.framebuffer;
			SurfaceGeometry geometry = wlc.pinnedToplevel.geometry;
			
			float x = -buf.getXOff() - geometry.x();
			float y = -buf.getYOff() - geometry.y();
			float w = buf.getWidth();
			float h = buf.getHeight();
			
			x /= guiScale * 2;
			y /= guiScale * 2;
			w /= guiScale * 2;
			h /= guiScale * 2;
			
			Vec3 tl = new Vec3(x, y, 0);
			Vec3 bl = new Vec3(x, y + h, 0);
			Vec3 br = new Vec3(x + w, y + h, 0);
			Vec3 tr = new Vec3(x + w, y, 0);
			RenderSystem.enableBlend();
			RenderUtils.renderWindow(buf, false, context.pose().last(), tl, bl, br, tr, new Vec2(0, 0), new Vec2(0, 1), new Vec2(1, 1), new Vec2(1, 0));
			RenderSystem.disableBlend();
		}
	}
	
	private void renderDNDIcon(GuiGraphics context, DeltaTracker tracker) {
		int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
		
		IconSurface dndIcon = wlc.bridge.dndIcon;
		if(dndIcon != null && dndIcon.framebuffer != null) {
			WindowFramebuffer buf = dndIcon.framebuffer;
			
			float x = -buf.getXOff();
			float y = -buf.getYOff();
			float w = buf.getWidth();
			float h = buf.getHeight();
			
			x /= guiScale;
			y /= guiScale;
			w /= guiScale;
			h /= guiScale;
			
			x += context.guiWidth() / 2;
			y += context.guiHeight() / 2;
			
			Vec3 tl = new Vec3(x, y, 0);
			Vec3 bl = new Vec3(x, y + h, 0);
			Vec3 br = new Vec3(x + w, y + h, 0);
			Vec3 tr = new Vec3(x + w, y, 0);
			RenderSystem.enableBlend();
			RenderUtils.renderWindow(buf, false, context.pose().last(), tl, bl, br, tr, new Vec2(0, 0), new Vec2(0, 1), new Vec2(1, 1), new Vec2(1, 0));
			RenderSystem.disableBlend();
		}
	}
	
	private void renderTimeDate(GuiGraphics context, DeltaTracker deltaTracker) {
		Font font = Minecraft.getInstance().font;
		String datetime = String.format("%1$tF %1$tR", Calendar.getInstance());
		
		context.drawString(font, datetime, context.guiWidth() - font.width(datetime) - 2, 2, Color.white.getRGB(), true);
	}
	
}

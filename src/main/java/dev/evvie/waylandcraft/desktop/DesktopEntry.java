package dev.evvie.waylandcraft.desktop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

public class DesktopEntry {
	
	public @NotNull String appId;
	public @Nullable String name;
	public @Nullable String genericName;
	public @Nullable String exec;
	public boolean execTerminal;
	public boolean visible;
	public @Nullable ResourceLocation icon;
	protected String iconPath;
	protected AbstractTexture iconTex;
	
	public DesktopEntry(String appId, String name, String genericName, String exec, boolean execTerminal, boolean visible, String iconPath) {
		this.appId = appId;
		this.name = name;
		this.genericName = genericName;
		this.exec = exec;
		this.execTerminal = execTerminal;
		this.visible = visible;
		this.icon = null;
		this.iconPath = iconPath;
	}
	
	@Override
	public String toString() {
		return "DesktopEntry [appId: " + appId + ", name: " + name + ", genericName: " + genericName + ", exec: '" + exec + "', execTerminal: " + execTerminal + ", visible: " + visible + ", icon: " + icon + "]";
	}
	
}

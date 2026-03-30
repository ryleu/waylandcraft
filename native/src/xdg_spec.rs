use std::path::PathBuf;
use freedesktop_desktop_entry::{
    DesktopEntry,
    get_languages_from_env, desktop_entries,
};
use cosmic_freedesktop_icons::lookup;

pub struct XDGSpecHelper {
    locales: Vec<String>,
    entries: Vec<DesktopEntry>,
}

pub struct RawDesktopEntry {
    pub app_id: String,
    pub name: Option<String>,
    pub generic_name: Option<String>,
    pub exec: Option<String>,
    pub exec_terminal: bool,
    pub visible: bool,
    pub icon_path: Option<String>,
}

impl XDGSpecHelper {
    pub fn init() -> Self {
        let locales = get_languages_from_env();
        let entries = desktop_entries(&locales);
        
        XDGSpecHelper {
            locales,
            entries
        }
    }

    fn to_raw(&self, entry: &DesktopEntry) -> RawDesktopEntry {
        let icon = self.resolve_icon_path(entry);
        let mut visible = true;
        if entry.hidden() || entry.no_display() {
            visible = false;
        } else if entry.only_show_in().is_some_and(|v| !v.is_empty()) {
            visible = false;
        }

        RawDesktopEntry {
            app_id: entry.id().into(),
            name: entry.name(&self.locales).map(|c| c.into_owned()),
            generic_name:
                entry.generic_name(&self.locales).map(|c| c.into_owned()),
            exec: entry.exec().map(|s| s.into()),
            exec_terminal: entry.terminal(),
            visible: visible,
            icon_path: icon.map(|p| p.into_os_string().into_string().unwrap()),
        }
    }

    pub fn load_entry(&self, path: PathBuf) -> Option<RawDesktopEntry> {
        let entry = DesktopEntry::from_path(path, Some(&self.locales)).ok()?;
        Some(self.to_raw(&entry))
    }

    pub fn get_raw_entries(&self) -> Vec<RawDesktopEntry> {
        self.entries.iter().map(|e| self.to_raw(&e)).collect()
    }

    fn resolve_icon_path(&self, entry: &DesktopEntry) -> Option<PathBuf> {
        let icon = entry.icon()?;

        // Absolute icon path
        let abspath = PathBuf::from(icon);
        if abspath.is_absolute() && abspath.is_file() {
            return Some(abspath);
        }

        // Lookup 128x128 icons
        let path = lookup(icon)
            .with_size(128)
            .with_scale(1)
            .find();
        if path.is_some() { return path }

        // Lookup 64x64 icons
        let path = lookup(icon)
            .with_size(64)
            .with_scale(1)
            .find();
        if path.is_some() { return path }

        // Fallback to any icon paths
        lookup(icon).find()
    }
}

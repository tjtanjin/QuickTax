package tk.taverncraft.quicktax.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import tk.taverncraft.quicktax.Main;

/**
 * DependencyLoadEvent ensures plugin soft dependencies are loaded properly.
 */
public class DependencyLoadEvent implements Listener {
    Main main;

    /**
     * Constructor for DependencyLoadEvent.
     */
    public DependencyLoadEvent(Main main) {
        this.main = main;
    }

    // required to bypass a spigot bug with softdepend
    @EventHandler
    private void onServerLoad(ServerLoadEvent e) {
        main.loadDependencies();
    }
}

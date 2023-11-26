package tk.taverncraft.quicktax.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import tk.taverncraft.quicktax.Main;
import tk.taverncraft.quicktax.leaderboard.SignHelper;
import tk.taverncraft.quicktax.utils.MessageManager;
import tk.taverncraft.quicktax.utils.ValidationManager;

/**
 * SignPlaceEvent checks for when a QuickTax sign is placed.
 */
public class SignPlaceEvent implements Listener {

    private final String signAddPerm = "quicktax.sign.add";
    Main main;
    ValidationManager validationManager;

    /**
     * Constructor for SignPlaceEvent.
     */
    public SignPlaceEvent(Main main) {
        this.main = main;
        this.validationManager = new ValidationManager(main);
    }

    @EventHandler
    private void onSignPlace(SignChangeEvent e) {
        Block block = e.getBlock();

        if (!block.getType().toString().contains("WALL_SIGN")) {
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return;
        }

        String line1 = e.getLine(0);
        String line2 = e.getLine(1);

        SignHelper signHelper = new SignHelper(main);
        if (!signHelper.isQuickTaxSign(line1, line2)) {
            return;
        }

        Player player = e.getPlayer();

        if (!player.hasPermission(signAddPerm)) {
            e.setCancelled(true);
            MessageManager.sendMessage(player, "no-quicktax-sign-add-permission");
            return;
        }

        if (!validationManager.doStoreData(player)) {
            e.setCancelled(true);
            return;
        }

        assert line2 != null;
        if (main.getStatsManager().isUpdating()) {
            signHelper.updateSign(e.getBlock(), null, Integer.parseInt(line2), "Updating...", "Updating...");
        } else {
            signHelper.updateSign(e.getBlock(), null, Integer.parseInt(line2), "Not updated", "Not updated");
        }
        MessageManager.sendMessage(player, "quicktax-sign-placed",
                new String[]{"%rank%"},
                new String[]{line2});
    }
}



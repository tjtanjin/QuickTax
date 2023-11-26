package tk.taverncraft.quicktax.leaderboard;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

/**
 * PlayerHeadHelper handles all the operations required to update a playerhead for leaderboard.
 */
public class PlayerHeadHelper {

    /**
     * Constructor for PlayerHeadHelper.
     */
    public PlayerHeadHelper() {}

    /**
     * Gets skull above block behind sign.
     * @param block block where sign is
     */
    public Block getSkullAboveBlockBehindSign(Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Directional)) {
            return null;
        }
        Directional directional = (Directional) data;
        Location locBehind = block.getRelative(directional.getFacing().getOppositeFace()).getLocation();
        Block skullBlock = locBehind.add(0, 1, 0).getBlock();
        BlockState state = skullBlock.getState();
        if (state instanceof Skull) {
            return skullBlock;
        } else {
            return null;
        }
    }

    /**
     * Gets skull above sign.
     * @param block block where sign is
     */
    public Block getSkullAboveSign(Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Directional)) {
            return null;
        }
        Location locUp = block.getRelative(BlockFace.UP).getLocation();
        Block skullBlock = locUp.getBlock();
        BlockState state = skullBlock.getState();
        if (state instanceof Skull) {
            return skullBlock;
        } else {
            return null;
        }
    }

    /**
     * Updates skulls with the playerhead of the player in leaderboard position.
     * @param player player to update skull with
     * @param skullAboveBlockBehindSign skull block above block behind sign
     * @param skullAboveSign skull block above sign
     */
    public void update(OfflinePlayer player, Block skullAboveBlockBehindSign, Block skullAboveSign) {
        if (skullAboveBlockBehindSign != null) {
            BlockState state = skullAboveBlockBehindSign.getState();
            Skull skull = (Skull) state;
            skull.setOwningPlayer(player);
            skull.update();
        }

        if (skullAboveSign != null) {
            BlockState state = skullAboveSign.getState();
            Skull skull = (Skull) state;
            skull.setOwningPlayer(player);
            skull.update();
        }
    }
}


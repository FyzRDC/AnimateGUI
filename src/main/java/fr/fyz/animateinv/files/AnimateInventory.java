package fr.fyz.animateinv.files;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.fyz.animateinv.MainAnimInventory;

public class AnimateInventory {

	private Player player;
	private ArrayList<Frame> frames;
	private int ticks;
	private boolean loop;
	private int size;
	private String title;
	private boolean closeAtEnd;
	private Inventory inv;
	private BukkitTask task;

	public AnimateInventory(String title, int size, Player player, ArrayList<Frame> frames, int ticks, boolean loop,
			boolean closeAtEnd) {
		this.player = player;
		this.closeAtEnd = closeAtEnd;
		this.frames = frames;
		this.ticks = ticks;
		this.loop = loop;
		this.title = title;
		this.size = size;
	}

	public void play() {
		inv = Bukkit.createInventory(player, size, title);
		updateInv(frames.get(0), true);
		player.openInventory(inv);

		task = new BukkitRunnable() {
			int actual_frame = 1;

			@Override
			public void run() {
				if (isCorrectInventory()) { // Check if inventory is valid and if the player is online
					Frame frame;
					if (canAccessFrame(actual_frame)) { // check if the list can get the frame from the int
						frame = frames.get(actual_frame);
					} else {
						frame = frames.get(actual_frame - 1);
					}
					updateInv(frame, false);
					actual_frame++;
					if (actual_frame >= frames.size()) { // Frames ended.
						if (loop) {
							actual_frame = 0; // Loop
						} else {
							cancel();
							if (closeAtEnd) {
								close();
							}
						}
					}
				} else {
					cancel();
					close();
				}

			}
		}.runTaskTimer(MainAnimInventory.getInstance(), ticks, ticks);
	}

	private boolean isCorrectInventory() {
		Inventory actual = player.getOpenInventory().getTopInventory();
		if (player != null && player.isOnline() && player.getOpenInventory().getTopInventory() != null
				&& actual.getTitle().equals(inv.getTitle())) {
			return true;
		}
		return false;
	}

	private void updateInv(Frame f, boolean firstFrame) {
		if (!firstFrame) {
			player.getOpenInventory().getTopInventory().clear();
		}
		f.getAllItems().keySet().forEach(e -> inv.setItem(e, f.getAllItems().get(e)));
		if (!firstFrame) {
			player.updateInventory();
		}
	}

	private void close() {
		task.cancel();
		if (player.isOnline()) {
			player.closeInventory();
		}
	}

	private boolean canAccessFrame(int i) {
		return i < frames.size();
	}

}

package to.joe.j2mc.iptracker.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import to.joe.j2mc.core.J2MC_Core;
import to.joe.j2mc.core.J2MC_Manager;
import to.joe.j2mc.core.command.MasterCommand;
import to.joe.j2mc.iptracker.J2MC_IPtracker;

public class BanIPCommand extends MasterCommand {

    public BanIPCommand(J2MC_IPtracker iptracker) {
        super(iptracker);
    }

    @Override
    public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "/ban-ip ip reason");
            return;
        }
        if (((J2MC_IPtracker) this.plugin).isIP(args[0])) {
            final String reason = J2MC_Core.combineSplit(1, args, " ");
            J2MC_Manager.getCore().adminAndLog(ChatColor.RED + "Banning " + args[0] + " by " + sender.getName() + ": " + reason);
            ((J2MC_IPtracker) this.plugin).banIP(args[0], reason, sender.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Not a valid IP");
        }
    }

}

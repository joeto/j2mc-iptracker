package to.joe.j2mc.iptracker.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import to.joe.j2mc.core.J2MC_Core;
import to.joe.j2mc.core.J2MC_Manager;
import to.joe.j2mc.core.command.MasterCommand;
import to.joe.j2mc.core.event.MessageEvent;
import to.joe.j2mc.iptracker.J2MC_IPtracker;

public class StompCommand extends MasterCommand<J2MC_IPtracker> {

    public StompCommand(J2MC_IPtracker iptracker) {
        super(iptracker);
    }

    @Override
    public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /stomp <playername> <reason>");
            sender.sendMessage(ChatColor.RED + "       reason can have spaces in it");
            return;
        }
        final String target = args[0];

        ResultSet rs = null;
        String ip = null;
        try {
            final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT IP FROM `alias` WHERE name=? order by Time desc limit 1");
            ps.setString(1, args[0]);
            rs = ps.executeQuery();
        } catch (final SQLException e) {
            this.plugin.getLogger().warning(ChatColor.RED + "Unable to load user/ip from MySQL. Oh hell");
            this.plugin.getLogger().log(Level.SEVERE, "SQL Exception:", e);
        }

        try {
            if (rs.next()) {
                ip = rs.getString("IP");
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger().warning(ChatColor.RED + "Unable to load user/ip from MySQL. Oh hell");
            this.plugin.getLogger().log(Level.SEVERE, "SQL Exception:", ex);
        }
        if ((ip == null) || (ip.length() < 8)) {
            sender.sendMessage(ChatColor.RED + "No IP matches on that username, could not stomp.");
            return;
        } else {
            final String reason = J2MC_Core.combineSplit(1, args, " ");
            ((J2MC_IPtracker) this.plugin).banIP(ip, reason, sender.getName());
            J2MC_Manager.getCore().adminAndLog(ChatColor.RED + "Banning " + ip + " by " + sender.getName());
            final String reasonprocessed = reason.replace(":", "/OMGREPLACEWITHCOLON\\");
            final String toSend = (sender.getName() + ":" + target + ":" + reasonprocessed + ":" + sender.getName());
            final HashSet<String> targets = new HashSet<String>();
            targets.add("NEWADDBAN");
            this.plugin.getServer().getPluginManager().callEvent(new MessageEvent(targets, toSend));
        }

    }

}

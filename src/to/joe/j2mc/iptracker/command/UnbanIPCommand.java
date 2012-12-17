package to.joe.j2mc.iptracker.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import to.joe.j2mc.core.J2MC_Manager;
import to.joe.j2mc.core.command.MasterCommand;
import to.joe.j2mc.iptracker.J2MC_IPtracker;

public class UnbanIPCommand extends MasterCommand<J2MC_IPtracker> {

    public UnbanIPCommand(J2MC_IPtracker iptracker) {
        super(iptracker);
    }

    @Override
    public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "Need a name or IP");
            return;
        }
        String ip;
        if (((J2MC_IPtracker) this.plugin).isIP(args[0])) {
            ip = args[0];
        } else {
            ResultSet rs = null;
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
                } else {
                    sender.sendMessage(ChatColor.RED + "No ip matches on that username D:");
                    return;
                }
            } catch (final SQLException ex) {
                this.plugin.getLogger().warning(ChatColor.RED + "Unable to load user/ip from MySQL. Oh hell");
                this.plugin.getLogger().log(Level.SEVERE, "SQL Exception:", ex);
                sender.sendMessage(ChatColor.RED + "FAILURE. Report to senior staff");
                return;
            }
        }
        ((J2MC_IPtracker) this.plugin).unbanIP(ip);
        J2MC_Manager.getCore().adminAndLog(ChatColor.RED + "Unbanning " + ip + " by " + sender.getName());
    }

}

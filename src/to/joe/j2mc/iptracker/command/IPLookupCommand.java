package to.joe.j2mc.iptracker.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import to.joe.j2mc.core.J2MC_Manager;
import to.joe.j2mc.core.command.MasterCommand;
import to.joe.j2mc.iptracker.J2MC_IPtracker;

public class IPLookupCommand extends MasterCommand {

    public IPLookupCommand(J2MC_IPtracker iptracker) {
        super(iptracker);
    }

    @Override
    public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
        final String aliasdb = "alias";
        ResultSet rs = null;
        String result = "";
        try {
            final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT IP FROM " + aliasdb + " WHERE name=? order by Time desc limit 1");
            ps.setString(1, args[0]);
            rs = ps.executeQuery();
        } catch (final SQLException e) {
            this.plugin.getLogger().warning(ChatColor.RED + "Unable to load user/ip from MySQL. Oh hell");
            this.plugin.getLogger().log(Level.SEVERE, "SQL Exception:", e);
        }
        try {
            if (rs.next()) {
                result = rs.getString("IP");
            }
        } catch (final SQLException ex) {
            this.plugin.getLogger().warning(ChatColor.RED + "Unable to load user/ip from MySQL. Oh hell");
            this.plugin.getLogger().log(Level.SEVERE, "SQL Exception:", ex);
        }
        if (!result.isEmpty()) {
            sender.sendMessage(ChatColor.AQUA + "IPLookup on " + ChatColor.WHITE + args[0] + ChatColor.AQUA + "\'s last IP: " + ChatColor.WHITE + result);
            final HashMap<String, Long> nameDates = new HashMap<String, Long>();
            ResultSet resultset = null;
            try {
                final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `Name`, `Time` FROM " + aliasdb + " WHERE IP=? ORDER BY `Time` DESC LIMIT 5");
                ps.setString(1, result);
                resultset = ps.executeQuery();
                while (resultset.next()) {
                    nameDates.put(resultset.getString("Name"), resultset.getTimestamp("Time").getTime());
                }
                if (!nameDates.isEmpty()) {
                    for (final String key : nameDates.keySet()) {
                        if (!key.isEmpty() && (key.toLowerCase() != "null")) {
                            final Long time = nameDates.get(key);
                            final Date date = new Date(time);
                            sender.sendMessage(ChatColor.AQUA + key + " : " + ChatColor.BLUE + date);
                        }
                    }
                }
            } catch (final SQLException e) {
                this.plugin.getLogger().warning(ChatColor.RED + "Unable to load user/ip from MySQL. Oh hell");
                this.plugin.getLogger().log(Level.SEVERE, "SQL Exception:", e);

            }
        } else {
            sender.sendMessage(ChatColor.AQUA + "Could not find any matches.");
        }
    }
}

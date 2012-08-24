package to.joe.j2mc.iptracker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import to.joe.j2mc.core.J2MC_Manager;
import to.joe.j2mc.iptracker.command.BanIPCommand;
import to.joe.j2mc.iptracker.command.IPLookupCommand;
import to.joe.j2mc.iptracker.command.StompCommand;
import to.joe.j2mc.iptracker.command.UnbanIPCommand;

public class J2MC_IPtracker extends JavaPlugin implements Listener {

    private static final Pattern ipattern = Pattern.compile("(?<=(^|[(\\p{Space}|\\p{Punct})]))((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])(?=([(\\p{Space}|\\p{Punct})]|$))");

    public void banIP(String ip, String reason, String admin) {
        try {
            final PreparedStatement ban = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO ipbans (`ip`,`reason`,`admin`,`timeofban`) VALUES(?,?,?,?)");
            ban.setString(1, ip);
            ban.setString(2, reason);
            ban.setString(3, admin);
            ban.setLong(4, new Date().getTime() / 1000);
            ban.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isIP(String string) {
        final Matcher matcher = J2MC_IPtracker.ipattern.matcher(string);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("IP tracker module disabled");
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("iplookup").setExecutor(new IPLookupCommand(this));
        this.getCommand("stomp").setExecutor(new StompCommand(this));
        this.getCommand("ban-ip").setExecutor(new BanIPCommand(this));
        this.getCommand("pardon-ip").setExecutor(new UnbanIPCommand(this));

        this.getLogger().info("IP tracker module enabled");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    //Purely to get started early ^_^
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        final String ip = event.getAddress().getHostAddress();
        final String name = event.getName();
        try {
            final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT * FROM alias WHERE Name=? AND IP=?");
            ps.setString(1, name);
            ps.setString(2, ip);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("Logins");
                count++;
                final PreparedStatement increment = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE alias SET logins=?, Time=now() WHERE Name=? AND IP=?");
                increment.setInt(1, count);
                increment.setString(2, name);
                increment.setString(3, ip);
                increment.executeUpdate();
            } else {
                final PreparedStatement insertIntoAlias = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO alias (`Name`,`IP`,`Time`,`Logins`) VALUES(?,?,now(),?)");
                insertIntoAlias.setString(1, name);
                insertIntoAlias.setString(2, ip);
                insertIntoAlias.setInt(3, 1);
                insertIntoAlias.executeUpdate();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        try {
            final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT * FROM ipbans WHERE `ip`=? AND `unbanned`=0");
            ps.setString(1, ip);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                event.disallow(Result.KICK_BANNED, "IP Banned!");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void unbanIP(String ip) {
        try {
            final PreparedStatement unban = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE ipbans SET unbanned=1 WHERE ip=?");
            unban.setString(1, ip);
            unban.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

}

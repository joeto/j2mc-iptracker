package to.joe.j2mc.iptracker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import to.joe.j2mc.core.J2MC_Manager;

public class J2MC_IPtracker extends JavaPlugin implements Listener{

    @Override
    public void onEnable(){
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("IP tracker module enabled");
    }
    
    @Override
    public void onDisable(){
        this.getLogger().info("IP tracker module disabled");
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerPreLoginEvent event){
        final String ip = event.getAddress().getHostAddress();
        final String name = event.getName();
        try{
            PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT * FROM alias WHERE Name=? AND IP=?");
            ps.setString(1, name);
            ps.setString(2, ip);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int count = rs.getInt("Logins");
                count++;
                PreparedStatement increment = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE alias SET logins=?, Time=now() WHERE Name=? AND IP=?");
                increment.setInt(1, count);
                increment.setString(2, name);
                increment.setString(3, ip);
                increment.executeUpdate();
            }else{
                PreparedStatement insertIntoAlias = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO alias (`Name`,`IP`,`Time`,`Logins`) VALUES(?,?,now(),?)");
                insertIntoAlias.setString(1, name);
                insertIntoAlias.setString(2, ip);
                insertIntoAlias.setInt(3, 1);
                insertIntoAlias.executeUpdate();
            }
        }catch(SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}

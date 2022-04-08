package cn.LTCraft.core.game;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Created by Angel、 on 2022/4/8 21:44
 * 针对怪物管理
 */
public class TargetOnlyMobsManager {
    private static TargetOnlyMobsManager mobsForPlayersManager = null;

    public static TargetOnlyMobsManager getInstance() {
        if (mobsForPlayersManager == null){
            mobsForPlayersManager = new TargetOnlyMobsManager();
        }
        return mobsForPlayersManager;
    }

    /**
     * 仅限目标怪物
     */
    public static final Map<ActiveMob, Player> targetOnlyMobs = new HashMap<>();
    private TargetOnlyMobsManager(){

    }

    public void add(ActiveMob activeMob, Player player){
        targetOnlyMobs.put(activeMob, player);
    }

    /**
     * tick
     */
    public void doTick(){
        Set<Map.Entry<ActiveMob, Player>> entries = targetOnlyMobs.entrySet();
        for (Iterator<Map.Entry<ActiveMob, Player>> iterator = entries.iterator();iterator.hasNext();){
            Map.Entry<ActiveMob, Player> next = iterator.next();
            Player player = next.getValue();
            ActiveMob activeMob = next.getKey();
            if (!player.isOnline() || !player.getWorld().equals(activeMob.getEntity().getBukkitEntity().getWorld()) || activeMob.getEntity().getBukkitEntity().getLocation().distance(player.getLocation()) > 32) {
                activeMob.getEntity().remove();
                activeMob.setDead();
                iterator.remove();
                return;
            }
            ActiveMob.ThreatTable threatTable = activeMob.getThreatTable();
            AbstractEntity topThreatHolder = threatTable.getTopThreatHolder();
            if(topThreatHolder == null || topThreatHolder.getBukkitEntity() != player){
                threatTable.Taunt(BukkitAdapter.adapt(player));
            }
        }
    }

    /**
     * 获取一个怪物针对目标
     * @return 如果返回null说明不是针对怪物
     */
    public static AbstractEntity getMobTargetOnly(Entity entity){
        Optional<ActiveMob> activeMob = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId());
        if (activeMob.isPresent()){
            ActiveMob am = activeMob.get();
            if (targetOnlyMobs.containsKey(am)){
                return am.getThreatTable().getTopThreatHolder();
            }
            return null;
        }
        return null;
    }
}

package cn.LTCraft.core.entityClass;

import cn.LTCraft.core.dataBase.bean.PlayerInfo;
import cn.LTCraft.core.dataBase.mappers.PlayerMapper;
import cn.LTCraft.core.dataBase.SQLQueue;
import cn.LTCraft.core.Main;
import cn.LTCraft.core.utils.PlayerUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.lumine.utils.config.file.YamlConfiguration;
import io.lumine.xikage.mythicmobs.io.MythicConfig;
import org.apache.ibatis.session.SqlSession;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlayerConfig {
    private static final Map<String, PlayerConfig> configMap = new HashMap<>();
//    private static final Table<String, String, Integer> counter = HashBasedTable.create();
    private static final Map<String, Map<String, Integer>> counter = new HashMap<>();

    /**
     * 获取玩家配置文件
     * @param player 玩家
     * @return 配置文件
     */
    public static PlayerConfig getPlayerConfig(Player player){
        return configMap.get(player.getName());
    }

    /**
     * 离线玩家只能获取Config
     * 并且需要你手动保存
     * @param playerName 玩家名字
     * @return Config
     */
    public static YamlConfiguration getPlayerConfig(String playerName){
        if (configMap.containsKey(playerName)) {
            return configMap.get(playerName).getConfig();
        }
        File file = new File(Main.getInstance().getDataFolder()  + File.separator + "playerData" + File.separator + playerName.toLowerCase() + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 保存配置文件
     * @param playerName 玩家姓名
     * @param config 配置文件
     */
    public static void savePlayerConfig(String playerName, YamlConfiguration config){
        File file = new File(Main.getInstance().getDataFolder()  + File.separator + "playerData" + File.separator + playerName.toLowerCase() + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private final YamlConfiguration config;
    private final Player owner;
    private final File file;
    private final Main plugin;
    private MythicConfig classAttConfig;
    private PlayerInfo playerInfo;
    public PlayerConfig(Player player){
        plugin = Main.getInstance();
        owner = player;
        file = new File(plugin.getDataFolder()  + File.separator + "playerData" + File.separator + player.getName().toLowerCase() + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        configMap.put(player.getName(), this);
        updateClassAttConfig();
        SqlSession sqlSession = Main.getInstance().getSQLServer().getSqlSessionFactory().openSession();
        PlayerMapper mapper = sqlSession.getMapper(PlayerMapper.class);
        SQLQueue select = new SQLQueue(sqlSession, () -> mapper.selectByName(player.getName().toLowerCase()), sqlQueue -> {
            if (sqlQueue.getStatus() == SQLQueue.STATUS.DONE){
                playerInfo = sqlQueue.getResult();
                if (playerInfo == null){
                    playerInfo = new PlayerInfo(player.getName().toLowerCase());
                }
            }else {
                player.kickPlayer("§c查询信息失败！");
            }
        });
        plugin.getSQLManage().getQueue().add(select);
    }
    public void updateClassAttConfig(){
        classAttConfig = getMythicConfig("classAtt." + PlayerUtils.getAccount(owner));
        try {
            classAttConfig.getKeys("skills");
            config.save(file);
        }catch (Exception e){
            classAttConfig.set("skills", new HashMap<>());
        }
    }
    public void updateClassAttConfig(int id){
        classAttConfig = getMythicConfig("classAtt." + id);
        try {
            classAttConfig.getKeys("skills");
            config.save(file);
        }catch (Exception e){
            classAttConfig.set("skills", new HashMap<>());
        }
    }
    public void save(){
        try {
            if (playerInfo != null) {
                playerInfo.setLastPlayTime(new Date());
                playerInfo.commitChanges();
            }
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        return file;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public static Map<String, PlayerConfig> getConfigMap() {
        return configMap;
    }

    public MythicConfig getMythicConfig(String path){
        return new MythicConfig(path, config);
    }

    public MythicConfig getClassAttConfig() {
        return classAttConfig;
    }

    public Main getPlugin() {
        return plugin;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public void setClassAttConfig(MythicConfig classAttConfig) {
        this.classAttConfig = classAttConfig;
    }

    /**
     * 获取玩家的计数器
     * @param player 玩家对象
     * @return 计数器 Map
     */
    public static Map<String, Integer> getCounter(Player player) {
        return getCounter(player.getName());
    }
    public static Map<String, Integer> getCounter(String playerName) {
        Map<String, Integer> map = counter.computeIfAbsent(playerName, k -> new HashMap<>());
        return map;
    }

    public static Map<String, Map<String, Integer>> getCounter() {
        return counter;
    }
}

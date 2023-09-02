package fr.thefirstalpha.mastersdc;

import fr.thefirstalpha.mastersdc.chest.BetaChest;
import fr.thefirstalpha.mastersdc.chest.AlphaChest;
import fr.thefirstalpha.mastersdc.chest.MasterChest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database {
    public static Connection conn;

    public Database() {
        String url = "jdbc:sqlite:" + MasterSDC.instance.getDataFolder().getAbsolutePath() + "/sdc.db";
        MasterSDC.log.info(url);
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                try {
                    conn.createStatement().execute("CREATE TABLE Chests(id INTEGER PRIMARY KEY AUTOINCREMENT, loc_x INT, loc_y INT, loc_z INT, world TEXT, alpha BOOL DEFAULT false, name TEXT, link INT, owner TEXT,UNIQUE (loc_x,loc_y,loc_z,world));");
                } catch (Exception ignored) {
                }
                try {
                    conn.createStatement().execute("CREATE TABLE Filters(id INTEGER, material TEXT, UNIQUE(id,material));");
                } catch (Exception ignored) {
                }
                try {
                    conn.createStatement().execute("CREATE TABLE Shares(id INTEGER, player TEXT, UNIQUE(id,player));");
                } catch (Exception ignored) {
                }
                conn.setNetworkTimeout(command -> {
                    try {
                        conn.close();
                        conn = DriverManager.getConnection(url);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }, 500);
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MasterChest getChest(long id) {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT Chests.*,group_concat(Filters.material) AS material FROM Chests LEFT JOIN Filters ON Chests.id = Filters.id WHERE Chests.id = ? GROUP BY Chests.id;");
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next())
                return extractChest(result);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static boolean removeChest(long id) {
        try {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM Chests WHERE id = ?;DELETE FROM Filters WHERE id = ?");
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static AlphaChest getAlphaChest(String name, UUID owner) {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Chests WHERE name = ? AND owner = ? AND alpha = true LIMIT 1;");
            statement.setString(1, name);
            statement.setString(2, owner.toString());
            ResultSet result = statement.executeQuery();
            if (result.next())
                return (AlphaChest) extractChest(result);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static List<AlphaChest> getAlphaChest(UUID owner) {
        List<AlphaChest> chests = new ArrayList<>();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Chests WHERE owner = ? AND alpha = true;");
            statement.setString(1, owner.toString());
            ResultSet result = statement.executeQuery();
            while (result.next())
                chests.add((AlphaChest) extractChest(result));

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return chests;
    }

    public static MasterChest getChest(Location location) {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT Chests.*,group_concat(Filters.material) AS material FROM Chests LEFT JOIN Filters ON Chests.id = Filters.id WHERE Chests.loc_x = ? AND Chests.loc_y = ? AND Chests.loc_z = ? AND Chests.world = ? GROUP BY Chests.id LIMIT 1;");
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.setString(4, location.getWorld().getUID().toString());
            ResultSet result = statement.executeQuery();
            if (result.next())
                return extractChest(result);

        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static List<BetaChest> getLinkedBetaChest(long chest_id, Material type) {
        List<BetaChest> chests = new ArrayList<>();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT Chests.*,group_concat(Filters.material) AS material FROM Chests INNER JOIN Filters ON Chests.id = Filters.id WHERE Chests.alpha = 0 AND Chests.link = ? AND Filters.material = ? GROUP BY Chests.id");
            statement.setLong(1, chest_id);
            statement.setString(2, type.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                do {
                    chests.add((BetaChest) extractChest(result));
                } while (result.next());
            }
            statement = conn.prepareStatement("SELECT *,null AS material FROM Chests WHERE alpha = 0 AND link = ? AND Chests.id NOT IN (SELECT Filters.id FROM Filters);");
            statement.setLong(1, chest_id);
            result = statement.executeQuery();
            while (result.next()) {
                chests.add((BetaChest) extractChest(result));
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return chests;
    }

    public static MasterChest extractChest(ResultSet result) throws Exception {
        boolean alpha = result.getBoolean("alpha");
        long id = result.getLong("id");
        UUID uid = UUID.fromString(result.getString("world"));
        UUID owner = UUID.fromString(result.getString("owner"));
        Location loc = new Location(Bukkit.getWorld(uid), result.getInt("loc_x"), result.getInt("loc_y"), result.getInt("loc_z"));
        if (alpha) {
            String name = result.getString("name");
            return new AlphaChest(id, owner, name, loc);
        } else {
            long alphaChest = result.getLong("link");
            Set<Material> filter = new HashSet<>();
            if (result.findColumn("material") > 0 && result.getString("material") != null)
                filter = Arrays.stream(result.getString("material").split(",")).map(Material::valueOf).collect(Collectors.toSet());
            return new BetaChest(id, owner, alphaChest, filter, loc);
        }
    }

    public static MasterChest createBetaChest(Location location, Set<Material> filter, long link, UUID owner) {
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO Chests(loc_x,loc_y,loc_z,world,link,owner) VALUES(?,?,?,?,?,?);");
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.setString(4, location.getWorld().getUID().toString());
            statement.setLong(5, link);
            statement.setString(6, owner.toString());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return null;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                PreparedStatement statement1 = conn.prepareStatement("REPLACE INTO Filters(id,material) VALUES(?,?);");
                for (Material material : filter) {
                    statement1.setLong(1, id);
                    statement1.setString(2, material.toString());
                    statement1.addBatch();
                }
                statement1.executeBatch();
                return new BetaChest(id, owner, link, filter, location);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addFilter(long chest_id, Set<Material> filter) {
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO Filters(id,material) VALUES(?,?);");
            for (Material material : filter) {
                statement.setLong(1, chest_id);
                statement.setString(2, material.toString());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeFilter(long chest_id, Set<Material> filter) {
        try {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM Filters WHERE id = ? AND material = ?;");
            for (Material material : filter) {
                statement.setLong(1, chest_id);
                statement.setString(2, material.toString());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static MasterChest createAlphaChest(Location location, UUID owner, String name) {
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO Chests(loc_x,loc_y,loc_z,world,owner,alpha,name) VALUES(?,?,?,?,?,?,?);");
            statement.setInt(1, location.getBlockX());
            statement.setInt(2, location.getBlockY());
            statement.setInt(3, location.getBlockZ());
            statement.setString(4, location.getWorld().getUID().toString());
            statement.setString(5, owner.toString());
            statement.setBoolean(6, true);
            statement.setString(7, name);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return null;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return new AlphaChest(generatedKeys.getLong(1), owner, name, location);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean addShare(long chest_id, UUID player) {
        try {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO Shares(id,player) VALUES(?,?);");
            statement.setLong(1, chest_id);
            statement.setString(2, player.toString());
            int affectedRows = statement.executeUpdate();
            return affectedRows != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeShare(long chest_id, UUID player) {
        try {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM Shares WHERE id = ? AND player = ?;");
            statement.setLong(1, chest_id);
            statement.setString(2, player.toString());
            int affectedRows = statement.executeUpdate();
            return affectedRows != 0;
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static List<String> getShare(long chest_id) {
        List<String> share = new ArrayList<>();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM Shares WHERE id = ?;");
            statement.setLong(1, chest_id);
            ResultSet result = statement.executeQuery();
            while (result.next())
                share.add(result.getString("player"));
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return share;
    }


}

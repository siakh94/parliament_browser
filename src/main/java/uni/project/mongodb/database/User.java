package uni.project.mongodb.database;

import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Website User.
 */
public class User {
    private final String username;
    private final List<String> permissions;
    public User(String username, List<String> permissions) {
        this.username = username;
        this.permissions = permissions;
    }

    public User(Document document) {
        this.username = document.getString("username");
        this.permissions = document.getList("permissions", String.class);
    }

    public User(JSONObject obj) {
        this.username = obj.getString("username");
        ArrayList<String> permissions = new ArrayList<>();
        this.permissions = new ArrayList<>();
        obj.getJSONArray("permissions").iterator().forEachRemaining((p) -> this.permissions.add((String) p));
    }

    public String getUsername() {
        return username;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Checks whether the user has a permission.
     * @param permission Permission to check for. One of "admin", "speech", "protocol", "template".
     * @return Whether the permission was present.
     * @throws Exception If permission was not present.
     */
    public boolean hasPermission(String permission) throws Exception {
        if (this.permissions.contains(permission)) {
            return true;
        } else {
            throw new Exception("unauthorized user");
        }
    }

    /**
     * Constructs key value map to be used with freemarker templates representing user.
     * @return Key value map.
     */
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("loggedIn", true);
        hm.put("username", this.username);
        for (String p : new String[]{"admin", "protocol", "speech", "template"}) {
            if (this.permissions.contains(p)) hm.put(p, true);
            else hm.put(p, false);
        }
        return hm;
    }
}

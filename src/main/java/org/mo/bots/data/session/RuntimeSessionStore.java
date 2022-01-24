package org.mo.bots.data.session;

import java.util.HashMap;
import java.util.Map;

public class RuntimeSessionStore implements SessionStore {

    private Map<String, Map<String, Object>> store = new HashMap<>();

    @Override
    public Map<String, Object> get(String userId) {
        return store.get(userId);
    }

    @Override
    public void save(String userId, Map<String, Object> data) {
        store.put(userId, data);
    }
}

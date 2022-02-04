package org.mo.bots.data.session;

import java.util.HashMap;
import java.util.Map;

public class RuntimeSessionStore implements SessionStore {

    private Map<String, Map<String, Object>> store = new HashMap<>();

    @Override
    public Map<String, Object> get(String userId) {
        Map<String, Object> session = store.get(userId);
        if(session == null) {
            session = new HashMap<>();
        }
        return session;
    }

    @Override
    public void save(String userId, Map<String, Object> data) {
        store.put(userId, data);
    }

    @Override
    public void putValue(String userId, String key, Object value) {
        Map<String, Object> session = get(userId);
        session.put(key, value);
        save(userId, session);
    }

    @Override
    public Object getValue(String userId, String key) {
        Map<String, Object> session = get(userId);
        return session.get(key);
    }
}

package org.mo.bots.data.session;

import java.util.Map;

public interface SessionStore {

    Map<String, Object> get(String userId);

    void save(String userId, Map<String, Object> data);

    void putValue(String userId, String key, Object value);

    Object getValue(String userId, String key);

}

package com.opcon.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import com.opcon.database.ContactBase;
import com.opcon.firebaseclient.PresenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by Mahmut Taşkiran on 28/10/2016.
 */

public class Component {

    private static final String PREFIX = "opcon_";

    public static final int SID = 573;
    public static final int LID = 225;

    private SparseBooleanArray sba;
    private SparseArrayCompat<Double> sda;
    private SparseArrayCompat<Long> sla;
    private SparseArrayCompat<String> ssa;
    private SparseIntArray sia;
    private SparseArrayCompat<Component> sca;
    private HashMap<String, String> str;

    private boolean isDigit(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void put(JSONObject json) {
        parse(this, json);
    }

    public void put(Map<String, Object> map) {
        parse(this, new JSONObject(map));
    }

    public void put(String key, Object o) {
        if (isDigit(key)) {
            put(Integer.parseInt(key), o);
        } else {
            if (o != null)
                putParam(key, o.toString());
        }
    }

    public void put(int key, Object o) {
        if (o instanceof Integer) {
            putParam(key, (Integer) o);
        } else if (o instanceof Long) {
            putParam(key, (Long) o);
        } else if (o instanceof Double) {
            putParam(key, (Double) o);
        } else if (o instanceof String) {
            putParam(key, (String) o);
        } else if (o instanceof Boolean) {
            putParam(key, (Boolean) o);
        } else if (o instanceof Component) {
            putParam(key, (Component) o);
        }
    }

    private void putParam(int def, boolean value) {
        if (sba == null)
            sba = new SparseBooleanArray();
        sba.put(def, value);
    }

    private void putParam(int def, Component component) {
        if (sca ==null) {
            sca = new SparseArrayCompat<>();
        }
        sca.put(def, component);
    }

    private void putParam(String key, String value){
        if (str == null) {
            str = new HashMap<>();
        }
        str.put(key, value);
    }


    private void putParam(int def, int value) {
        if (sia == null)
            sia = new SparseIntArray();
        sia.put(def, value);
    }


    private void putParam(int def, double value) {
        if (sda == null)
            sda = new SparseArrayCompat<>();
        sda.put(def, value);
    }


    private void putParam(int def, String value) {
        if (ssa == null)
            ssa = new SparseArrayCompat<>();
        ssa.put(def, value);
    }


    private void putParam(int def, long value) {
        if (sla == null) {
            sla = new SparseArrayCompat<>();
        }
        sla.put(def, value);
    }


    public int getInt(int def) {
        if (sia == null || sia.indexOfKey(def) < 0)
            return (int) getLong(def);
        return sia.get(def);
    }

    public boolean getBoolean(int def) {
        return sba != null && sba.get(def);
    }


    public String getString(int def) {
        if (ssa == null) {
            return null;
        }
        return ssa.get(def);
    }

    public String getString(String key) {
        if (str != null) {
            return str.get(key);
        }
        return null;
    }


    public double getDouble(int def) {
        if (sda == null ||
            sda.indexOfKey(def) < 0)
            return 0;
        return sda.get(def);
    }


    public Component getComponent(int key) {
        if(sca == null) {
            return null;
        }
        return sca.get(key);
    }


    public long getLong(int def) {
        if (sla == null || sla.indexOfKey(def) < 0)
            return -1;
        return sla.get(def);
    }


    public Map<String, Object> toPureMap() {
        Map<String, Object> map = new HashMap<>();
        if (sba != null)
            for (int i = 0; i < sba.size(); i++) {
                int key = sba.keyAt(i);
                map.put(String.valueOf(key), sba.get(key));
            }
        if (sda != null)
            for (int i = 0; i < sda.size(); i++) {
                int key = sda.keyAt(i);
                map.put(String.valueOf(key), sda.get(key));
            }
        if (sia != null)
            for (int i = 0; i < sia.size(); i++) {
                int key = sia.keyAt(i);
                map.put(String.valueOf(key), sia.get(key));
            }
        if (ssa != null)
            for (int i = 0; i < ssa.size(); i++) {
                int key = ssa.keyAt(i);
                map.put(String.valueOf(key), ssa.get(key));
            }
        if (sla != null)
            for (int i = 0; i < sla.size(); i++) {
                int key = sla.keyAt(i);
                map.put(String.valueOf(key), sla.get(key));
            }
        if (sca != null) {
            for (int i = 0; i < sca.size(); i++) {
                int key = sca.keyAt(i);
                map.put(String.valueOf(key), sca.get(key).toPureMap());
            }
        }

        if (str != null) {
            Set<Map.Entry<String, String>> entries = str.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        return map;
    }

    public boolean isSenderAmI() {
        return getSender().equals(PresenceManager.uid());
    }

    public boolean received() {
        return getReceiver().equals(PresenceManager.uid());
    }

    public String getRelationship() {
        if (isSenderAmI()) {
            return getReceiver();
        }
        return getSender();
    }

    public String getRelationshipAvatar(Context context) {
        return ContactBase.Utils.getValidAvatar(context, getRelationship());
    }

    public String getRelationshipName(Context context) {
        return ContactBase.Utils.getName(context, getRelationship());
    }


    public JSONObject toPureJson() {
        return tj(null);
    }

    public JSONObject toJson() {
        return tj(PREFIX);
    }


    private JSONObject tj(@Nullable String prefix) {
        JSONObject jo= new JSONObject();

        if (prefix == null) {
            prefix= "";
        }

        try {
            if (sba != null)
                for (int i = 0; i < sba.size(); i++) {
                    int key = sba.keyAt(i);
                    jo.put(prefix + String.valueOf(key), sba.get(key));
                }

            if (sda != null)
                for (int i = 0; i < sda.size(); i++) {
                    int key = sda.keyAt(i);
                    jo.put(prefix + String.valueOf(key), sda.get(key));
                }
            if (sia != null)
                for (int i = 0; i < sia.size(); i++) {
                    int key = sia.keyAt(i);
                    jo.put(prefix + String.valueOf(key), sia.get(key));
                }
            if (ssa != null)
                for (int i = 0; i < ssa.size(); i++) {
                    int key = ssa.keyAt(i);
                    jo.put(prefix + String.valueOf(key), ssa.get(key));
                }
            if (sla != null)
                for (int i = 0; i < sla.size(); i++) {
                    int key = sla.keyAt(i);
                    jo.put(prefix + String.valueOf(key), sla.get(key));
                }
            if (sca != null) {
                for (int i = 0; i < sca.size(); i++) {
                    int key = sca.keyAt(i);
                    jo.put(prefix + String.valueOf(key), sca.get(key).tj(prefix.isEmpty() ? null : prefix));
                }
            }

            if (str != null) {
                Set<Map.Entry<String, String>> entries = str.entrySet();
                for (Map.Entry<String, String> entry : entries)
                    jo.put(prefix + entry.getKey(), entry.getValue());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (sba != null)
            for (int i = 0; i < sba.size(); i++) {
                int key = sba.keyAt(i);
                map.put(PREFIX + String.valueOf(key), sba.get(key));
            }
        if (sda != null)
            for (int i = 0; i < sda.size(); i++) {
                int key = sda.keyAt(i);
                map.put(PREFIX + String.valueOf(key), sda.get(key));
            }
        if (sia != null)
            for (int i = 0; i < sia.size(); i++) {
                int key = sia.keyAt(i);
                map.put(PREFIX + String.valueOf(key), sia.get(key));
            }
        if (ssa != null)
            for (int i = 0; i < ssa.size(); i++) {
                int key = ssa.keyAt(i);
                map.put(PREFIX + String.valueOf(key), ssa.get(key));
            }
        if (sla != null)
            for (int i = 0; i < sla.size(); i++) {
                int key = sla.keyAt(i);
                map.put(PREFIX + String.valueOf(key), sla.get(key));
            }
        if (sca != null) {
            for (int i = 0; i < sca.size(); i++) {
                int key = sca.keyAt(i);
                map.put(PREFIX + String.valueOf(key), sca.get(key).toMap());
            }
        }

        if (str != null) {
            Set<Map.Entry<String, String>> entries = str.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                map.put(PREFIX + entry.getKey(), entry.getValue());
            }
        }

        return map;
    }

    public static <T extends Number> boolean isNotEmptyNumber(T number) {
        return number != null && !number.equals(0) && !number.equals(-1);
    }

    public void delete(int ...p) {
        for (int i : p) {
            delete(i);
        }
    }

    public void delete(int key) {
        if (sca != null) sca.delete(key);
        if (sba != null) sba.delete(key);
        if (sda != null) sda.delete(key);
        if (sia != null) sia.delete(key);
        if (ssa != null) ssa.delete(key);
        if (sla != null) sla.delete(key);
    }

    public void delete(String key) {
        if (str != null) {
            str.remove(key);
        }
    }

    public boolean isEmpty() {
        return  (sia == null || sia.size() == 0) &&
            (sba == null || sba.size() == 0) &&
            (sla == null || sla.size() == 0) &&
            (sda == null || sda.size() == 0) &&
            (ssa == null || ssa.size() == 0) &&
            (sca == null || sca.size() == 0) &&
            (str == null || str.size() == 0);
    }

    public int getTotalElement() {
        int total = 0;
        total += sia != null ? sia.size(): 0;
        total += sba != null ? sba.size(): 0;
        total += sla != null ? sla.size(): 0;
        total += sda != null ? sda.size(): 0;
        total += ssa != null ? ssa.size(): 0;
        total += sca != null ? sca.size(): 0;
        total += str != null ? str.size(): 0;
        return total;
    }

    public void setSender(String sender) {
        put("sender", sender);
    }

    public String getSender() {
        return getString("sender");
    }

    public void setReceiver(String receiver) {
        put("receiver", receiver);
    }

    public String getReceiver() {
        return getString("receiver");
    }

    public String getReceiverName(Context context) {
        return ContactBase.Utils.getName(context, getReceiver());
    }

    public String getSid() {
        return getString(SID);
    }

    public void setSid(String s) {
        putParam(SID, s);
    }

    public void setId(int uid) {
        put(LID, uid);
    }

    public int getId() {
        return getInt(LID);
    }

    @Override public String toString() {
        return toJson().toString();
    }

    @Override public int hashCode() {
        int id = getId();
        if (id != 0 && id != -1) {
            return id;
        } else {
            String sid = getSid();
            if (sid != null) {
                return sid.hashCode();
            }
        }
        return super.hashCode();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Component)) return false;
        Component that = (Component) o;
        String sid = getSid();
        int id = getId();
        return sid != null ? sid.equals(that.getSid()): id == that.getId();
    }

    private static <T extends Component> void parse(T instance, JSONObject json) {
        if ( json == null ) {
            throw new NullPointerException("json is null.");
        }
        Iterator<String> keys = json.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Object obj = json.get(key);
                if (key.startsWith(PREFIX))
                    key = key.substring(PREFIX.length());
                if (obj instanceof JSONObject) {
                    Component c = new Component();
                    parse(c, (JSONObject) obj);
                    instance.put(key, c);
                } else if (obj instanceof Map)  {
                    Component c = new Component();
                    parse(c, new JSONObject((Map<String, Object>) obj));
                    instance.put(key, c);
                } else {
                    if (obj instanceof Long) {
                        Long ll = (Long) obj;
                        if (ll < Integer.MAX_VALUE) {
                            obj = Integer.parseInt(ll.toString());
                        }
                    }
                    instance.put(key, obj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String completeParamName(Object p) {
        return PREFIX + p.toString();
    }

}

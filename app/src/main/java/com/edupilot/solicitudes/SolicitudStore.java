package com.edupilot.solicitudes;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SolicitudStore {
    private static final String PREFS = "solicitudes";
    private static final String KEY = "data";

    private final SharedPreferences prefs;

    public SolicitudStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Solicitud> load() {
        List<Solicitud> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                list.add(new Solicitud(
                        o.getString("titulo"),
                        o.optString("descripcion", ""),
                        o.optBoolean("atendida", false)));
            }
        } catch (JSONException ignored) {
            // datos corruptos: se empieza con lista vacía
        }
        return list;
    }

    public void save(List<Solicitud> list) {
        JSONArray array = new JSONArray();
        try {
            for (Solicitud s : list) {
                JSONObject o = new JSONObject();
                o.put("titulo", s.titulo);
                o.put("descripcion", s.descripcion);
                o.put("atendida", s.atendida);
                array.put(o);
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        prefs.edit().putString(KEY, array.toString()).apply();
    }
}
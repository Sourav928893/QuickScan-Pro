package com.sourav.qrscan;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryManager {
    private static final String PREF_NAME = "scan_history";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_SIZE = 10;

    public static void saveHistory(Context context, String result) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        List<String> history = getHistory(context);
        
        // Remove if already exists to move it to top
        history.remove(result);
        history.add(0, result);

        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }

        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>(history);
        // Note: Set doesn't preserve order, so we'll store as a comma-separated string or just use a simple approach
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i));
            if (i < history.size() - 1) sb.append(";;;"); // Custom separator
        }
        editor.putString(KEY_HISTORY, sb.toString());
        editor.apply();
    }

    public static List<String> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String historyStr = prefs.getString(KEY_HISTORY, "");
        if (historyStr.isEmpty()) return new ArrayList<>();
        
        String[] items = historyStr.split(";;;");
        List<String> history = new ArrayList<>();
        Collections.addAll(history, items);
        return history;
    }
}

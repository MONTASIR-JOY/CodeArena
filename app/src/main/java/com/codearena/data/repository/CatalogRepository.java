package com.codearena.data.repository;

import android.content.Context;
import com.codearena.data.model.CatalogItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CatalogRepository {

    private final Context context;
    private final UserPreferencesRepository prefsRepo;
    private List<CatalogItem> cachedItems = null;

    public CatalogRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefsRepo = new UserPreferencesRepository(context);
    }

    public List<CatalogItem> getCatalogItems() {
        if (cachedItems == null) {
            cachedItems = loadCatalogFromAssets();
        }

        // Synchronize with stored read states
        Set<String> readIds = prefsRepo.getReadCatalogIds();
        for (CatalogItem item : cachedItems) {
            item.setRead(readIds.contains(String.valueOf(item.getId())));
        }

        return new ArrayList<>(cachedItems);
    }

    public List<CatalogItem> getCodeforcesCatalogItems() {
        List<CatalogItem> all = getCatalogItems();
        List<CatalogItem> cf = new ArrayList<>();
        for (CatalogItem item : all) {
            if (!"CSES Problem Set".equalsIgnoreCase(item.getMainBlock())) {
                cf.add(item);
            }
        }
        return cf;
    }

    public List<CatalogItem> getCsesCatalogItems() {
        List<CatalogItem> all = getCatalogItems();
        List<CatalogItem> cses = new ArrayList<>();
        for (CatalogItem item : all) {
            if ("CSES Problem Set".equalsIgnoreCase(item.getMainBlock())) {
                cses.add(item);
            }
        }
        return cses;
    }

    private List<CatalogItem> loadCatalogFromAssets() {
        List<CatalogItem> result = new ArrayList<>();
        try (InputStream is = context.getAssets().open("codeforces_catalog.json");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<CatalogItem>>() {}.getType();
            List<CatalogItem> items = new Gson().fromJson(reader, listType);
            if (items != null) result.addAll(items);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream is = context.getAssets().open("cses_catalog.json");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<CatalogItem>>() {}.getType();
            List<CatalogItem> csesItems = new Gson().fromJson(reader, listType);
            if (csesItems != null) result.addAll(csesItems);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void setItemRead(long id, boolean read) {
        prefsRepo.setCatalogRead(id, read);
        if (cachedItems != null) {
            for (CatalogItem item : cachedItems) {
                if (item.getId() == id) {
                    item.setRead(read);
                    break;
                }
            }
        }
        // Recompute and save CSES solved count from canonical source of truth
        updateCsesSolvedCount();
    }

    /**
     * Counts how many CSES catalog items are marked as read (solved)
     * and saves the accurate count to SharedPreferences.
     * Uses the loaded catalog as source of truth rather than in-memory fragment lists.
     */
    public void updateCsesSolvedCount() {
        List<CatalogItem> csesItems = getCsesCatalogItems();
        int solved = 0;
        for (CatalogItem item : csesItems) {
            if (item.isRead()) solved++;
        }
        prefsRepo.saveCsesSolvedCount(solved);
    }
}

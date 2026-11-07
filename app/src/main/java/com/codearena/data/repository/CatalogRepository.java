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

    private List<CatalogItem> loadCatalogFromAssets() {
        try (InputStream is = context.getAssets().open("codeforces_catalog.json");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<CatalogItem>>() {}.getType();
            List<CatalogItem> items = new Gson().fromJson(reader, listType);
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
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
    }
}

package com.kuet.codearena.data.remote;

import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.model.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * PlatformSource implementation for CodeChef fetching upcoming contests through clist.by.
 */
public class CodeChefPlatformSource implements PlatformSource {

    private final ClistPlatformSource clistSource = new ClistPlatformSource();

    @Override
    public Platform getPlatform() {
        return Platform.CODECHEF;
    }

    @Override
    public List<ContestEntity> fetchUpcomingContests() throws Exception {
        List<ContestEntity> list = new ArrayList<>();
        if (ClistPlatformSource.isConfigured()) {
            List<ContestEntity> all = clistSource.fetchUpcomingContests();
            for (ContestEntity c : all) {
                if (Platform.CODECHEF.name().equalsIgnoreCase(c.getPlatform())) {
                    list.add(c);
                }
            }
        }
        return list;
    }

    @Override
    public List<SolveEntity> fetchUserAcceptedSubmissions(String handle) throws Exception {
        // CodeChef only participates in upcoming contest calendars
        return new ArrayList<>();
    }
}


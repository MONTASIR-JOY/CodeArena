package com.kuet.codearena.data.remote;

import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.model.Platform;

import java.util.List;

/**
 * Interface defining contract for platform-specific contest retrieval and submission polling.
 */
public interface PlatformSource {
    Platform getPlatform();
    List<ContestEntity> fetchUpcomingContests() throws Exception;
    List<SolveEntity> fetchUserAcceptedSubmissions(String handle) throws Exception;
}

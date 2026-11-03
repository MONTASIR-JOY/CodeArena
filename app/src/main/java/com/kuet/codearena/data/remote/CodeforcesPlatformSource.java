package com.kuet.codearena.data.remote;

import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.remote.model.CfApiResponse;
import com.kuet.codearena.data.remote.model.CfContest;
import com.kuet.codearena.data.remote.model.CfProblem;
import com.kuet.codearena.data.remote.model.CfSubmission;
import com.kuet.codearena.data.remote.model.Platform;
import com.kuet.codearena.data.repository.ContestRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * PlatformSource implementation for the official Codeforces API with rate limiting and normalisation.
 */
public class CodeforcesPlatformSource implements PlatformSource {

    @Override
    public Platform getPlatform() {
        return Platform.CODEFORCES;
    }

    @Override
    public List<ContestEntity> fetchUpcomingContests() throws Exception {
        List<ContestEntity> list = new ArrayList<>();
        Response<CfApiResponse<List<CfContest>>> response =
                ApiClient.getCodeforcesApi().getContests(false).execute();

        if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
            List<CfContest> cfList = response.body().getResult();
            long nowSec = System.currentTimeMillis() / 1000L;

            if (cfList != null) {
                for (CfContest cf : cfList) {
                    if ("BEFORE".equalsIgnoreCase(cf.getPhase()) || cf.getStartTimeSeconds() > nowSec) {
                        String division = ContestRepository.parseDivision(cf.getName());
                        String url = Endpoints.CF_PROBLEM_WEB_BASE + cf.getId();
                        list.add(new ContestEntity(
                                cf.getId(),
                                cf.getName(),
                                cf.getType(),
                                "BEFORE",
                                cf.getDurationSeconds(),
                                cf.getStartTimeSeconds(),
                                division,
                                Platform.CODEFORCES.name(),
                                url
                        ));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public List<SolveEntity> fetchUserAcceptedSubmissions(String handle) throws Exception {
        List<SolveEntity> solves = new ArrayList<>();
        if (handle == null || handle.trim().isEmpty()) return solves;

        Response<CfApiResponse<List<CfSubmission>>> response =
                ApiClient.getCodeforcesApi().getUserStatus(handle, 1, 150).execute();

        if (response.isSuccessful() && response.body() != null && response.body().isOk()) {
            List<CfSubmission> subs = response.body().getResult();
            if (subs != null) {
                for (CfSubmission s : subs) {
                    if ("OK".equalsIgnoreCase(s.getVerdict()) && s.getProblem() != null) {
                        CfProblem p = s.getProblem();
                        String problemId = "CF-" + p.getContestId() + p.getIndex();
                        String tags = p.getTags() != null ? String.join(", ", p.getTags()) : "";
                        solves.add(new SolveEntity(
                                Platform.CODEFORCES.name(),
                                problemId,
                                p.getName(),
                                p.getRating(),
                                tags,
                                s.getCreationTimeSeconds() * 1000L
                        ));
                    }
                }
            }
        }
        return solves;
    }
}


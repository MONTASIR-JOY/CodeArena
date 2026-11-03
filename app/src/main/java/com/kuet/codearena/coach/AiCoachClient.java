package com.kuet.codearena.coach;

import com.kuet.codearena.coach.model.AnonymousUserStats;

/**
 * Interface defining contract for AI-driven competitive programming advice generation.
 */
public interface AiCoachClient {

    interface Callback {
        void onResponse(String coachingMessage, boolean isAiGenerated);
    }

    void getCoachingMessage(AnonymousUserStats stats, Callback callback);
}

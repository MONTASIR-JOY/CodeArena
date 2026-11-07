package com.codearena.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendComparison {

    public static class FriendRow {
        public final String handle;
        public final Integer rating;
        public final Integer maxRating;
        public final String rank;
        public final boolean isMe;
        public final Integer deltaVsMe;

        public FriendRow(String handle, Integer rating, Integer maxRating, String rank, boolean isMe, Integer deltaVsMe) {
            this.handle = handle;
            this.rating = rating;
            this.maxRating = maxRating;
            this.rank = rank;
            this.isMe = isMe;
            this.deltaVsMe = deltaVsMe;
        }
    }

    public static class UserData {
        public final String handle;
        public final Integer rating;
        public final Integer maxRating;
        public final String rank;

        public UserData(String handle, Integer rating, Integer maxRating, String rank) {
            this.handle = handle;
            this.rating = rating;
            this.maxRating = maxRating;
            this.rank = rank;
        }
    }

    public static List<String> cleanHandles(Collection<String> raw) {
        if (raw == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String h : raw) {
            if (h != null) {
                String trimmed = h.trim();
                if (!trimmed.isEmpty() && seen.add(trimmed.toLowerCase())) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    public static List<FriendRow> build(String myHandle, List<UserData> users) {
        if (users == null) return Collections.emptyList();

        Integer myRating = null;
        for (UserData u : users) {
            if (u.handle.equalsIgnoreCase(myHandle)) {
                myRating = u.rating;
                break;
            }
        }

        List<FriendRow> rows = new ArrayList<>();
        for (UserData u : users) {
            boolean isMe = u.handle.equalsIgnoreCase(myHandle);
            Integer delta = (isMe || u.rating == null || myRating == null) ? null : u.rating - myRating;
            rows.add(new FriendRow(u.handle, u.rating, u.maxRating, u.rank, isMe, delta));
        }

        Collections.sort(rows, (a, b) -> {
            int rA = a.rating != null ? a.rating : Integer.MIN_VALUE;
            int rB = b.rating != null ? b.rating : Integer.MIN_VALUE;
            int cmp = Integer.compare(rB, rA);
            if (cmp != 0) return cmp;
            return a.handle.compareToIgnoreCase(b.handle);
        });

        return rows;
    }
}


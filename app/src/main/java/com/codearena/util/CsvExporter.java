package com.codearena.util;

import com.codearena.data.db.entity.VirtualRunEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CsvExporter {

    private static final Set<Character> FORMULA_STARTERS = new HashSet<>(
        Arrays.asList('=', '+', '-', '@', '\t', '\r')
    );

    public static String escape(String value) {
        if (value == null) return "";
        String text = value;
        if (!text.isEmpty() && FORMULA_STARTERS.contains(text.charAt(0))) {
            text = "'" + text;
        }
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public static String runsToCsv(List<VirtualRunEntity> runs) {
        if (runs == null) return "";
        List<VirtualRunEntity> sorted = new ArrayList<>(runs);
        Collections.sort(sorted, (a, b) -> Long.compare(a.getStartTimeEpochMillis(), b.getStartTimeEpochMillis()));

        StringBuilder sb = new StringBuilder();
        sb.append("Contest,Contest ID,Started (UTC),Mode,Solved,Problems,Penalty (min),Estimated performance\r\n");

        for (VirtualRunEntity run : sorted) {
            sb.append(escape(run.getContestName())).append(",");
            sb.append(run.getContestId()).append(",");
            sb.append(Instant.ofEpochMilli(run.getStartTimeEpochMillis()).toString()).append(",");
            sb.append(escape(run.getMode())).append(",");
            sb.append(run.getSolvedCount()).append(",");
            sb.append(run.getTotalProblemsCount()).append(",");
            sb.append(run.getPenaltyMinutes()).append(",");
            sb.append(run.getEstimatedPerformance() > 0 ? String.valueOf(run.getEstimatedPerformance()) : "");
            sb.append("\r\n");
        }
        return sb.toString();
    }
}


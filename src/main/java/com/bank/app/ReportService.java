package com.bank.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ReportService {

    public static final Path REPORT_FILE = Paths.get("report.txt");
    private static final int MIN_LOGIN_WIDTH = 7;
    private static final int TIME_WIDTH = 19;

    private ReportService() {
    }

    public static void onClose(Session session) {
        if (session == null) {
            return;
        }
        session.close();
        try {
            save(session);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить отчёт: " + e.getMessage());
        }
    }

    public static void save(Session session) throws IOException {
        List<String[]> rows = readRows();
        rows.add(new String[]{session.getLogin(), session.getTimeInText(), session.getTimeOutText()});
        Files.writeString(REPORT_FILE, render(rows), StandardCharsets.UTF_8);
    }

    public static String preview(Session current) {
        List<String[]> rows;
        try {
            rows = readRows();
        } catch (IOException e) {
            rows = new ArrayList<>();
        }
        if (current != null) {
            rows.add(new String[]{current.getLogin(), current.getTimeInText(), current.getTimeOutText()});
        }
        return render(rows);
    }

    public static List<String[]> readRows() throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(REPORT_FILE)) {
            return rows;
        }
        for (String raw : Files.readAllLines(REPORT_FILE, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (!line.startsWith("|") || !line.endsWith("|") || line.length() < 2) {
                continue;
            }
            String[] parts = line.substring(1, line.length() - 1).split("\\|");
            if (parts.length != 3) {
                continue;
            }
            String login = parts[0].trim();
            if (login.equals("login")) {
                continue; // шапка таблицы
            }
            rows.add(new String[]{login, parts[1].trim(), parts[2].trim()});
        }
        return rows;
    }

    public static String render(List<String[]> rows) {
        int w = MIN_LOGIN_WIDTH;
        for (String[] r : rows) {
            w = Math.max(w, r[0].length());
        }
        String border = "+" + "-".repeat(w + 2)
                + "+" + "-".repeat(TIME_WIDTH + 2)
                + "+" + "-".repeat(TIME_WIDTH + 2) + "+\n";
        StringBuilder sb = new StringBuilder();
        sb.append(border);
        sb.append(row("login", "time_in", "time_out", w));
        sb.append(border);
        for (String[] r : rows) {
            sb.append(row(r[0], r[1], r[2], w));
        }
        sb.append(border);
        return sb.toString();
    }

    private static String row(String a, String b, String c, int loginWidth) {
        return String.format("| %-" + loginWidth + "s | %-" + TIME_WIDTH + "s | %-" + TIME_WIDTH + "s |\n", a, b, c);
    }
}
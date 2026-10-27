package com.diary.service;

import com.diary.dto.EntryForm;
import com.diary.model.AppUser;
import com.diary.model.DiaryEntry;
import com.diary.repository.AppUserRepository;
import com.diary.repository.DiaryEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class DiaryService {

    /** One bar of the 7-day mood chart. */
    public static class DayBar {
        private final String label;
        private final double avg;
        public DayBar(String label, double avg) { this.label = label; this.avg = avg; }
        public String getLabel() { return label; }
        public double getAvg() { return avg; }
    }

    /** Numbers shown on the dashboard. */
    public static class WeekStats {
        private final int count;
        private final double avgMood;
        private final List<DayBar> days;
        public WeekStats(int count, double avgMood, List<DayBar> days) {
            this.count = count; this.avgMood = avgMood; this.days = days;
        }
        public int getCount() { return count; }
        public double getAvgMood() { return avgMood; }
        public List<DayBar> getDays() { return days; }
    }

    private final DiaryEntryRepository entries;
    private final AppUserRepository users;

    public DiaryService(DiaryEntryRepository entries, AppUserRepository users) {
        this.entries = entries;
        this.users = users;
    }

    private AppUser user(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @Transactional(readOnly = true)
    public List<DiaryEntry> search(String username, String q, String category, LocalDate from, LocalDate to) {
        String query = q == null ? "" : q.trim().toLowerCase();
        String cat = category == null ? "" : category;
        LocalDate f = from != null ? from : LocalDate.of(1900, 1, 1);
        LocalDate t = to != null ? to : LocalDate.of(2999, 12, 31);
        return entries.search(user(username).getId(), query, "%" + query + "%", cat, f, t);
    }

    @Transactional(readOnly = true)
    public DiaryEntry findOwned(String username, Long id) {
        return entries.findByIdAndOwnerId(id, user(username).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void save(String username, Long id, EntryForm form) {
        AppUser owner = user(username);
        DiaryEntry e = (id == null) ? new DiaryEntry() : findOwned(username, id);
        e.setOwner(owner);
        e.setEntryDate(form.getEntryDate());
        e.setTitle(form.getTitle().trim());
        e.setCategory(form.getCategory());
        e.setMood(form.getMood());
        e.setContent(form.getContent() == null ? "" : form.getContent());
        entries.save(e);
    }

    @Transactional
    public void delete(String username, Long id) {
        entries.delete(findOwned(username, id));
    }

    @Transactional(readOnly = true)
    public WeekStats weekStats(String username) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        List<DiaryEntry> week = entries.findByOwnerIdAndEntryDateBetween(user(username).getId(), start, today);

        List<DayBar> bars = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double avg = week.stream().filter(e -> e.getEntryDate().equals(d))
                    .mapToInt(DiaryEntry::getMood).average().orElse(0);
            bars.add(new DayBar(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), avg));
        }
        double overall = week.stream().mapToInt(DiaryEntry::getMood).average().orElse(0);
        return new WeekStats(week.size(), overall, bars);
    }
}

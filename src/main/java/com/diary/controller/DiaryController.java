package com.diary.controller;

import com.diary.dto.EntryForm;
import com.diary.model.DiaryEntry;
import com.diary.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DiaryController {
    private static final List<String> CATEGORIES =
            List.of("Meeting", "Task", "Learning", "Idea", "Review", "Other");

    private final DiaryService service;

    public DiaryController(DiaryService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/entries";
    }

    @GetMapping("/entries")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Principal principal, Model model) {
        model.addAttribute("entries", service.search(principal.getName(), q, category, from, to));
        model.addAttribute("stats", service.weekStats(principal.getName()));
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "entries";
    }

    @GetMapping("/entries/new")
    public String newForm(Model model) {
        prepareForm(model, new EntryForm(), "/entries", "New entry");
        return "entry-form";
    }

    @PostMapping("/entries")
    public String create(@Valid @ModelAttribute("form") EntryForm form, BindingResult br,
                         Principal principal, Model model) {
        if (br.hasErrors()) {
            prepareFormKeepForm(model, "/entries", "New entry");
            return "entry-form";
        }
        service.save(principal.getName(), null, form);
        return "redirect:/entries";
    }

    @GetMapping("/entries/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        DiaryEntry e = service.findOwned(principal.getName(), id);
        EntryForm f = new EntryForm();
        f.setEntryDate(e.getEntryDate());
        f.setTitle(e.getTitle());
        f.setCategory(e.getCategory());
        f.setMood(e.getMood());
        f.setContent(e.getContent());
        prepareForm(model, f, "/entries/" + id, "Edit entry");
        return "entry-form";
    }

    @PostMapping("/entries/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") EntryForm form,
                         BindingResult br, Principal principal, Model model) {
        if (br.hasErrors()) {
            prepareFormKeepForm(model, "/entries/" + id, "Edit entry");
            return "entry-form";
        }
        service.save(principal.getName(), id, form);
        return "redirect:/entries";
    }

    @PostMapping("/entries/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        service.delete(principal.getName(), id);
        return "redirect:/entries";
    }

    @GetMapping("/entries/export")
    public ResponseEntity<byte[]> export(Principal principal) {
        StringBuilder sb = new StringBuilder();
        for (DiaryEntry e : service.search(principal.getName(), "", "", null, null)) {
            sb.append(e.getEntryDate()).append(" | ").append(e.getTitle())
              .append(" | ").append(e.getCategory())
              .append(" | Mood ").append(e.getMood()).append("\n")
              .append(e.getContent()).append("\n\n----------------------------\n\n");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diary-export.txt\"")
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void prepareForm(Model model, EntryForm form, String action, String title) {
        model.addAttribute("form", form);
        prepareFormKeepForm(model, action, title);
    }

    private void prepareFormKeepForm(Model model, String action, String title) {
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("action", action);
        model.addAttribute("pageTitle", title);
    }
}

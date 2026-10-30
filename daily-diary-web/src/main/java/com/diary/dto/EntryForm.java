package com.diary.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class EntryForm {
    @NotNull(message = "Date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate entryDate = LocalDate.now();

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title can be at most 150 characters")
    private String title;

    @NotBlank(message = "Category is required")
    private String category = "Task";

    @Min(value = 1, message = "Mood must be 1 to 5")
    @Max(value = 5, message = "Mood must be 1 to 5")
    private int mood = 3;

    @Size(max = 10000, message = "Notes can be at most 10000 characters")
    private String content;

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getMood() { return mood; }
    public void setMood(int mood) { this.mood = mood; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

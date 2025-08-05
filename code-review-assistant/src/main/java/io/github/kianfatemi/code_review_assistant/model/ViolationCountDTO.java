package io.github.kianfatemi.code_review_assistant.model;

import java.time.LocalDate;

public class ViolationCountDTO {

    private LocalDate date;
    private Long count;

    public ViolationCountDTO(LocalDate date, Long count) {
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}

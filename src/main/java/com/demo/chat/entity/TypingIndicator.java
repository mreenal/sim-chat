package com.demo.chat.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class TypingIndicator {

    private final String name;
    private final LocalDateTime timestamp;

    public TypingIndicator(String name) {
        this.name = name;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypingIndicator that = (TypingIndicator) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

package com.bank.app;

import java.time.format.DateTimeFormatter;

public abstract class AbstractEntity<T extends AbstractEntity<T>> implements Comparable<T> {

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static long counter = 0;

    private long id;

    protected AbstractEntity() {
        this.id = ++counter;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным");
        }
        this.id = id;
    }

    public abstract String describe();

    public abstract boolean isValid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((AbstractEntity<?>) o).id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id;
    }
}
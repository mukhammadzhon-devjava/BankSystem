package com.bank.app;

import java.time.LocalDateTime;
import java.util.Objects;

public class Session extends AbstractEntity<Session> implements Comparable<Session> {

    private String login;
    private LocalDateTime timeIn;
    private LocalDateTime timeOut;

    public Session(String login) {
        super();
        setLogin(login);
        setTimeIn(LocalDateTime.now());
        this.timeOut = null;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин сессии не может быть пустым");
        }
        this.login = login.trim();
    }

    public LocalDateTime getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(LocalDateTime timeIn) {
        if (timeIn == null) {
            throw new IllegalArgumentException("Время входа не может быть пустым");
        }
        this.timeIn = timeIn;
    }

    public LocalDateTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalDateTime timeOut) {
        this.timeOut = timeOut;
    }

    public String getTimeInText() {
        return timeIn.format(TIME_FORMAT);
    }

    public String getTimeOutText() {
        return timeOut == null ? "—" : timeOut.format(TIME_FORMAT);
    }

    public void close() {
        if (timeOut == null) {
            timeOut = LocalDateTime.now();
        }
    }

    @Override
    public String describe() {
        return login + " | " + getTimeInText() + " | " + getTimeOutText();
    }

    @Override
    public boolean isValid() {
        return login != null && !login.isBlank() && timeIn != null;
    }

    @Override
    public int compareTo(Session other) {
        return this.timeIn.compareTo(other.timeIn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session s)) return false;
        return login.equals(s.login) && timeIn.equals(s.timeIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, timeIn);
    }

    @Override
    public String toString() {
        return "Session{login='" + login + "', timeIn=" + getTimeInText() + ", timeOut=" + getTimeOutText() + "}";
    }
}
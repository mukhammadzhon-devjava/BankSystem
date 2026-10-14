package com.bank.app;

import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction extends AbstractEntity<Transaction> implements Comparable<Transaction> {

    public static final String DEPOSIT = "Пополнение";
    public static final String WITHDRAW = "Снятие";
    public static final String TRANSFER = "Перевод";

    private String fromNumber;
    private String toNumber;
    private double amount;
    private String kind;
    private LocalDateTime time;

    public Transaction(String fromNumber, String toNumber, double amount, String kind, LocalDateTime time) {
        super();
        setFromNumber(fromNumber);
        setToNumber(toNumber);
        setAmount(amount);
        setKind(kind);
        setTime(time);
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = (fromNumber == null) ? "" : fromNumber;
    }

    public String getToNumber() {
        return toNumber;
    }

    public void setToNumber(String toNumber) {
        this.toNumber = (toNumber == null) ? "" : toNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (Double.isNaN(amount) || amount <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть больше нуля");
        }
        this.amount = amount;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("Тип транзакции не может быть пустым");
        }
        this.kind = kind;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        if (time == null) {
            throw new IllegalArgumentException("Время транзакции не может быть пустым");
        }
        this.time = time;
    }

    public String getTimeText() {
        return time.format(TIME_FORMAT);
    }

    public String getFromText() {
        return fromNumber.isEmpty() ? "—" : fromNumber;
    }

    public String getToText() {
        return toNumber.isEmpty() ? "—" : toNumber;
    }

    @Override
    public String describe() {
        return kind + ": " + getFromText() + " → " + getToText() + ", " + String.format("%.2f", amount);
    }

    @Override
    public boolean isValid() {
        return amount > 0 && kind != null && !kind.isBlank() && time != null;
    }

    @Override
    public int compareTo(Transaction other) {
        int c = this.time.compareTo(other.time);
        return c != 0 ? c : Long.compare(this.getId(), other.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction t)) return false;
        return Double.compare(amount, t.amount) == 0
                && fromNumber.equals(t.fromNumber)
                && toNumber.equals(t.toNumber)
                && kind.equals(t.kind)
                && time.equals(t.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromNumber, toNumber, amount, kind, time);
    }

    @Override
    public String toString() {
        return "Transaction{id=" + getId() + ", " + describe() + ", time=" + getTimeText() + "}";
    }
}
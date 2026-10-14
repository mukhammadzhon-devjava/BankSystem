package com.bank.app;

import java.util.Objects;

public class Account extends AbstractEntity<Account> implements Comparable<Account> {

    public static final String CURRENT = "Текущий";
    public static final String SAVINGS = "Накопительный";

    private String number;
    private long ownerId;
    private String type;
    private double balance;

    public Account(String number, long ownerId, String type, double balance) {
        super();
        setNumber(number);
        setOwnerId(ownerId);
        setType(type);
        setBalance(balance);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Номер счёта не может быть пустым");
        }
        this.number = number.trim();
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        if (ownerId <= 0) {
            throw new IllegalArgumentException("Некорректный владелец счёта");
        }
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Тип счёта не может быть пустым");
        }
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (Double.isNaN(balance) || balance < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }
        this.balance = Math.round(balance * 100.0) / 100.0;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }
        setBalance(balance + amount);
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше нуля");
        }
        if (amount > balance) {
            throw new IllegalStateException("Недостаточно средств на счёте " + number);
        }
        setBalance(balance - amount);
    }

    @Override
    public String describe() {
        return number + " · " + type + " · " + String.format("%.2f", balance);
    }

    @Override
    public boolean isValid() {
        return number != null && !number.isBlank() && ownerId > 0 && balance >= 0;
    }

    @Override
    public int compareTo(Account other) {
        int c = Double.compare(this.balance, other.balance);
        return c != 0 ? c : this.number.compareTo(other.number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account a)) return false;
        return number.equals(a.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Account{id=" + getId() + ", number='" + number + "', type='" + type
                + "', balance=" + balance + "}";
    }
}
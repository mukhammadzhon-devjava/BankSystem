package com.bank.app;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Client extends AbstractEntity<Client> implements Comparable<Client> {

    private String name;
    private String login;
    private String passwordHash;
    private String city;

    public Client(String name, String login, String password, String city) {
        super();
        setName(name);
        setLogin(login);
        setPasswordHash(hash(password));
        setCity(city);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        this.name = name.trim();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        this.login = login.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Хэш пароля не может быть пустым");
        }
        this.passwordHash = passwordHash;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = (city == null) ? "" : city.trim();
    }

    public boolean checkPassword(String raw) {
        return raw != null && passwordHash.equals(hash(raw));
    }

    public static String hash(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Пароль не может быть null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }

    @Override
    public String describe() {
        return name + " (" + login + "), " + city;
    }

    @Override
    public boolean isValid() {
        return name != null && !name.isBlank()
                && login != null && !login.isBlank()
                && passwordHash != null && !passwordHash.isBlank();
    }

    @Override
    public int compareTo(Client other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client c)) return false;
        return login.equals(c.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "Client{id=" + getId() + ", name='" + name + "', login='" + login + "', city='" + city + "'}";
    }
}
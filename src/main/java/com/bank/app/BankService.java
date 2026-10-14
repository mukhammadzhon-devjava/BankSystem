package com.bank.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BankService {

    private static final ObservableList<Client> CLIENTS = FXCollections.observableArrayList();
    private static final ObservableList<Account> ACCOUNTS = FXCollections.observableArrayList();
    private static final ObservableList<Transaction> TRANSACTIONS = FXCollections.observableArrayList();

    private static final EntityMapper<String, Client> CLIENTS_BY_LOGIN = new EntityMapper<>();
    private static final EntityMapper<String, Account> ACCOUNTS_BY_NUMBER = new EntityMapper<>();

    private static Session currentSession;
    private static Client currentClient;

    private BankService() {
    }

    public static ObservableList<Client> getClients() {
        return CLIENTS;
    }

    public static ObservableList<Account> getAccounts() {
        return ACCOUNTS;
    }

    public static ObservableList<Transaction> getTransactions() {
        return TRANSACTIONS;
    }

    public static Session getCurrentSession() {
        return currentSession;
    }

    public static Client getCurrentClient() {
        return currentClient;
    }

    public static Client addClient(Client client) {
        CLIENTS.add(client);
        CLIENTS_BY_LOGIN.put(client.getLogin(), client);
        return client;
    }

    public static Account addAccount(Account account) {
        ACCOUNTS.add(account);
        ACCOUNTS_BY_NUMBER.put(account.getNumber(), account);
        return account;
    }

    public static void seed() {
        if (!CLIENTS.isEmpty()) {
            return;
        }
        Client ivanov = addClient(new Client("Иван Иванов", "ivanov", "1234", "Москва"));
        Client petrova = addClient(new Client("Мария Петрова", "petrova", "1234", "Казань"));
        Client sidorov = addClient(new Client("Алексей Сидоров", "sidorov", "1234", "Самара"));
        Client rahimov = addClient(new Client("Карим Рахимов", "rahimov", "1234", "Душанбе"));
        Client smirnova = addClient(new Client("Ольга Смирнова", "smirnova", "1234", "Тверь"));

        addAccount(new Account("40817-0001", ivanov.getId(), Account.CURRENT, 85000));
        addAccount(new Account("40817-0002", ivanov.getId(), Account.SAVINGS, 250000));
        addAccount(new Account("40817-0003", petrova.getId(), Account.CURRENT, 42000.50));
        addAccount(new Account("40817-0004", sidorov.getId(), Account.CURRENT, 12000));
        addAccount(new Account("40817-0005", rahimov.getId(), Account.CURRENT, 99000));
        addAccount(new Account("40817-0006", rahimov.getId(), Account.SAVINGS, 150000));
        addAccount(new Account("40817-0007", smirnova.getId(), Account.CURRENT, 5000));

        LocalDateTime now = LocalDateTime.now();
        TRANSACTIONS.add(new Transaction("40817-0005", "40817-0003", 3500, Transaction.TRANSFER, now.minusDays(1)));
        TRANSACTIONS.add(new Transaction("40817-0004", "", 2000, Transaction.WITHDRAW, now.minusDays(2)));
        TRANSACTIONS.add(new Transaction("", "40817-0001", 15000, Transaction.DEPOSIT, now.minusDays(3)));
        TRANSACTIONS.add(new Transaction("40817-0001", "40817-0007", 1200, Transaction.TRANSFER, now.minusDays(5)));
        TRANSACTIONS.add(new Transaction("", "40817-0002", 50000, Transaction.DEPOSIT, now.minusDays(7)));

        FXCollections.sort(CLIENTS);   // Comparable<Client>: по имени
        FXCollections.sort(ACCOUNTS);  // Comparable<Account>: по балансу
    }


    public static Client authenticate(String login, String password) {
        if (login == null || login.isBlank() || password == null) {
            return null;
        }
        Client client = CLIENTS_BY_LOGIN.get(login.trim().toLowerCase());
        return (client != null && client.checkPassword(password)) ? client : null;
    }

    public static Session startSession(Client client) {
        currentClient = client;
        currentSession = new Session(client.getLogin());
        return currentSession;
    }


    public static Transaction deposit(Account account, double amount) {
        requireAccount(account);
        account.deposit(amount);
        return record(new Transaction("", account.getNumber(), amount, Transaction.DEPOSIT, LocalDateTime.now()));
    }

    public static Transaction withdraw(Account account, double amount) {
        requireAccount(account);
        account.withdraw(amount);
        return record(new Transaction(account.getNumber(), "", amount, Transaction.WITHDRAW, LocalDateTime.now()));
    }

    public static Transaction transfer(Account from, Account to, double amount) {
        requireAccount(from);
        if (to == null) {
            throw new IllegalArgumentException("Выберите счёт получателя");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("Нельзя перевести деньги на тот же счёт");
        }
        from.withdraw(amount);
        to.deposit(amount);
        return record(new Transaction(from.getNumber(), to.getNumber(), amount, Transaction.TRANSFER, LocalDateTime.now()));
    }

    private static Transaction record(Transaction t) {
        TRANSACTIONS.add(0, t); // новые сверху
        return t;
    }

    private static void requireAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Выберите счёт");
        }
    }

    public static double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Введите сумму");
        }
        try {
            double value = Double.parseDouble(text.trim().replace(',', '.'));
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                throw new IllegalArgumentException("Сумма должна быть больше нуля");
            }
            return Math.round(value * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Сумма введена неверно");
        }
    }

    public static double tryParseAmount(String text) {
        try {
            return parseAmount(text);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static String formatMoney(double value) {
        return String.format("%,.2f ₽", value);
    }

    public static String ownerName(Account account) {
        for (Client c : CLIENTS) {
            if (c.getId() == account.getOwnerId()) {
                return c.getName();
            }
        }
        return "—";
    }

    public static boolean matchesAccount(Account account, Client owner) {
        return owner == null || account.getOwnerId() == owner.getId();
    }

    public static boolean matches(Transaction t, String kind, LocalDate from, Client owner) {
        if (kind != null && !kind.isEmpty() && !t.getKind().equals(kind)) {
            return false;
        }
        if (from != null && t.getTime().toLocalDate().isBefore(from)) {
            return false;
        }
        return owner == null || isOwnedBy(t.getFromNumber(), owner) || isOwnedBy(t.getToNumber(), owner);
    }

    private static boolean isOwnedBy(String number, Client owner) {
        Account a = ACCOUNTS_BY_NUMBER.get(number);
        return a != null && a.getOwnerId() == owner.getId();
    }
}
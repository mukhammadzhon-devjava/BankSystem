package com.bank.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public class MainController {

    @FXML private Label titleLabel, userLabel, operationsLabel, balanceLabel, statusLabel, filterLabel;
    @FXML private TableView<Client> clientTable;
    @FXML private TableView<Account> accountTable;
    @FXML private TableView<Transaction> transactionTable;
    @FXML private ComboBox<Account> accountCombo, targetCombo;
    @FXML private TextField amountField;
    @FXML private Slider amountSlider;
    @FXML private ProgressBar shareBar;
    @FXML private Button depositButton, withdrawButton, transferButton, reportButton;
    @FXML private RadioButton allRadio, depositRadio, withdrawRadio, transferRadio;
    @FXML private DatePicker fromDate;
    @FXML private CheckBox myOnlyCheck;

    private final ToggleGroup filterGroup = new ToggleGroup();
    private FilteredList<Account> accountsView;
    private FilteredList<Transaction> transactionsView;
    private Client me;

    @FXML
    private void initialize() {
        me = BankService.getCurrentClient();
        userLabel.setText("Пользователь: " + me.getName() + " (" + me.getLogin() + ")");
        setupTables();
        setupOperations();
        setupFilters();
        styleControls();
        updateShare();
    }

    private void setupTables() {
        addColumn(clientTable, "ID", c -> String.valueOf(c.getId()), 50);
        addColumn(clientTable, "Имя", Client::getName, 160);
        addColumn(clientTable, "Логин", Client::getLogin, 100);
        addColumn(clientTable, "Город", Client::getCity, 100);
        clientTable.setItems(BankService.getClients());

        addColumn(accountTable, "Номер", Account::getNumber, 100);
        addColumn(accountTable, "Владелец", BankService::ownerName, 150);
        addColumn(accountTable, "Тип", Account::getType, 110);
        addColumn(accountTable, "Баланс", a -> BankService.formatMoney(a.getBalance()), 120);
        accountsView = new FilteredList<>(BankService.getAccounts(), a -> true);
        SortedList<Account> sortedAccounts = new SortedList<>(accountsView);
        sortedAccounts.comparatorProperty().bind(accountTable.comparatorProperty());
        accountTable.setItems(sortedAccounts);

        addColumn(transactionTable, "Дата", Transaction::getTimeText, 160);
        addColumn(transactionTable, "Тип", Transaction::getKind, 110);
        addColumn(transactionTable, "Откуда", Transaction::getFromText, 120);
        addColumn(transactionTable, "Куда", Transaction::getToText, 120);
        addColumn(transactionTable, "Сумма", t -> BankService.formatMoney(t.getAmount()), 130);
        transactionsView = new FilteredList<>(BankService.getTransactions(), t -> true);
        SortedList<Transaction> sortedTransactions = new SortedList<>(transactionsView);
        sortedTransactions.comparatorProperty().bind(transactionTable.comparatorProperty());
        transactionTable.setItems(sortedTransactions);

        clientTable.setPlaceholder(new Label("Нет клиентов"));
        accountTable.setPlaceholder(new Label("Нет счетов"));
        transactionTable.setPlaceholder(new Label("Нет транзакций"));
    }

    private static <S> void addColumn(TableView<S> table, String title, Function<S, String> getter, double width) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER-LEFT;");
        table.getColumns().add(column);
    }


    private void setupOperations() {
        FilteredList<Account> own = new FilteredList<>(BankService.getAccounts(), a -> a.getOwnerId() == me.getId());
        StringConverter<Account> converter = new StringConverter<>() {
            @Override
            public String toString(Account a) {
                return a == null ? "" : a.getNumber() + " · " + a.getType();
            }

            @Override
            public Account fromString(String s) {
                return null;
            }
        };

        accountCombo.setItems(own);
        accountCombo.setConverter(converter);
        accountCombo.setPromptText("Ваш счёт");
        accountCombo.getSelectionModel().selectFirst();
        accountCombo.setOnAction(e -> updateShare());

        targetCombo.setItems(BankService.getAccounts());
        targetCombo.setConverter(converter);
        targetCombo.setPromptText("Счёт получателя");

        amountSlider.setMin(0);
        amountSlider.setMax(10000);
        amountSlider.setValue(0);
        amountSlider.setMajorTickUnit(2500);
        amountSlider.setMinorTickCount(4);
        amountSlider.setBlockIncrement(100);
        amountSlider.setShowTickMarks(true);
        amountSlider.setShowTickLabels(true);
        amountSlider.valueProperty().addListener((obs, oldV, newV) ->
                amountField.setText(String.valueOf(Math.round(newV.doubleValue()))));

        amountField.textProperty().addListener((obs, oldV, newV) -> updateShare());

        depositButton.setOnAction(e -> execute(
                () -> BankService.deposit(selectedAccount(), BankService.parseAmount(amountField.getText())),
                "Счёт пополнен"));
        withdrawButton.setOnAction(e -> execute(
                () -> BankService.withdraw(selectedAccount(), BankService.parseAmount(amountField.getText())),
                "Деньги сняты"));
        transferButton.setOnAction(e -> execute(
                () -> BankService.transfer(selectedAccount(), targetCombo.getValue(),
                        BankService.parseAmount(amountField.getText())),
                "Перевод выполнен"));
        reportButton.setOnAction(e -> showReport());
    }

    private Account selectedAccount() {
        Account a = accountCombo.getValue();
        if (a == null) {
            throw new IllegalArgumentException("Выберите свой счёт");
        }
        return a;
    }

    private void execute(Runnable operation, String okMessage) {
        try {
            operation.run();
            setStatus(okMessage, false);
            refreshAll();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            setStatus(ex.getMessage(), true);
        }
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setTextFill(error ? Color.CRIMSON : Color.web("#2e7d32"));
        statusLabel.setText(message);
    }

    private void refreshAll() {
        accountTable.refresh();
        transactionTable.refresh();
        updateShare();
    }

    private void updateShare() {
        Account a = accountCombo.getValue();
        double amount = BankService.tryParseAmount(amountField.getText());
        double progress = (a == null || a.getBalance() <= 0) ? 0 : amount / a.getBalance();
        shareBar.setProgress(Math.min(1.0, progress));
        shareBar.setStyle(progress > 1.0 ? "-fx-accent: #c62828;" : "-fx-accent: #2e7d32;");
        balanceLabel.setText(a == null ? "Выберите счёт" : "Баланс: " + BankService.formatMoney(a.getBalance()));
    }

    private void showReport() {
        TextArea area = new TextArea(ReportService.preview(BankService.getCurrentSession()));
        area.setEditable(false);
        area.setFont(Font.font("Monospaced", 13));
        area.setPrefSize(640, 220);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Отчёт о работе");
        alert.setHeaderText("Сессии пользователей (report.txt)");
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private void setupFilters() {
        allRadio.setUserData("");
        depositRadio.setUserData(Transaction.DEPOSIT);
        withdrawRadio.setUserData(Transaction.WITHDRAW);
        transferRadio.setUserData(Transaction.TRANSFER);
        for (RadioButton r : List.of(allRadio, depositRadio, withdrawRadio, transferRadio)) {
            r.setToggleGroup(filterGroup);
        }
        allRadio.setSelected(true);

        filterGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> applyFilters());
        fromDate.setOnAction(e -> applyFilters());
        myOnlyCheck.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        String kind = (String) filterGroup.getSelectedToggle().getUserData();
        LocalDate from = fromDate.getValue();
        Client owner = myOnlyCheck.isSelected() ? me : null;
        transactionsView.setPredicate(t -> BankService.matches(t, kind, from, owner));
        accountsView.setPredicate(a -> BankService.matchesAccount(a, owner));
    }

    private void styleControls() {
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#1b5e20"));
        userLabel.setFont(Font.font("Arial", 14));
        userLabel.setTextFill(Color.DARKSLATEGRAY);
        operationsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        balanceLabel.setTextFill(Color.web("#1565c0"));
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setWrapText(true);
        statusLabel.setMinHeight(44);

        amountField.setPromptText("Сумма, ₽");
        amountField.setFont(Font.font("Arial", 14));
        amountField.setPadding(new Insets(6, 8, 6, 8));
        amountField.setStyle("-fx-background-color: white; -fx-border-color: #81c784; -fx-border-radius: 4;");
        amountField.setOnAction(e -> updateShare());

        shareBar.setPrefHeight(18);
        shareBar.setProgress(0);
        amountSlider.setPadding(new Insets(4, 0, 4, 0));

        accountCombo.setStyle("-fx-font-size: 13px;");
        accountCombo.setPadding(new Insets(2));
        targetCombo.setStyle("-fx-font-size: 13px;");
        targetCombo.setPadding(new Insets(2));

        for (RadioButton r : List.of(allRadio, depositRadio, withdrawRadio, transferRadio)) {
            r.setFont(Font.font("Arial", 13));
            r.setTextFill(Color.DARKSLATEGRAY);
            r.setPadding(new Insets(2, 4, 2, 4));
        }

        fromDate.setPromptText("С даты");
        fromDate.setPrefWidth(140);
        fromDate.setStyle("-fx-font-size: 13px;");

        myOnlyCheck.setFont(Font.font("Arial", 13));
        myOnlyCheck.setTextFill(Color.DARKSLATEGRAY);
        myOnlyCheck.setPadding(new Insets(2, 4, 2, 4));

        styleButton(depositButton, "#2e7d32");
        styleButton(withdrawButton, "#ef6c00");
        styleButton(transferButton, "#1565c0");
        styleButton(reportButton, "#6a1b9a");
    }

    private static void styleButton(Button button, String color) {
        button.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        button.setTextFill(Color.WHITE);
        button.setPadding(new Insets(8, 16, 8, 16));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        button.setCursor(Cursor.HAND);
        button.setOnMouseEntered(e -> button.setOpacity(0.85));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }
}
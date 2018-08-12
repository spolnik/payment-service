package io.payments;

import io.payments.account.Account;
import io.payments.payment.PaymentApiV1IntegrationTest;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class TestUtils {

    public static final CurrencyUnit PLN = CurrencyUnit.getInstance("PLN");

    public static Integer findRandomOpenPortOnAllLocalInterfaces() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return findRandomOpenPortOnAllLocalInterfaces();
        }
    }

    @SuppressWarnings("all")
    public static void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static Account createAccount(String name, int initialBalance) {
        String userId = String.format("user%s@mail.com", name);
        String accountId = String.format("ACCOUNT_%s", name);

        return new Account(
                null, userId, accountId, Money.of(PLN, initialBalance)
        );
    }
}

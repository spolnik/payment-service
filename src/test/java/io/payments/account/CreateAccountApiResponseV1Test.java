package io.payments.account;

import org.joda.money.Money;
import org.junit.Test;

import static io.payments.TestUtils.PLN;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountApiResponseV1Test {

    private static final CreateAccountApiRequestV1 request = new CreateAccountApiRequestV1(
            "USER_ID",
            "ACCOUNT_ID",
            Money.of(PLN, 1000.0),
            "TRACK_ID"
    );

    @Test
    public void reassigns_user_id_to_account() {
        Account account = request.toAccount();
        assertThat(account.getUserId()).isEqualTo(request.getUserId());
    }

    @Test
    public void reassigns_initial_balance_to_account() {
        Account account = request.toAccount();
        assertThat(account.getBalance()).isEqualTo(request.getInitialBalance());
    }

    @Test
    public void reassigns_account_id_to_account() {
        Account account = request.toAccount();
        assertThat(account.getAccountId()).isEqualTo(request.getAccountId());
    }
}
package io.payments.account;

import io.payments.payment.PaymentStatus;
import org.joda.money.Money;
import org.junit.Test;

import static io.payments.TestUtils.PLN;
import static io.payments.api.Common.gson;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountSerializationTest {

    private static final Money ACCOUNT_BALANCE = Money.of(PLN, 1000.0);
    private static final String ACCOUNT_ID = "ACCOUNT_ID";

    private final Account account = new Account(
            "ID", "USER_ID", ACCOUNT_ID, ACCOUNT_BALANCE
    );

    private final CreateAccountApiRequestV1 request = new CreateAccountApiRequestV1(
            "ID", ACCOUNT_ID, ACCOUNT_BALANCE, "TRACK_ID"
    );

    private final CreateAccountApiResponseV1 response = new CreateAccountApiResponseV1(
            ACCOUNT_ID, PaymentStatus.COMPLETED.toString(), "TRACK_ID"
    );

    @Test
    public void serializes_account_to_json_and_back() {
        String accountAsJson = gson().toJson(account);
        Account accountFromJson = gson().fromJson(accountAsJson, Account.class);

        assertThat(accountFromJson).isEqualTo(account);
    }

    @Test
    public void serializes_create_account_request_to_json_and_back() {
        String requestAsJson = gson().toJson(request);
        CreateAccountApiRequestV1 requestFromJson =
                gson().fromJson(requestAsJson, CreateAccountApiRequestV1.class);

        assertThat(requestFromJson).isEqualTo(request);
    }

    @Test
    public void serializes_create_account_response_to_json_and_back() {
        String responseAsJson = gson().toJson(response);
        CreateAccountApiResponseV1 responseFromJson =
                gson().fromJson(responseAsJson, CreateAccountApiResponseV1.class);

        assertThat(responseFromJson).isEqualTo(response);
    }
}

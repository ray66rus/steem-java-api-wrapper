package eu.bittrade.libs.steemj.base.models.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.Asset;
import eu.bittrade.libs.steemj.base.models.BaseTransactionalUnitTest;
import eu.bittrade.libs.steemj.enums.AssetSymbolType;
import eu.bittrade.libs.steemj.exceptions.SteemInvalidTransactionException;

/**
 * Test a Steem "delgate vesting shares operation".
 * 
 * @author <a href="http://steemit.com/@dez1337">dez1337</a>
 */
public class DelegateVestingSharesOperationTest extends BaseTransactionalUnitTest {
    final String EXPECTED_BYTE_REPRESENTATION = "280764657a3133333706737465656d6ac4f4f518000000000656455354530000";
    final String EXPECTED_TRANSACTION_HASH = "30a4cf13c8dbcb08aee58f7adf7fbde780e15bf40b07c32553aea15621224a6b";
    final String EXPECTED_TRANSACTION_SERIALIZATION = "0000000000000000000000000000000000000000000000000000000000000000f68585abf4dceec804"
            + "5701280764657a3133333706737465656d6ac4f4f51800000000065645535453000000";

    private static DelegateVestingSharesOperation delegateVestingSharesOperation;

    /**
     * Prepare the environment for this specific test.
     * 
     * @throws Exception
     *             If something went wrong.
     */
    @BeforeClass()
    public static void prepareTestClass() throws Exception {
        setupUnitTestEnvironmentForTransactionalTests();

        AccountName delegator = new AccountName("dez1337");
        AccountName delegatee = new AccountName("steemj");
        Asset vestingShares = new Asset(418772164L, AssetSymbolType.VESTS);

        delegateVestingSharesOperation = new DelegateVestingSharesOperation(delegator, delegatee, vestingShares);

        ArrayList<Operation> operations = new ArrayList<>();
        operations.add(delegateVestingSharesOperation);

        signedTransaction.setOperations(operations);
    }

    @Override
    @Test
    public void testOperationToByteArray() throws UnsupportedEncodingException, SteemInvalidTransactionException {
        assertThat("Expect that the operation has the given byte representation.",
                Utils.HEX.encode(delegateVestingSharesOperation.toByteArray()), equalTo(EXPECTED_BYTE_REPRESENTATION));
    }

    @Override
    @Test
    public void testTransactionWithOperationToHex()
            throws UnsupportedEncodingException, SteemInvalidTransactionException {
        sign();

        assertThat("The serialized transaction should look like expected.",
                Utils.HEX.encode(signedTransaction.toByteArray()), equalTo(EXPECTED_TRANSACTION_SERIALIZATION));
        assertThat("Expect that the serialized transaction results in the given hex.",
                Utils.HEX.encode(Sha256Hash.wrap(Sha256Hash.hash(signedTransaction.toByteArray())).getBytes()),
                equalTo(EXPECTED_TRANSACTION_HASH));
    }
}

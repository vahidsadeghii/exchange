package com.exchange.wallet;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class WalletAppTest {
    @Test
    void testWalletApp() {
        assertNotNull(new WalletApp());
    }
}

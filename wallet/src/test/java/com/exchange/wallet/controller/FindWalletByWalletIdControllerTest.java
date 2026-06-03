package com.exchange.wallet.controller;


import com.exchange.wallet.controller.findbywalletid.FindWalletByWalletIdController;
import com.exchange.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FindWalletByWalletIdController.class)
public class FindWalletByWalletIdControllerTest {
   @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    void shouldReturnWallet() throws Exception {

        mockMvc.perform(get("/wallet/api/v1/wallet-by-id")
                .param("walletId", "123"))
                .andExpect(status().isOk());
    }
}

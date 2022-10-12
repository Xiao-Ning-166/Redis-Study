package com.example;

import com.example.service.IShopService;
import com.example.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author xiaoning
 * @date 2022/10/12
 */
@SpringBootTest
public class RedisTests {

    @Autowired
    private ShopServiceImpl shopService;

    @Test
    void saveShop2Redis() throws InterruptedException {

        shopService.saveShop2Redis(1L, 20L);

    }
}

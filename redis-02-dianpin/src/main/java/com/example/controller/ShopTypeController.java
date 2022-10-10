package com.example.controller;


import com.example.dto.Result;
import com.example.entity.ShopType;
import com.example.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商户控制器
 *
 * @author xiaoning
 * @date 2022/10/09
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService shopTypeService;

    @GetMapping("list")
    public Result queryTypeList() {

        return shopTypeService.listByCache();
    }
}

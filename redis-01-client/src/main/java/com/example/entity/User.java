package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaoning
 * @date 2022/10/07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String name;

    private Integer age;

}

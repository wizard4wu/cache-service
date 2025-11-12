package com.dev.wizard.cache.domain;

import lombok.Data;

import java.time.Instant;

@Data
public class TestUser {

    private Long userId;
    private String userName;

    //测试枚举、List等复杂类型

    private Instant birthdayDate;

    public TestUser (){

    }
    public TestUser(Long userId, String userName, Instant birthdayDate) {
        this.userId = userId;
        this.userName = userName;
        this.birthdayDate = birthdayDate;
    }
}

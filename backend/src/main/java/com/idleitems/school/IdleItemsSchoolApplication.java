package com.idleitems.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdleItemsSchoolApplication {

    public static void main(String[] args) {
        // 设置系统编码为UTF-8
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        SpringApplication.run(IdleItemsSchoolApplication.class, args);
    }

}
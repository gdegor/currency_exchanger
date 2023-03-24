package com.example.demo.DTOs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Currency extends DataTransferObject {
    private final Integer id;
    private final String code;
    private final String name;
    private final String sign;


    public Currency(Integer id, String code, String name, String sign) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}

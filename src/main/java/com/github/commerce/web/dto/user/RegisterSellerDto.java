package com.github.commerce.web.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterSellerDto extends RegisterUserDto{
    private String shopName;
    private String address;
}

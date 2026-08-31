package org.jejuro.miraero.domain.user.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCreateCommand {

    private String email;
    private String password;
}

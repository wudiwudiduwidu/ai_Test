package com.xz.aiTest.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserImformationRequest implements Serializable {
private String userName;
private String userProfile;
}

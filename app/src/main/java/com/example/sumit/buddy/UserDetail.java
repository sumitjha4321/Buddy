package com.example.sumit.buddy;

import java.io.Serializable;
import java.util.Date;


public class UserDetail implements Serializable {

    private String UId;
    private String email;
    private String name;
    private String dateOfBirth;
    private String profileUrl;
    private String oneSignalId;

    public String getOneSignalId() {
        return oneSignalId;
    }

    public void setOneSignalId(String oneSignalId) {
        this.oneSignalId = oneSignalId;
    }

    public UserDetail() {
    }

    public UserDetail(String email, String name, String profileUrl, String dateOfBirth, String oneSignalId) {
        this.name = name;
        this.email = email;
        this.profileUrl = profileUrl;
        this.dateOfBirth = dateOfBirth;
        this.oneSignalId = oneSignalId;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUId() {
        return UId;
    }

    public void setUId(String UId) {
        this.UId = UId;
    }

    @Override
    public String toString() {
        return "UId = " + getUId() + ", name = " + getName() + ", email = " + getEmail() + ", profile_url = " + getProfileUrl();
    }
}

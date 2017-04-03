package com.scorpion.sleep.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Friends {

    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("graduationYear")
    @Expose
    private String graduationYear;
    @SerializedName("university")
    @Expose
    private String university;
    @SerializedName("major")
    @Expose
    private String major;

    @SerializedName("acceptedFriendList")
    @Expose
    private List<String> acceptedFriendList = null;
    @SerializedName("pendingFriendList")
    @Expose
    private List<String> pendingFriendList = null;
    @SerializedName("_links")
    @Expose
    private Links links;


    // Getter and Setter
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getAcceptedFriendList() {
        return acceptedFriendList;
    }

    public void setAcceptedFriendList(List<String> acceptedFriendList) {
        this.acceptedFriendList = acceptedFriendList;
    }

    public List<String> getPendingFriendList() {
        return pendingFriendList;
    }

    public void setPendingFriendList(List<String> pendingFriendList) {
        this.pendingFriendList = pendingFriendList;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(String graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

}

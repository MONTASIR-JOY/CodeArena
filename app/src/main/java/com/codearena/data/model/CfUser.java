package com.codearena.data.model;

import com.google.gson.annotations.SerializedName;

public class CfUser {
    @SerializedName("handle")
    private String handle;

    @SerializedName("email")
    private String email;

    @SerializedName("vkId")
    private String vkId;

    @SerializedName("openId")
    private String openId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("country")
    private String country;

    @SerializedName("city")
    private String city;

    @SerializedName("organization")
    private String organization;

    @SerializedName("contribution")
    private int contribution;

    @SerializedName("rank")
    private String rank;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("maxRank")
    private String maxRank;

    @SerializedName("maxRating")
    private Integer maxRating;

    @SerializedName("lastOnlineTimeSeconds")
    private long lastOnlineTimeSeconds;

    @SerializedName("registrationTimeSeconds")
    private long registrationTimeSeconds;

    @SerializedName("friendOfCount")
    private int friendOfCount;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("titlePhoto")
    private String titlePhoto;

    public CfUser() {}

    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public int getContribution() { return contribution; }
    public void setContribution(int contribution) { this.contribution = contribution; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getMaxRank() { return maxRank; }
    public void setMaxRank(String maxRank) { this.maxRank = maxRank; }

    public Integer getMaxRating() { return maxRating; }
    public void setMaxRating(Integer maxRating) { this.maxRating = maxRating; }

    public long getLastOnlineTimeSeconds() { return lastOnlineTimeSeconds; }
    public void setLastOnlineTimeSeconds(long lastOnlineTimeSeconds) { this.lastOnlineTimeSeconds = lastOnlineTimeSeconds; }

    public long getRegistrationTimeSeconds() { return registrationTimeSeconds; }
    public void setRegistrationTimeSeconds(long registrationTimeSeconds) { this.registrationTimeSeconds = registrationTimeSeconds; }

    public int getFriendOfCount() { return friendOfCount; }
    public void setFriendOfCount(int friendOfCount) { this.friendOfCount = friendOfCount; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getTitlePhoto() { return titlePhoto; }
    public void setTitlePhoto(String titlePhoto) { this.titlePhoto = titlePhoto; }
}


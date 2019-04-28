package com.test.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {
    private String username;
    private String password;
    private String ip;
    private String port;
    private long camera;
    private String date;

    @JsonProperty("username")
    public String getUsername() { return username; }
    @JsonProperty("username")
    public void setUsername(String value) { this.username = value; }

    @JsonProperty("password")
    public String getPassword() { return password; }
    @JsonProperty("password")
    public void setPassword(String value) { this.password = value; }

    @JsonProperty("ip")
    public String getIP() { return ip; }
    @JsonProperty("ip")
    public void setIP(String value) { this.ip = value; }

    @JsonProperty("port")
    public String getPort() { return port; }
    @JsonProperty("port")
    public void setPort(String value) { this.port = value; }

    @JsonProperty("camera")
    public long getCamera() { return camera; }
    @JsonProperty("camera")
    public void setCamera(long value) { this.camera = value; }

    @JsonProperty("date")
    public String getDate() { return date; }
    @JsonProperty("date")
    public void setDate(String value) { this.date = value; }
}


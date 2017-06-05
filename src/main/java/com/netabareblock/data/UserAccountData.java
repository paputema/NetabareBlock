package com.netabareblock.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "useraccount")
public class UserAccountData {
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public String getAccessToken() {
		return AccessToken;
	}
	public void setAccessToken(String accessToken) {
		AccessToken = accessToken;
	}
	public String getAccessTokenSecret() {
		return AccessTokenSecret;
	}
	public void setAccessTokenSecret(String accessTokenSecret) {
		AccessTokenSecret = accessTokenSecret;
	}
	@Id
	@Column
	private Long userid;
	@Column(name = "accesstoken")
	private String AccessToken;
	@Column(name = "accesstokensecret")
	private String AccessTokenSecret;
}

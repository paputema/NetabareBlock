package com.netabareblock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "NetabareBlock.api")
public class NetabareBlockConfig {
	public String getConsumerKey() {
		return ConsumerKey;
	}
	public void setConsumerKey(String consumerKey) {
		ConsumerKey = consumerKey;
	}
	public String getConsumerSecret() {
		return ConsumerSecret;
	}
	public void setConsumerSecret(String consumerSecret) {
		ConsumerSecret = consumerSecret;
	}
	public String getDomain() {
		return Domain;
	}
	public void setDomain(String domain) {
		Domain = domain;
	}
	public Long getAdminTwitterId() {
		return AdminTwitterId;
	}
	public void setAdminTwitterId(Long adminTwitterId) {
		AdminTwitterId = adminTwitterId;
	}
	private String ConsumerKey;
	private String ConsumerSecret;
	private String Domain;
	private Long AdminTwitterId;
}

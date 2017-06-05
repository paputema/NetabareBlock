package com.netabareblock.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name = "netabareaccount")
public class NetabareAccountData {
	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}


	@Id
	@Column
	private Long userid;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}


	public String toStoring() {

		return userid.toString() + ":" + result ;

	}
	@Transient
	private String result  = "";


}

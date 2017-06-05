package com.netabareblock.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.netabareblock.data.UserAccountData;


@Repository
public interface UserAccountDataRepository  extends JpaRepository<UserAccountData,Long>{
	public UserAccountData findByUserid(Long userid) ;
}

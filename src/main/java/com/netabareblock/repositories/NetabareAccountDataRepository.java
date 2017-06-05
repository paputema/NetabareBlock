package com.netabareblock.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.netabareblock.data.NetabareAccountData;

@Repository
public interface NetabareAccountDataRepository  extends JpaRepository<NetabareAccountData,Long>{
	public List<NetabareAccountData> findByUserid(Long userid) ;
}

package com.dinesh.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dinesh.entity.DcCaseEntity;

@Repository
public interface DcCaseRepo extends JpaRepository<DcCaseEntity, Serializable> {
	public DcCaseEntity findByAppId(Integer appId);
	

	@Query("select max(caseNum) from DcCaseEntity")
	public Optional<Long> findMaxCaseNo();
	
	public Optional<DcCaseEntity> findByCaseNo(Long caseNum);
}

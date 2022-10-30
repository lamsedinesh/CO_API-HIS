package com.dinesh.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dinesh.entity.CoTriggerEntity;

@Repository
public interface CoTriggerRepository extends JpaRepository<CoTriggerEntity, Serializable> {
	public List<CoTriggerEntity> findByTriggerStatus(String triggerStatus);
}

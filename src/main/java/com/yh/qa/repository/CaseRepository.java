package com.yh.qa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yh.qa.entity.Case;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

}

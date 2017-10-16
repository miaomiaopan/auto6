package com.yh.qa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yh.qa.entity.Case;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
	//统计某个项目某天执行次数，用来生成batch_no
    @Query(value = "SELECT count(distinct(batch_no)) FROM test_log WHERE batch_no LIKE ?1 AND project_name=?2",nativeQuery = true)
    int countBatchNoForProject(String dateStr, String projectName);
    
    //批量更新case的batchNo
    @Transactional
    @Modifying
    @Query(value = "update test_log set batch_no = :batch_no WHERE id in (:caseIds)",nativeQuery = true)
    int updateBatchNo(@Param(value = "batch_no") String batch_no, @Param(value = "caseIds") List<String> caseIds);
}

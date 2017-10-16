package com.yh.qa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

//case实体
@Entity
@Table(name="test_log")
public class Case {
    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	@GeneratedValue(generator = "system-uuid")
	@Column(name = "id",length=32)
    private String id;

    @Column(name="batch_no")
    private String batchNo;

    @Column(name="project_name")
    private String projectName;

    @Column(name="module_name")
    private String moduleName;

    @Column(name="test_name", length=2048)
    private String testName;

    @Column(name="begin_time")
    private String beginTime;

    @Column(name="end_time")
    private String endtime;

    @Column(name="last_time")
    private String lastTime;

    @Column(name="status")
    private String status;

    @Column(name="description", length=2048)
    private String description;

    // 历史平均时间
    @Column(name = "avg_time")
    private String avgTime;

    //增加性能衰退的参数
    @Column(name="performance_degradation")
    private String perfDegrade;

    public String getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(String avgTime) {
        this.avgTime = avgTime;
    }

    public String getPerfDegrade() {
        return perfDegrade;
    }

    public void setPerfDegrade(String perfDegrade) {
        this.perfDegrade = perfDegrade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

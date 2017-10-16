package com.yh.qa.basecase;

import com.yh.qa.entity.Case;
import com.yh.qa.repository.CaseRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
public class BaseTestCase extends AbstractTestNGSpringContextTests {
	protected Case testcase;
	@Autowired
	protected CaseRepository caseRepository;
	// 时间格式化
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	// 模块名
	@Value("${moduleName}")
	protected String moduleName;
	// 项目名
	@Value("${projectName}")
	protected String projectName;

	protected Date beginDate;

	@BeforeMethod
	public void setUp() {
		// 建立case实例，记录case执行结果
		testcase = new Case();
		// 项目名
		testcase.setProjectName(projectName);
		testcase.setModuleName(moduleName);
		// case开始执行时间
		beginDate = new Date();
		testcase.setBeginTime(sdf.format(beginDate));
	}

	@AfterMethod
	public void tearDown() {
		// case 执行结果
		Date endDate = new Date();
		// case结束时间
		testcase.setEndtime(sdf.format(endDate));
		// case执行所花时间
		testcase.setLastTime(String.valueOf(endDate.getTime() - beginDate.getTime()));

		// 如果case执行成功，则设置case状态为SUCCESS
		if (StringUtils.isEmpty(testcase.getStatus())) {
			testcase.setStatus("SUCCESS");
		}
		caseRepository.save(testcase);
	}
}

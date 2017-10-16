package com.yh.qa.basecase;

import com.yh.qa.entity.Case;
import com.yh.qa.repository.CaseRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class BaseTestCase extends AbstractTestNGSpringContextTests {
    protected Case testcase;
    @Autowired
    protected CaseRepository caseRepository;
    // 时间格式化
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    // 项目名
    @Value("${projectName}")
    protected String projectName;
    // 模块名
    @Value("${moduleName}")
    protected String moduleName;
    private Date beginDate;
    
    // caseId的list
    private static List<String> caseIds = new ArrayList<String>();
    
    @BeforeClass
    public void setUp(){
        // 建立case实例，记录case执行结果
        testcase = new Case();
        // 项目名
        testcase.setProjectName(projectName);
        // 模块名
        testcase.setModuleName(moduleName);;
        // case开始执行时间
        beginDate = new Date();
        testcase.setBeginTime(sdf.format(beginDate));
    }

    @AfterClass
    public void tearDown(){
        // case 执行结果
        Date endDate = new Date();
        // case结束时间
        testcase.setEndtime(sdf.format(endDate));
        // case执行所花时间
        testcase.setLastTime(String.valueOf(endDate.getTime() - beginDate.getTime()));
        
        //如果case执行成功，则设置case状态为SUCCESS
        if(StringUtils.isEmpty(testcase.getStatus())){
        	testcase.setStatus("SUCCESS");
        }
        
        Case cs =  caseRepository.save(testcase);
        caseIds.add(cs.getId());
    }
    
    @AfterSuite
 	public void testAfterSuite() {
    	// batchNo   每次执行完所有case，批量更新batchNo
     	String batchNo;
 		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
 		int count = caseRepository.countBatchNoForProject(dateStr + "%", projectName);
 		batchNo = dateStr + "_" + (count + 1);
 		System.out.println("case 个数"+caseIds.size());
 		caseRepository.updateBatchNo(batchNo, caseIds);
 	}

}

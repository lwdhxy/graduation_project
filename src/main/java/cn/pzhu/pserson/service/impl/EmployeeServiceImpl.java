package cn.pzhu.pserson.service.impl;

import cn.pzhu.pserson.dao.dao.DeptMapper;
import cn.pzhu.pserson.dao.dao.EmployeeMapper;
import cn.pzhu.pserson.dao.dao.HourMapper;
import cn.pzhu.pserson.dao.dao.JobMapper;
import cn.pzhu.pserson.domain.*;
import cn.pzhu.pserson.domain.response.EmployeeResDTO;
import cn.pzhu.pserson.domain.response.HourResDto;
import cn.pzhu.pserson.service.EmployeeService;
import cn.pzhu.pserson.util.PinyinUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class EmployeeServiceImpl implements EmployeeService {


  @Resource
  JobMapper jobMapper;

  @Resource
  DeptMapper deptMapper;

  @Resource
  EmployeeMapper employeeMapper;
  @Resource
  HourMapper hourMapper;

  @Override
  public Map<String, Object> getInfo() {
    List<Job> jobs = jobMapper.get_List();
    List<Dept> depts = deptMapper.selectAllDept();
    Map<String,Object> map = new HashMap<>();
    map.put("jobs",jobs);
    map.put("depts",depts);
    return map;
  }

  @Override
  public PageInfo getEmployee(int pageNum,int pageSize) {
    PageInfo<Object> pageInfo = PageHelper.startPage(pageNum, pageSize, true)
        .doSelectPageInfo(() -> employeeMapper.getEm());
    return pageInfo;
  }

  @Override
  public void insert(Employee employee) {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
    employee.setCreateDate(format.format(date));
    employee.setLoginpassword("0000");
    employee.setLoginname(getname(employee.getName()));
    employeeMapper.insert(employee);
  }
  private String getname(String user) {
    String loginname = PinyinUtil.getLoginname(user);
    int count = employeeMapper.selectByName(loginname);
    if(count != 0){
      loginname += ("0"+count);
    }
    return loginname;
  }

  @Override
  public void update(Employee employee) {
    Employee employee1 = employeeMapper.selectByPrimaryKey(employee.getId());
    if(!employee1.getBasepay().equals("")){
      if(employee1.getBasepay() != employee.getBasepay()){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        int userid = (int) request.getSession().getAttribute("userid");
        employee.setUpdatepayoperator(userid);
        employee.setUpdatepaytime(LocalDateTime.now());
      }
    }
    employeeMapper.updateByPrimaryKey(employee);
  }

  @Override
  public PageInfo getEmployee(String content, int pageNum, int pageSize) {
    PageInfo<Object> pageInfo = PageHelper.startPage(pageNum, pageSize, true)
        .doSelectPageInfo(() -> employeeMapper.getEmployees(content));
    return pageInfo;
  }

  @Override
  public Employee getEmployee(Integer id) {
    return employeeMapper.selectByPrimaryKey(id);
  }

  @Override
  public EmployeeResDTO employeedetails(Integer id) {

    return employeeMapper.employeedetails(id);
  }

  @Override
  public void inhour(Hour hour, int userid) {
    hour.setEmpid(userid);
    hour.setCreatetime(LocalDateTime.now());
    hour.setState("submitted");
    hourMapper.insert(hour);
  }

  @Override
  public List<HourResDto> hourList(int userid, String worktime) {
    return hourMapper.selectKey(userid, worktime);
  }


}

package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    /**
     *
     *
     * @param employeeId
     * @return
     */
    @Override
    public ReportingStructure getReportingStructure(String employeeId) {
        Employee employee = read(employeeId);

        int numberOfReports = calculateNumberOfReports(employee);

        return new ReportingStructure(employee, numberOfReports);
    }

    /**
     *
     *
     * @param employee
     * @return
     */
    private int calculateNumberOfReports(Employee employee) {
        int numberOfReports = 0;

        /**
         * Thought Process
         * I need to iterate through the direct reports of the provided employee,
         * and recursively through their reports which could either be an employee id
         * or potentially an object representing the employee record
         *
         * for each valid report, numberOfReports++
         */

        return numberOfReports;
    }

}

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
     * Recursively calculates the total number of reports (direct and indirect)
     * for a given employee.
     *
     * This method handles the case where the employee's direct reports list may
     * contain either fully or populated employee objects or just employee ids.
     *
     * Edge cases:
     * If the employee has no direct reports, returns 0.
     * If a direct report is invalid or cannot be resolved, that branch is counted as 0.
     *
     * @param employee the employee whose reporting hierarchy is being counted.
     * @return the total number of direct and indirect reports under the given employee.
     */
    private int calculateNumberOfReports(Employee employee) {

        /**
         * Thought Process Task 1
         * I need to iterate through the direct reports of the provided employee,
         * and recursively through their reports which could either be an employee id
         * or potentially an object representing the employee record
         *
         * for each valid report, numberOfReports++
         */

        // I chose to stream elements of the direct reports array here MOSTLY because
        // after working with RXjava professionally for so many years,
        // I find this way more comfortable and readable :)

        //However there _are_ some benefits here worth noting,
        // .sum handles null/empty automatically (stream().sum on an empty stream = 0)
        // and I was able to add parallelism much easier,
        // which doesn't save much time here,
        // but would certainly make a difference across a large org chart.

        //this also makes future changes much easier, if say we wanted to
        //filter to see direct reports of a certain department

        //Null check first to avoid a NPE on checking if empty
        if (employee.getDirectReports() == null || employee.getDirectReports().isEmpty()) {
            LOG.debug("No direct reports found for employee [{}]", employee.getFirstName());
            return 0;
        }

        return employee.getDirectReports().parallelStream()
                .mapToInt(report -> {
                    //report could be an Employee, or as we see in the test data, just an employeeId.
                    Employee fullReport;
                    if (report.getFirstName() == null) {
                        LOG.debug("Unpopulated report found for employee: [{}]. Checking db", employee.getEmployeeId());
                        fullReport = read(report.getEmployeeId());
                    } else {
                        LOG.debug("Populated report found for [{}].", employee.getEmployeeId());
                        fullReport = report;
                    }
                    return 1 + calculateNumberOfReports(fullReport);
                })
                .sum();
    }
}

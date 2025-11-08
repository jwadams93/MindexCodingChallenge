package com.mindex.challenge.service;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee read(String id);
    Employee update(Employee employee);
    /*
        Task 1 thought:
        While a separate service could be created for reporting structure,
        in this case I've chosen to add this logic as a part of the employeeService.
        Since we will be fetching an employee and computing something about that employee,
        I consider this to be fundamentally an employee operation. On top of that,
        reporting logic will only be reused in our one controller.
     */
    ReportingStructure getReportingStructure(String employeeId);
}

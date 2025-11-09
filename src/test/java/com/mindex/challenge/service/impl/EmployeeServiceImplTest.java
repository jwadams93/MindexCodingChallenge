package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String directReportsUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        directReportsUrl = "http://localhost:" + port + "/employee/{id}/reportingStructure";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testReadReportStructure() {
        Employee newEmployee = new Employee();
        newEmployee.setFirstName("Gandalf");
        newEmployee.setLastName("TheGrey");
        newEmployee.setDepartment("Wizarding");
        newEmployee.setPosition("Wizard");

        // Create checks
        Employee createdNewEmployee = restTemplate.postForEntity(employeeUrl, newEmployee, Employee.class).getBody();

        assertNotNull(createdNewEmployee.getEmployeeId());
        assertEmployeeEquivalence(newEmployee, createdNewEmployee);

        // Read checks
        Employee readNewEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdNewEmployee.getEmployeeId()).getBody();
        assertEquals(createdNewEmployee.getEmployeeId(), readNewEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdNewEmployee, readNewEmployee);

        //Create new top-of-hierarchy employee
        List<Employee> directReports = new ArrayList<>();
        directReports.add(createdNewEmployee);

        Employee newestEmployee = new Employee();
        newestEmployee.setFirstName("Jake");
        newestEmployee.setLastName("Adams");
        newestEmployee.setDepartment("Engineering");
        newestEmployee.setPosition("Developer");
        newestEmployee.setDirectReports(directReports);

        Employee createdNewestEmployee = restTemplate.postForEntity(employeeUrl, newestEmployee, Employee.class).getBody();

        assertNotNull(createdNewestEmployee.getEmployeeId());
        assertEmployeeEquivalence(newestEmployee, createdNewestEmployee);

        // Read newest employee
        Employee readNewestEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdNewestEmployee.getEmployeeId()).getBody();
        assertEquals(createdNewestEmployee.getEmployeeId(), readNewestEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdNewestEmployee, readNewestEmployee);

        // Get the reporting structure
        ReportingStructure reportingStructure = restTemplate.getForEntity(directReportsUrl,
                ReportingStructure.class,
                readNewestEmployee.getEmployeeId()).getBody();

        assertEquals(1, reportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadReportStructureNoReportingEmployees() {
        Employee newEmployee = new Employee();
        newEmployee.setFirstName("Gandalf");
        newEmployee.setLastName("TheGrey");
        newEmployee.setDepartment("Wizarding");
        newEmployee.setPosition("Wizard");

        // Create checks
        Employee createdNewEmployee = restTemplate.postForEntity(employeeUrl, newEmployee, Employee.class).getBody();

        assertNotNull(createdNewEmployee.getEmployeeId());
        assertEmployeeEquivalence(newEmployee, createdNewEmployee);

        // Read checks
        Employee readNewEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdNewEmployee.getEmployeeId()).getBody();
        assertEquals(createdNewEmployee.getEmployeeId(), readNewEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdNewEmployee, readNewEmployee);

        // Get the reporting structure
        ReportingStructure reportingStructure = restTemplate.getForEntity(directReportsUrl,
                ReportingStructure.class,
                readNewEmployee.getEmployeeId()).getBody();

        assertEquals(0, reportingStructure.getNumberOfReports());
    }

    @Test
    public void testCalculateNumberOfReports() {
        ReportingStructure numberOfReports = employeeService
                .getReportingStructure("16a596ae-edd3-4847-99fe-c4518e82c86f");
        assertEquals(4, numberOfReports.getNumberOfReports());
        assertEquals("John", numberOfReports.getEmployee().getFirstName());
    }

    @Test
    public void testCalculateNumberOfReportsWhenNoReports() {
        ReportingStructure report = employeeService
                .getReportingStructure("b7839309-3348-463b-a7e3-5de1c168beb3");
        assertEquals(0, report.getNumberOfReports());
        assertEquals("Paul", report.getEmployee().getFirstName());
    }

    @Test
    public void testCalculateNumberOfReportsWhenNoEmployee() {
        Exception exception = assertThrows(RuntimeException.class,
                () -> employeeService.getReportingStructure(""));
        assertTrue(exception.getMessage().contains("Invalid employeeId"));
    }

    @Test
    public void testCalculateNumberPopulatedAsExpected() {
        ReportingStructure reportingStructure = employeeService
                .getReportingStructure("16a596ae-edd3-4847-99fe-c4518e82c86f");
        assertEquals(4, reportingStructure.getNumberOfReports());
        assertEquals("John", reportingStructure.getEmployee().getFirstName());
        assertEquals("Lennon", reportingStructure.getEmployee().getLastName());
        assertEquals("Engineering", reportingStructure.getEmployee().getDepartment());
        assertEquals("Development Manager", reportingStructure.getEmployee().getPosition());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}

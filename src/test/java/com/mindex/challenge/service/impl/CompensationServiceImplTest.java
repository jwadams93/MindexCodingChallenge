package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;
    private String employeeIdCompensationUrl;

    private final String EMPLOYEE_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        employeeIdCompensationUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateReadCompensation() {
        Compensation testComp = new Compensation();
        testComp.setEmployeeId(EMPLOYEE_ID);
        testComp.setEffectiveDate(new Date());
        testComp.setSalary(100000);

        Compensation newCompensation =
                restTemplate.postForEntity(compensationUrl,
                        testComp,
                        Compensation.class).getBody();

        assertNotNull(newCompensation.getEmployeeId());
        assertEquals(newCompensation.getSalary(), testComp.getSalary());
        assertEquals(newCompensation.getEffectiveDate(), testComp.getEffectiveDate());

        List<Compensation> readCompensations =
                restTemplate.exchange(employeeIdCompensationUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Compensation>>() {
                        },
                        EMPLOYEE_ID).getBody();

        assertNotNull(readCompensations);
        assertEquals(1, readCompensations.size());
        assertEquals(EMPLOYEE_ID, readCompensations.get(0).getEmployeeId());
        assertEquals(100000, readCompensations.get(0).getSalary());
    }

    @Test
    public void testCreateReadMultipleCompensations() {
        Compensation testComp = new Compensation();
        String employee1 = EMPLOYEE_ID.substring(0, EMPLOYEE_ID.length() - 1) + 2;
        testComp.setEmployeeId(employee1);
        testComp.setEffectiveDate(new Date());
        testComp.setSalary(100000);

        //Create new Compensation
        Compensation newCompensation =
                restTemplate.postForEntity(compensationUrl,
                        testComp,
                        Compensation.class).getBody();

        assertNotNull(newCompensation.getEmployeeId());
        assertEquals(newCompensation.getSalary(), testComp.getSalary());
        assertEquals(newCompensation.getEffectiveDate(), testComp.getEffectiveDate());

        //Validate compensation was created
        List<Compensation> readCompensations =
                restTemplate.exchange(employeeIdCompensationUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Compensation>>() {
                        },
                        employee1).getBody();

        assertNotNull(readCompensations);
        assertEquals(1, readCompensations.size());
        assertEquals(employee1, readCompensations.get(0).getEmployeeId());
        assertEquals(100000, readCompensations.get(0).getSalary());

        //Add new compensation
        Compensation testComp1 = new Compensation();
        testComp1.setEmployeeId(employee1);
        testComp1.setEffectiveDate(new Date());
        testComp1.setSalary(120000);

        Compensation newNewCompensation =
                restTemplate.postForEntity(compensationUrl,
                        testComp1,
                        Compensation.class).getBody();

        assertNotNull(newNewCompensation.getEmployeeId());
        assertEquals(newNewCompensation.getSalary(), testComp1.getSalary());
        assertEquals(newNewCompensation.getEffectiveDate(), testComp1.getEffectiveDate());
        assertTrue(newNewCompensation.getEffectiveDate().after(newCompensation.getEffectiveDate()));

        //Check employee now has a compensation history
        List<Compensation> secondReadCompensations =
                restTemplate.exchange(employeeIdCompensationUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Compensation>>() {
                        },
                        employee1).getBody();

        assertNotNull(secondReadCompensations);
        assertEquals(2, secondReadCompensations.size());
        assertEquals(employee1, secondReadCompensations.get(0).getEmployeeId());
        assertEquals(employee1, secondReadCompensations.get(1).getEmployeeId());
        assertEquals(100000, secondReadCompensations.get(0).getSalary());
        assertEquals(120000, secondReadCompensations.get(1).getSalary());
    }

    @Test
    public void testReadCompensationNoCompensation() {
        String JUNK_ID = "Jake Adams";
        try {
            restTemplate.exchange(employeeIdCompensationUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Compensation>>() {
                    },
                    JUNK_ID).getBody();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("No compensation found for employee id"));
        }
    }

    @Test
    public void testCreateCompensationNoEmployeeId() {
        Compensation testComp = new Compensation();

        try {
            restTemplate.postForEntity(compensationUrl,
                    testComp,
                    Compensation.class).getBody();
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getMessage().contains("Employee Id is required."));
        }
    }

    @Test
    public void testCreateCompensationNoEffectiveDate() {
        Compensation testComp = new Compensation();
        testComp.setEmployeeId(EMPLOYEE_ID);

        try {
            restTemplate.postForEntity(compensationUrl,
                    testComp,
                    Compensation.class).getBody();
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getMessage().contains("Effective date is required."));
        }
    }

    @Test
    public void testCreateCompensationNegativeSalary() {
        Compensation testComp = new Compensation();
        testComp.setEmployeeId(EMPLOYEE_ID);
        testComp.setEffectiveDate(new Date());
        testComp.setSalary(-120000);

        try {
            restTemplate.postForEntity(compensationUrl,
                    testComp,
                    Compensation.class).getBody();
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertTrue(e.getMessage().contains("Salary must be positive."));
        }
    }
}

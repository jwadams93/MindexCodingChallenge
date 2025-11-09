package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rest Controller for managing Compensation resources.
 *
 * This controller exposes endpoints to create and read compensation records
 * for employees. Each compensation entry includes an employee Id, salary, and effective date,
 * allowing the app to maintain a historical record of salary changes over time.
 *
 * Task 2 Design notes
 *
 * I've tried to keep this class as similar as possible to the existing EmployeeController for consistency
 *
 * Basic input validation ensures that employee ids are provided, salaries are positive,
 * and effective dates are not null. Exceptions for these result in HTTP 400 responses.
 *
 */
@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    @PostMapping("/compensation")
    public Compensation create(@RequestBody Compensation compensation) {
        LOG.debug("Creating compensation for employee with id [{}]", compensation.getEmployeeId());

        return compensationService.create(compensation);
    }

    @GetMapping("/compensation/{id}")
    public List<Compensation> read(@PathVariable("id") String employeeId) {
        LOG.debug("Reading compensation(s) for employee with id [{}]", employeeId);

        return compensationService.read(employeeId);
    }
}

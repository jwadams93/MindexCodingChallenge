package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the CompensationService interface
 */
@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    /**
     * Creates a new Compensation record.
     * <p>
     * Validates the provided compensation data, ensuring that
     * The employee Id is not null or empty
     * The effective date is provided
     * The salary value is positive
     *
     * @param compensation the compensation object to create.
     * @return IllegalArgumentException if any required field is missing or invalid
     */
    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation for [{}].", compensation.getEmployeeId());

        validateCompensation(compensation);
        compensationRepository.insert(compensation);

        return compensation;
    }

    /**
     * Retrieves all compensation records for a given employee Id;
     * if none exist, a runtime exception is thrown to indicate missing data.
     *
     * @param employeeId unique identifer of the employee whe compensation records are being returned.
     * @return A list of compensation objects for the specified employee.
     * @throws RuntimeException if no compensation data is found for the given employee Id
     */
    @Override
    public List<Compensation> read(String employeeId) {
        LOG.debug("Reading compensation for [{}].", employeeId);

        List<Compensation> compensation = compensationRepository.findCompensationByEmployeeId(employeeId);

        if (compensationRepository.findCompensationByEmployeeId(employeeId) == null) {
            throw new RuntimeException("No compensation found for employee id [" + employeeId + "]");
        }

        return compensation;
    }

    private void validateCompensation(Compensation compensation) {
        if (compensation.getEmployeeId() == null || compensation.getEmployeeId().isEmpty()) {
            throw new IllegalArgumentException("Employee Id is required.");
        }
        if (compensation.getEffectiveDate() == null) {
            throw new IllegalArgumentException("Effective Date is required.");
        }
        if (compensation.getSalary() < 0) {
            throw new IllegalArgumentException("Salary must be positive.");
        }
    }
}

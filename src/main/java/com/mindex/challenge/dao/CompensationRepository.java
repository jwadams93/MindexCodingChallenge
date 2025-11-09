package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {
    //Task 2 thought: Implementing this to return a list of Compensations,
    // since effective date leads to me believe an employee may have multiple compensations
    List<Compensation> findCompensationByEmployeeId(String employeeId);
}

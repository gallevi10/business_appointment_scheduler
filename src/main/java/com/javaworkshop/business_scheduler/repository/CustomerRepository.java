package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// This interface defines the repository for managing Customer entities.
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // retrieves a customer by their email and phone number
    Optional<Customer> findByEmailAndPhone(String email, String phone);

    // retrieves a customer by their username
    Optional<Customer> findByUserUsername(String username);

    // checks if a customer exists by their email excluding a specified customer
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Customer c
            WHERE (:customer IS NULL OR c <> :customer)
            AND c.email = :email
            """)
    boolean existsByEmail(@Param("customer") Customer customer,
                          @Param("email") String email);

    // checks if a customer exists by their phone number excluding a specified customer
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Customer c
            WHERE (:customer IS NULL OR c <> :customer)
            AND c.phone = :phone
            """)
    boolean existsByPhone(@Param("customer") Customer customer,
                          @Param("phone") String phone);
}

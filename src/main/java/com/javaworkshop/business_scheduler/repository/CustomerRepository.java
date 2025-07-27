package com.javaworkshop.business_scheduler.repository;

import com.javaworkshop.business_scheduler.model.Customer;
import com.javaworkshop.business_scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmailAndPhone(String email, String phone);

    Optional<Customer> findByUserUsername(String username);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Customer c
            WHERE (:customer IS NULL OR c <> :customer)
            AND c.email = :email
            """)
    boolean existsByEmail(@Param("customer") Customer customer,
                          @Param("email") String email);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Customer c
            WHERE (:customer IS NULL OR c <> :customer)
            AND c.phone = :phone
            """)
    boolean existsByPhone(@Param("customer") Customer customer,
                          @Param("phone") String phone);
}

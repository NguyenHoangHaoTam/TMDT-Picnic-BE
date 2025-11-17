package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Address;
import com.java.TMDTPicnic.entity.User;
import org.apache.commons.math3.analysis.function.Add;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;


public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void updateAllIsDefaultFalse(@Param("userId") Long userId);

    List<Address> findByUserId(Long id);

}

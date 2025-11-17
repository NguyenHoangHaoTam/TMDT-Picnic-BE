package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.SharedCart;
import com.java.TMDTPicnic.entity.SharedCartParticipant;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedCartParticipantRepository extends JpaRepository<SharedCartParticipant, Long> {
    List<SharedCartParticipant> findBySharedCartId(Long sharedCartId);
    boolean existsBySharedCartAndUser(SharedCart sharedCart, User user);
    java.util.Optional<SharedCartParticipant> findBySharedCartAndUser(SharedCart sharedCart, User user);
}

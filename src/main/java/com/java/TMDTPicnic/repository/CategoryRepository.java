package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParent_Id(Long parentId);
}

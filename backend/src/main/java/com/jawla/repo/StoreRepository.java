package com.jawla.repo;
import com.jawla.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StoreRepository extends JpaRepository<Store,Long> {}
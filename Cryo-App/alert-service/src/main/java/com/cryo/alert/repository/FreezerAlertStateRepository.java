package com.cryo.alert.repository;

import com.cryo.alert.entity.FreezerAlertState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreezerAlertStateRepository extends JpaRepository<FreezerAlertState, String> {
}
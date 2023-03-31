package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {
}

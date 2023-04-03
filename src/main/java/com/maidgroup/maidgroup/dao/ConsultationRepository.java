package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {

    @Query("select * from Consultation where status = :status")
    public List<Consultation> findByStatus(@Param("status") ConsultationStatus consultationStatus);

    @Query("select * from Consultation where date = :date")
    public List<Consultation> findByDate(@Param("date") LocalDate date);


}

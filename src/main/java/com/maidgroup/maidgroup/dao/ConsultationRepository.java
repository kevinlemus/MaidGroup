package com.maidgroup.maidgroup.dao;

import com.maidgroup.maidgroup.model.Consultation;
import com.maidgroup.maidgroup.model.consultationinfo.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {

    @Query("select c from Consultation c where c.status = :status")
    public List<Consultation> findByStatus(@Param("status") ConsultationStatus consultationStatus);

    @Query("select c from Consultation c where c.date = :date")
    public List<Consultation> findByDate(@Param("date") LocalDate date);

    @Query("select c from Consultation c where c.phoneNumber = :phoneNumber")
    public Optional<Consultation> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

}

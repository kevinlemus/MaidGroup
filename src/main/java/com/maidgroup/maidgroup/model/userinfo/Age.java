package com.maidgroup.maidgroup.model.userinfo;

import java.time.LocalDate;
import java.time.Period;

public class Age {

    public int getAge(LocalDate dateOfBirth){
        LocalDate currentDate = LocalDate.now();
        return Period.between(dateOfBirth, currentDate).getYears();

    }
}

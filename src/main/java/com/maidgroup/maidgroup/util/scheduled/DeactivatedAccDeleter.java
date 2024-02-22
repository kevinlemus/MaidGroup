package com.maidgroup.maidgroup.util.scheduled;

import com.maidgroup.maidgroup.dao.UserRepository;
import com.maidgroup.maidgroup.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DeactivatedAccDeleter {

    @Autowired
    UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?")  // Runs once a day at midnight
    public void deleteDeactivatedAccounts() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<User> usersToDelete = userRepository.findAllByDeactivationDateBefore(thirtyDaysAgo);
        userRepository.deleteAll(usersToDelete);
    }
}


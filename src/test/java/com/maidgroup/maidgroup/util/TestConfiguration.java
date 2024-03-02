package com.maidgroup.maidgroup.util;

import com.maidgroup.maidgroup.util.square.mock.SquareClientWrapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class TestConfiguration {

    @Bean
    public SquareClientWrapper testSquareClientWrapper() {
        // Using Mockito directly with an accurate import statement
        return Mockito.mock(SquareClientWrapper.class);
    }
}
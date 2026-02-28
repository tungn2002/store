package com.personal.store_api.dto.response;

import com.personal.store_api.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    String id;
    String name;
    String email;
    String phoneNumber;
    LocalDate dateOfBirth;
    Gender gender;
    String address;
}

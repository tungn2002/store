package com.personal.store_api.dto.request;

import com.personal.store_api.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    @NotBlank(message = "error.name.blank")
    @Size(min = 2, max = 100, message = "error.name.size")
    String name;

    @NotBlank(message = "error.email.blank")
    @Email(message = "error.email.invalid")
    String email;

    @Size(min = 10, max = 15, message = "error.phone.size")
    String phoneNumber;

    @Past(message = "error.dob.past")
    LocalDate dateOfBirth;

    Gender gender;

    String address;
}

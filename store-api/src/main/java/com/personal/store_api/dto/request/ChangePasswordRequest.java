package com.personal.store_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "error.old_password.blank")
    String oldPassword;

    @NotBlank(message = "error.new_password.blank")
    @Size(min = 8, max = 50, message = "error.new_password.size")
    String newPassword;

    @NotBlank(message = "error.confirm_password.blank")
    String confirmPassword;
}

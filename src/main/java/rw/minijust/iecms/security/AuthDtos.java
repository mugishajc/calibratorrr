package rw.minijust.iecms.security;

import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank @Size(max = 50) String username,
                               @NotBlank @Size(max = 100) String password) {}

    public record TokenResponse(String accessToken, String tokenType, long expiresIn, String username, Role role) {}

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "[a-z0-9._-]+",
                    message = "use lower-case letters, digits, dot, dash or underscore") String username,
            @NotBlank @Size(min = 8, max = 100) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                    message = "must contain upper-case, lower-case letters and a digit") String password,
            @NotNull Role role) {}

    public record UserResponse(Long id, String username, Role role) {
        public static UserResponse of(AppUser u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getRole());
        }
    }
}

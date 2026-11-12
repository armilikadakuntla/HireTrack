package com.hiretrack.controller;

import com.hiretrack.model.JobApplication;
import com.hiretrack.model.User;
import com.hiretrack.repository.JobApplicationRepository;
import com.hiretrack.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository users;
    private final JobApplicationRepository applications;
    private final PasswordEncoder encoder;

    public ApiController(UserRepository users, JobApplicationRepository applications, PasswordEncoder encoder) {
        this.users = users; this.applications = applications; this.encoder = encoder;
    }

    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @NotBlank String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record ApplicationRequest(@NotBlank String company, @NotBlank String role, String location,
                                     String jobUrl, String status, String appliedDate, String notes) {}
    public record UserResponse(Long id, String name, String email) {}
    public record ApplicationResponse(Long id, String company, String role, String location, String jobUrl,
                                      String status, LocalDate appliedDate, String notes) {}

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        User user = users.save(new User(req.name().trim(), email, encoder.encode(req.password())));
        session.setAttribute("userId", user.getId());
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @PostMapping("/auth/login")
    public UserResponse login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        if (!encoder.matches(req.password(), user.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        session.setAttribute("userId", user.getId());
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @GetMapping("/auth/me")
    public UserResponse me(HttpSession session) {
        User user = currentUser(session);
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @PostMapping("/auth/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Map.of("message", "Logged out");
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> list(HttpSession session) {
        User user = currentUser(session);
        return applications.findAllByUserIdOrderByAppliedDateDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody ApplicationRequest req, HttpSession session) {
        User user = currentUser(session);
        JobApplication app = new JobApplication();
        apply(app, req); app.setUser(user);
        return toResponse(applications.save(app));
    }

    @PutMapping("/applications/{id}")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody ApplicationRequest req, HttpSession session) {
        User user = currentUser(session);
        JobApplication app = applications.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        apply(app, req);
        return toResponse(applications.save(app));
    }

    @DeleteMapping("/applications/{id}")
    public Map<String, String> delete(@PathVariable Long id, HttpSession session) {
        User user = currentUser(session);
        JobApplication app = applications.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        applications.delete(app);
        return Map.of("message", "Application deleted");
    }

    private User currentUser(HttpSession session) {
        Object id = session.getAttribute("userId");
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in.");
        return users.findById((Long) id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in."));
    }

    private void apply(JobApplication app, ApplicationRequest req) {
        app.setCompany(req.company().trim());
        app.setRole(req.role().trim());
        app.setLocation(req.location());
        app.setJobUrl(req.jobUrl());
        String status = req.status() == null || req.status().isBlank() ? "Applied" : req.status();
        if (!List.of("Applied", "Online Assessment", "Interview", "Offer", "Rejected").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid application status.");
        app.setStatus(status);
        try { app.setAppliedDate(req.appliedDate() == null || req.appliedDate().isBlank() ? LocalDate.now() : LocalDate.parse(req.appliedDate())); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must be YYYY-MM-DD."); }
        app.setNotes(req.notes());
    }

    private ApplicationResponse toResponse(JobApplication a) {
        return new ApplicationResponse(a.getId(), a.getCompany(), a.getRole(), a.getLocation(),
            a.getJobUrl(), a.getStatus(), a.getAppliedDate(), a.getNotes());
    }
}

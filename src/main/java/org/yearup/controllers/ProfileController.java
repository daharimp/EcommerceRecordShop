package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.models.Profile;
import org.yearup.models.User;
import org.yearup.service.ProfileService;
import org.yearup.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("profile")
@CrossOrigin
@PreAuthorize("isAuthenticated()")   // only logged-in users can view/edit their profile
public class ProfileController
{
    private final ProfileService profileService;
    private final UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService)
    {
        this.profileService = profileService;
        this.userService = userService;
    }

    // GET https://localhost:8080/profile  -> the current user's profile
    @GetMapping
    public Profile getProfile(Principal principal)
    {
        return profileService.getByUserId(getUserId(principal));
    }

    // PUT https://localhost:8080/profile  -> update the current user's profile (200 OK)
    @PutMapping
    public Profile updateProfile(@RequestBody Profile profile, Principal principal)
    {
        return profileService.update(getUserId(principal), profile);
    }

    // shared helper: turn the logged-in Principal into this user's database id
    private int getUserId(Principal principal)
    {
        String userName = principal.getName();
        User user = userService.getByUserName(userName);
        return user.getId();
    }
}
package org.yearup.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Profile;
import org.yearup.repository.ProfileRepository;

@Service
public class ProfileService
{
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository)
    {
        this.profileRepository = profileRepository;
    }

    public Profile getByUserId(int userId)
    {
        // the profile's id IS the user id, so we look it up by that
        return profileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    public Profile update(int userId, Profile profile)
    {
        // load the existing profile (created at registration); 404 if somehow missing
        Profile existing = profileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        // copy each editable field from the incoming profile onto the stored one.
        // note: we do NOT copy userId from the body -- the logged-in user is the
        // source of truth, so a client can't edit someone else's profile.
        existing.setFirstName(profile.getFirstName());
        existing.setLastName(profile.getLastName());
        existing.setPhone(profile.getPhone());
        existing.setEmail(profile.getEmail());
        existing.setAddress(profile.getAddress());
        existing.setCity(profile.getCity());
        existing.setState(profile.getState());
        existing.setZip(profile.getZip());

        // id is already set, so save() updates the existing row
        return profileRepository.save(existing);
    }
    public Profile create(Profile profile)
    {
        return profileRepository.save(profile);
    }
}
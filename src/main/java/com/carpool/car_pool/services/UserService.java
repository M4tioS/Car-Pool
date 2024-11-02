package com.carpool.car_pool.services;

import com.carpool.car_pool.controllers.dtos.UserResponse;
import com.carpool.car_pool.repositories.UserRepository;
import com.carpool.car_pool.repositories.common.PageResponse;
import com.carpool.car_pool.repositories.entities.UserEntity;
import com.carpool.car_pool.services.converters.UserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service for users.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;

    /**
     * Retrieves paginated list of all users.
     *
     * @param page The page number (zero-based).
     * @param size The size of the page.
     * @return A {@link PageResponse} containing the paginated {@link UserResponse}.
     */
    public PageResponse<UserResponse> findAllUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<UserEntity> usersPage = userRepository.findAll(pageable);

        List<UserResponse> users = usersPage.stream()
                .map(userConverter::entityToDTO)
                .toList();

        return new PageResponse<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast()
        );
    }


    /**
     * Uploads or changes the profile picture of the current user.
     * If the user already has a profile picture, it will be replaced with the new one.
     *
     * @param file The new profile picture file to upload. Must be a valid image file.
     * @throws RuntimeException if the file upload fails.
     */
    public void uploadProfilePicture (@NotNull MultipartFile file){
        UserEntity currentUser = currentUserService.getCurrentUser();

        if(currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()){
            fileStorageService.deleteProfiePicture(currentUser.getProfilePicture());
        }

        String profilePicturePath = fileStorageService
                .saveProfilePicture(file, currentUser.getId());

        if (profilePicturePath != null) {
            currentUser.setProfilePicture(profilePicturePath);
            userRepository.save(currentUser);
        }
    }
}

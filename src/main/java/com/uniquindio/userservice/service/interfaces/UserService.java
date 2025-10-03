package com.uniquindio.userservice.service.interfaces;

import com.uniquindio.userservice.annotation.IsOwner;
import com.uniquindio.userservice.dto.*;


public interface UserService {

    UserResponse registerUser(UserRegistration user);

    PaginatedUserResponse getUsers(int page, int size);

    UserResponse getUser(int userId);

    UserResponse updateUser(int id, UserUpdateRequest userUpdateRequest);

    void deleteUser(int id);

    boolean updatePassword(PasswordRecoveryRequest passwordRecoveryRequest, int id);

    AccountStatusResponse verifyUserAccount(int userId);
}

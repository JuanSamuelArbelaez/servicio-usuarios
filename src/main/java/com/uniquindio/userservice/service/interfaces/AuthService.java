package com.uniquindio.userservice.service.interfaces;

import com.uniquindio.userservice.dto.*;

public interface AuthService {
    String login(LoginRequest loginRequest);

    OtpResponse requestOtp(OtpRequest otpRequest);
}
